package com.wedit.backend.api.reservation.service;


import com.wedit.backend.api.member.entity.Member;
import com.wedit.backend.api.member.repository.MemberRepository;
import com.wedit.backend.api.notification.dto.NotificationEvent;
import com.wedit.backend.api.notification.entity.NotificationType;
import com.wedit.backend.api.notification.entity.TargetDomainType;
import com.wedit.backend.api.reservation.dto.*;
import com.wedit.backend.api.reservation.entity.ConsultationSlot;
import com.wedit.backend.api.reservation.entity.Reservation;
import com.wedit.backend.api.reservation.entity.SlotStatus;
import com.wedit.backend.api.reservation.repository.ConsultationSlotRepository;
import com.wedit.backend.api.reservation.repository.ReservationRepository;
import com.wedit.backend.api.vendor.entity.Vendor;
import com.wedit.backend.api.vendor.repository.VendorRepository;
import com.wedit.backend.common.event.ReservationCancelledEvent;
import com.wedit.backend.common.event.ReservationCreatedEvent;
import com.wedit.backend.common.exception.ForbiddenException;
import com.wedit.backend.common.exception.NotFoundException;
import com.wedit.backend.common.response.ErrorStatus;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ConsultationSlotRepository consultationSlotRepository;
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final VendorRepository vendorRepository;

    // 특정 업체의 해당 월 상담 가능 시간 목록을 모두 조회
    // 인메모리가 아닌 DB 조회로 서버 부담 감소
    @Transactional(readOnly = true)
    public List<SlotResponseDTO> getAvailableSlots(Long vendorId, int year, int month) {
        
        log.debug("상담 가능 슬롯 조회 시작 - vendorId: {}, year: {}, month: {}", vendorId, year, month);

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<SlotResponseDTO> slots = consultationSlotRepository.findByVendorIdAndStartTimeBetween(vendorId, start, end)
                .stream()
                .map(SlotResponseDTO::from)
                .collect(Collectors.toList());
                
        log.info("상담 가능 슬롯 조회 완료 - vendorId: {}, {} 개 슬롯 반환", vendorId, slots.size());
        return slots;
    }

    // 상담 예약 생성
    // 동시성 문제 때문에 비관적 락 검
    public Long createReservation(Long memberId, ReservationRequestDTO request) {
        
        log.info("상담 예약 생성 시작 - memberId: {}, slotId: {}", memberId, request.slotId());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_USER.getMessage()));

        // 상담 슬롯 조회 및 락, 다른 트랜잭션은 대기 후 CS 진입
        // DB 단에서 보장
        ConsultationSlot slot = consultationSlotRepository.findByIdWithPessimisticLock(request.slotId())
                .orElseThrow(() -> new NotFoundException("상담 시간을 찾을 수 없습니다."));

        // 슬롯 상태 변경
        slot.book();

        // 예약 정보 생성 및 저장
        Reservation reservation = Reservation.builder()
                .member(member)
                .vendor(slot.getVendor())
                .visitDateTime(slot.getStartTime())
                .consultationSlotId(slot.getId())
                .build();
        reservationRepository.save(reservation);

        // 업체가 DRESS 타입이면, 투어 일지 생성 이벤트 발행
        // TourService와 강결합 회피 위함
        ReservationEventPayload payload = ReservationEventPayload.from(reservation);
        eventPublisher.publishEvent(new ReservationCreatedEvent(this, payload));

        Map<String, String> arguments = Map.of("vendorName", reservation.getVendor().getName());
        NotificationEvent notificationEvent = new NotificationEvent(
                member,
                NotificationType.RESERVATION_CONFIRMED,
                arguments,
                TargetDomainType.RESERVATION,
                reservation.getId()
        );
        eventPublisher.publishEvent(notificationEvent);

        log.info("상담 예약 생성 완료 및 이벤트 발행. reservationId: {}, memberId: {}", reservation.getId(), memberId);

        return reservation.getId();
    }

    // 내 예약 취소
    public void cancelReservation(Long memberId, Long reservationId) {
        
        log.info("상담 예약 취소 시작 - memberId: {}, reservationId: {}", memberId, reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_RESERVATION.getMessage()));

        if (!reservation.getMember().getId().equals(memberId)) {
            log.warn("예약 취소 권한 없음 - reservationId: {}, 요청자: {}, 예약자: {}", 
                    reservationId, memberId, reservation.getMember().getId());
            throw new ForbiddenException("자신만 예약을 취소할 수 있습니다.");
        }

        // 해당 예약과 연결된 상담 슬롯을 AVAILABLE 상태로 변경
        consultationSlotRepository.findById(reservation.getConsultationSlotId())
                .ifPresent(ConsultationSlot::makeAvailable);

        reservation.cancel();

        // 예약 취소 이벤트 발행
        ReservationEventPayload payload = ReservationEventPayload.from(reservation);
        eventPublisher.publishEvent(new ReservationCancelledEvent(this, payload));

        Map<String, String> arguments = Map.of("vendorName", reservation.getVendor().getName());
        NotificationEvent notificationEvent = new NotificationEvent(
                reservation.getMember(),
                NotificationType.RESERVATION_CANCELLED,
                arguments,
                TargetDomainType.RESERVATION,
                reservation.getId()
        );
        eventPublisher.publishEvent(notificationEvent);
        
        log.info("상담 예약 취소 완료 및 이벤트 발행. reservationId: {}, memberId: {}",  reservationId, memberId);
    }

    // 내 예약 목록 조회
    @Transactional(readOnly = true)
    public List<MyReservationResponseDTO> getMyReservations(Long memberId) {
        
        log.debug("내 예약 목록 조회 시작 - memberId: {}", memberId);

        List<Reservation> reservations = reservationRepository.findAllByMemberIdWithVendor(memberId);
        
        List<MyReservationResponseDTO> result = reservations.stream()
                .map(MyReservationResponseDTO::from)
                .collect(Collectors.toList());
                
        log.info("내 예약 목록 조회 완료 - memberId: {}, {} 개 예약 반환", memberId, result.size());
        return result;
    }

    // 특정 업체의 월간 예약 현황(날짜별 예약 가능 여부) 조회
    @Transactional(readOnly = true)
    public List<DateAvailabilityDTO> getMonthlyAvailability(Long vendorId, int year, int month) {
        
        log.debug("월별 예약 현황 조회 시작 - vendorId: {}, year: {}, month: {}", vendorId, year, month);

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        // 해당 월의 모든 슬롯 정보를 조회
        List<ConsultationSlot> slots = consultationSlotRepository.findByVendorIdAndStartTimeBetween(vendorId, startOfMonth, endOfMonth);
        
        // 날짜별로 슬롯 그룹화 후 각 날짜의 예약 가능 여부 계산
        Map<LocalDate, List<ConsultationSlot>> slotsByDate = slots.stream()
                .collect(Collectors.groupingBy(slot -> slot.getStartTime().toLocalDate()));

        // 해당 월의 모든 날짜에 대해 DTO 생성
        List<DateAvailabilityDTO> result = yearMonth.atDay(1).datesUntil(yearMonth.atEndOfMonth().plusDays(1))
                .map(date -> {
                    List<ConsultationSlot> dailySlots = slotsByDate.getOrDefault(date, List.of());

                    int totalSlots = dailySlots.size();
                    int availableSlots = (int) dailySlots.stream()
                            .filter(slot -> slot.getStatus() == SlotStatus.AVAILABLE)
                            .count();

                    return new DateAvailabilityDTO(date, availableSlots > 0, totalSlots, availableSlots);
                })
                .collect(Collectors.toList());
                
        log.info("월별 예약 현황 조회 완료 - vendorId: {}, {} 일 정보 반환", vendorId, result.size());
        return result;
    }

    // 특정 업체의 일간 예약 현황(시간별 예약 가능 여부) 조회
    @Transactional(readOnly = true)
    public List<SlotResponseDTO> getAvailableSlotsByDate(Long vendorId, int year, int month, int day) {
        
        log.debug("일별 예약 현황 조회 시작 - vendorId: {}, date: {}-{}-{}", vendorId, year, month, day);

        LocalDate date = LocalDate.of(year, month, day);
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        List<SlotResponseDTO> slots = consultationSlotRepository.findByVendorIdAndStartTimeBetween(vendorId, start, end)
                .stream()
                .map(SlotResponseDTO::from)
                .collect(Collectors.toList());
                
        log.info("일별 예약 현황 조회 완료 - vendorId: {}, {} 개 슬롯 반환", vendorId, slots.size());
        return slots;
    }

    public void createSlots(ConsultationSlotCreateRequestDTO request) {

        Vendor vendor = vendorRepository.findById(request.vendorId())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_VENDOR.getMessage() + " : " + request.vendorId()));

        List<ConsultationSlot> slots = request.startTimes().stream()
                .distinct()
                .map(startTime -> ConsultationSlot.builder()
                        .vendor(vendor)
                        .startTime(startTime)
                        .endTime(startTime.plusMinutes(30))
                        .build())
                .toList();

        consultationSlotRepository.saveAll(slots);

        log.info("{}개의 상담 슬롯이 업체 '{}'(ID:{})에 성공적으로 등록되었습니다.",
                slots.size(), vendor.getName(), vendor.getId());
    }
}
