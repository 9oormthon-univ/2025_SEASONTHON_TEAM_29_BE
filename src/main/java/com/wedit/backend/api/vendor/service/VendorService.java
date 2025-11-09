package com.wedit.backend.api.vendor.service;

import java.time.LocalDateTime;
import java.util.*;

import com.wedit.backend.api.vendor.entity.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wedit.backend.api.aws.s3.service.S3Service;
import com.wedit.backend.api.media.entity.Media;
import com.wedit.backend.api.media.entity.enums.MediaDomain;
import com.wedit.backend.api.media.service.MediaService;
import com.wedit.backend.api.vendor.dto.request.VendorCreateRequestDTO;
import com.wedit.backend.api.vendor.dto.response.ProductResponseDTO;
import com.wedit.backend.api.vendor.dto.response.VendorAddressResponseDTO;
import com.wedit.backend.api.vendor.dto.response.VendorBannerResponseDTO;
import com.wedit.backend.api.vendor.dto.response.VendorDetailResponseDTO;
import com.wedit.backend.api.vendor.entity.enums.DressOrigin;
import com.wedit.backend.api.vendor.entity.enums.DressStyle;
import com.wedit.backend.api.vendor.entity.enums.HallMeal;
import com.wedit.backend.api.vendor.entity.enums.HallStyle;
import com.wedit.backend.api.vendor.entity.enums.MakeupStyle;
import com.wedit.backend.api.vendor.entity.enums.StudioSpecialShot;
import com.wedit.backend.api.vendor.entity.enums.StudioStyle;
import com.wedit.backend.api.vendor.entity.enums.VendorType;
import com.wedit.backend.api.vendor.repository.RegionRepository;
import com.wedit.backend.api.vendor.repository.VendorProductQueryRepository;
import com.wedit.backend.api.vendor.repository.VendorRepository;
import com.wedit.backend.common.exception.BadRequestException;
import com.wedit.backend.common.exception.NotFoundException;
import com.wedit.backend.common.response.ErrorStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class VendorService {

	private final VendorRepository vendorRepository;
	private final RegionRepository regionRepository;
	private final S3Service s3Service;
	private final MediaService mediaService;
	private final ProductService productService;
	private final VendorProductQueryRepository vendorProductQueryRepository;

	@Transactional
	public Long createVendor(VendorCreateRequestDTO request) {
		log.info("업체 생성을 시작합니다. 업체명: '{}', 타입: {}", request.getName(), request.getVendorType());

		Region region = regionRepository.findByCode(request.getRegionCode())
			.orElseThrow(() -> new NotFoundException("지역 code를 찾을 수 없습니다: " + request.getRegionCode()));

		if (region.getLevel() != 3) {
			log.warn("잘못된 지역 레벨입니다. 최소 단위(level 3) 지역 코드가 필요합니다. regionId: {}, level: {}", region.getId(),
				region.getLevel());
			throw new BadRequestException(ErrorStatus.BAD_REQUEST_REQUIRED_LEAST_REGION_CODE.getMessage());
		}

		Vendor vendor = Vendor.builder()
			.name(request.getName())
			.vendorType(request.getVendorType())
			.phoneNumber(request.getPhoneNumber())
			.description(request.getDescription())
			.region(region)
			.fullAddress(request.getFullAddress())
			.addressDetail(request.getAddressDetail())
			.latitude(request.getLatitude())
			.longitude(request.getLongitude())
			.kakaoMapUrl(request.getKakaoMapUrl())
			.build();

		Vendor savedVendor = vendorRepository.save(vendor);

		if (request.getLogoImage() != null) {
			Media logo = request.getLogoImage().toEntity(MediaDomain.VENDOR, savedVendor.getId());
			Media savedLogo = mediaService.save(logo);
			savedVendor.setLogoMedia(savedLogo);
		}
		if (request.getMainImage() != null) {
			Media rep = request.getMainImage().toEntity(MediaDomain.VENDOR, savedVendor.getId());
			Media savedRep = mediaService.save(rep);
			savedVendor.setRepMedia(savedRep);
		}

		log.info("업체 생성 완료. ID: {}, 업체명: '{}'", savedVendor.getId(), savedVendor.getName());
		return savedVendor.getId();
	}

	@Transactional(readOnly = true)
	public VendorDetailResponseDTO getVendorDetail(Long vendorId) {
		log.debug("업체 상세 조회 시작 - vendorId: {}", vendorId);

		Vendor vendor = vendorRepository.findById(vendorId)
			.orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_VENDOR.getMessage() + " : " + vendorId));

		List<VendorDetailResponseDTO.ProductSummaryDTO> products = productService.getProductsByVendorId(vendor.getId());

		String repMediaUrl = (vendor.getRepMedia() != null)
			? s3Service.toCdnUrl(vendor.getRepMedia().getMediaKey())
			: null;

		log.info("업체 상세 조회 완료 - vendorId: {}, 상품 개수: {}", vendorId, products.size());

		return VendorDetailResponseDTO.builder()
			.vendorId(vendor.getId())
			.vendorName(vendor.getName())
			.vendorType(vendor.getVendorType())
			.phoneNumber(vendor.getPhoneNumber())
			.description(vendor.getDescription())
			.fullAddress(vendor.getFullAddress())
			.addressDetail(vendor.getAddressDetail())
			.repMediaUrl(repMediaUrl)
			.latitude(vendor.getLatitude())
			.longitude(vendor.getLongitude())
			.kakaoMapUrl(vendor.getKakaoMapUrl())
			.products(products)
			.build();
	}

	@Transactional(readOnly = true)
	public Page<VendorBannerResponseDTO> getVendorsForBanner(VendorType vendorType, Pageable pageable) {

		LocalDateTime twoWeeksAgo = LocalDateTime.now().minusWeeks(12);

		Page<Vendor> vendorPage = vendorRepository.findByVendorTypeOrderByRecentReviews(vendorType, twoWeeksAgo,
			pageable);

		return vendorPage.map(vendor -> {

			String logoUrl = (vendor.getLogoMedia() != null)
				? s3Service.toCdnUrl(vendor.getLogoMedia().getMediaKey())
				: null;
			String regionName = (vendor.getRegion() != null)
				? vendor.getRegion().getName()
				: null;

			return VendorBannerResponseDTO.builder()
				.vendorId(vendor.getId())
				.vendorName(vendor.getName())
				.logoImageUrl(logoUrl)
				.regionName(regionName)
				.averageRating(vendor.getAverageRating())
				.reviewCount(vendor.getReviewCount())
				.build();
		});
	}

	/**
	 * 입력받은 지역 코드들을 분석하여 모든 level 3 지역 코드들로 확장합니다.
	 * - null 또는 빈 리스트: 그대로 반환 (모든 지역 검색)
	 * - level 3 코드: 그대로 사용
	 * - level 2 코드: 해당 지역에 속하는 모든 level 3 코드들로 확장
	 * - level 1 코드: 해당 지역에 속하는 모든 level 3 코드들로 확장
	 */
	private List<String> expandToLevel3RegionCodes(List<String> inputRegionCodes) {
		if (inputRegionCodes == null || inputRegionCodes.isEmpty()) {
			log.debug("지역 코드가 null 또는 빈 리스트이므로 모든 지역을 대상으로 검색합니다.");
			return inputRegionCodes; // null이나 빈 리스트 그대로 반환하여 조건 제외
		}

		List<String> level3Codes = new ArrayList<>();

		for (String regionCode : inputRegionCodes) {
			Optional<Region> regionOpt = regionRepository.findByCode(regionCode);

			if (regionOpt.isEmpty()) {
				log.warn("존재하지 않는 지역 코드입니다: {}", regionCode);
				continue;
			}

			Region region = regionOpt.get();

			switch (region.getLevel()) {
				case 3:
					// level 3은 그대로 추가
					level3Codes.add(regionCode);
					break;

				case 2:
					// level 2의 하위 level 3들을 모두 조회해서 추가
					List<String> childLevel3Codes = regionRepository.findLevel3CodesByParentCode(regionCode);
					level3Codes.addAll(childLevel3Codes);
					log.debug("level 2 지역 {} 에서 {} 개의 level 3 지역 확장", regionCode, childLevel3Codes.size());
					break;

				case 1:
					// level 1의 하위 level 3들을 모두 조회해서 추가
					List<String> grandChildLevel3Codes = regionRepository.findLevel3CodesByGrandParentCode(regionCode);
					level3Codes.addAll(grandChildLevel3Codes);
					log.debug("level 1 지역 {} 에서 {} 개의 level 3 지역 확장", regionCode, grandChildLevel3Codes.size());
					break;

				default:
					log.warn("지원하지 않는 지역 레벨입니다: {} (level: {})", regionCode, region.getLevel());
			}
		}

		// 중복 제거
		List<String> uniqueLevel3Codes = level3Codes.stream().distinct().toList();
		log.debug("지역 코드 확장 완료: {} -> {} ({}개)", inputRegionCodes, uniqueLevel3Codes, uniqueLevel3Codes.size());

		return uniqueLevel3Codes;
	}

	public List<ProductResponseDTO> searchWeddingHall(List<String> regionCodes, Integer price,
		List<HallStyle> hallStyles, List<HallMeal> hallMeals, Integer capacity, Boolean hasParking) {

		log.info("웨딩홀 검색 시작 - regionCodes: {}, price: {}, hallStyles: {}, hallMeals: {}, capacity: {}, hasParking: {}",
			regionCodes, price, hallStyles, hallMeals, capacity, hasParking);

		long startTime = System.currentTimeMillis();

		try {
			// 유효성 검증: 음수값 체크
			if (price != null && price <= 0) {
				throw new BadRequestException("가격은 0보다 커야 합니다.");
			}
			if (capacity != null && capacity <= 0) {
				throw new BadRequestException("수용 인원은 1명 이상이어야 합니다.");
			}

			// 입력받은 지역 코드를 level 3 코드들로 확장
			List<String> expandedRegionCodes = expandToLevel3RegionCodes(regionCodes);

			List<VendorProductQueryRepository.VendorWithMinPrice> vendorsWithPrice =
				vendorProductQueryRepository.searchWeddingHallVendors(
					expandedRegionCodes, price, hallStyles, hallMeals, capacity, hasParking);

			List<ProductResponseDTO> results = vendorsWithPrice.stream()
				.map(vendorWithPrice -> convertToProductResponseDTO(
					vendorWithPrice.vendor, vendorWithPrice.minPrice != null ? vendorWithPrice.minPrice : 0L))
				.toList();

			long endTime = System.currentTimeMillis();
			log.info("[PERFORMANCE] 웨딩홀 검색 (QueryDSL) 완료 - {} 개 업체 결과, 소요시간: {}ms (확장된 지역: {}개)",
				results.size(), (endTime - startTime), expandedRegionCodes == null ? 0 : expandedRegionCodes.size());
			return results;
		} catch (Exception e) {
			long endTime = System.currentTimeMillis();
			log.error("웨딩홀 검색 실패 (QueryDSL) - regionCodes: {}, price: {}, 소요시간: {}ms",
				regionCodes, price, (endTime - startTime), e);
			throw e;
		}
	}

	/**
	 * 순수 JPA를 사용한 웨딩홀 검색 (성능 비교용)
	 */
	public List<ProductResponseDTO> searchWeddingHallWithJPA(List<String> regionCodes, Integer price,
		List<HallStyle> hallStyles, List<HallMeal> hallMeals, Integer capacity, Boolean hasParking) {

		log.info(
			"웨딩홀 검색 시작 (JPA) - regionCodes: {}, price: {}, hallStyles: {}, hallMeals: {}, capacity: {}, hasParking: {}",
			regionCodes, price, hallStyles, hallMeals, capacity, hasParking);

		try {
			// 유효성 검증: 음수값 체크
			if (price != null && price <= 0) {
				throw new BadRequestException("가격은 0보다 커야 합니다.");
			}
			if (capacity != null && capacity <= 0) {
				throw new BadRequestException("수용 인원은 1명 이상이어야 합니다.");
			}

			// 입력받은 지역 코드를 level 3 코드들로 확장
			List<String> expandedRegionCodes = expandToLevel3RegionCodes(regionCodes);

			List<VendorProductQueryRepository.VendorWithMinPrice> vendorsWithPrice =
				vendorProductQueryRepository.searchWeddingHallVendorsWithJPA(
					expandedRegionCodes, price, hallStyles, hallMeals, capacity, hasParking);

			List<ProductResponseDTO> results = vendorsWithPrice.stream()
				.map(vendorWithPrice -> convertToProductResponseDTO(
					vendorWithPrice.vendor, vendorWithPrice.minPrice != null ? vendorWithPrice.minPrice : 0L))
				.toList();

			log.info("웨딩홀 검색 성공 (JPA) - {} 개 업체 결과 반환 (확장된 지역: {}개)", results.size(),
				expandedRegionCodes == null ? 0 : expandedRegionCodes.size());
			return results;
		} catch (Exception e) {
			log.error("웨딩홀 검색 실패 (JPA) - regionCodes: {}, price: {}", regionCodes, price, e);
			throw e;
		}
	}

	public List<ProductResponseDTO> searchStudio(List<String> regionCodes, Integer price,
		List<StudioStyle> studioStyles,
		List<StudioSpecialShot> studioSpecialShots, Boolean iphoneSnap) {

		log.info("스튜디오 검색 시작 - regionCodes: {}, price: {}, studioStyles: {}, specialShots: {}, iphoneSnap: {}",
			regionCodes, price, studioStyles, studioSpecialShots, iphoneSnap);

		try {
			// 유효성 검증: 음수값 체크
			if (price != null && price <= 0) {
				throw new BadRequestException("가격은 0보다 커야 합니다.");
			}

			// 입력받은 지역 코드를 level 3 코드들로 확장
			List<String> expandedRegionCodes = expandToLevel3RegionCodes(regionCodes);

			List<VendorProductQueryRepository.VendorWithMinPrice> vendorsWithPrice =
				vendorProductQueryRepository.searchStudioVendors(
					expandedRegionCodes, price, studioStyles, studioSpecialShots, iphoneSnap);

			List<ProductResponseDTO> results = vendorsWithPrice.stream()
				.map(vendorWithPrice -> convertToProductResponseDTO(
					vendorWithPrice.vendor, vendorWithPrice.minPrice != null ? vendorWithPrice.minPrice : 0L))
				.toList();

			log.info("스튜디오 검색 성공 - {} 개 업체 결과 반환 (확장된 지역: {}개)", results.size(),
				expandedRegionCodes == null ? 0 : expandedRegionCodes.size());
			return results;
		} catch (Exception e) {
			log.error("스튜디오 검색 실패 - regionCodes: {}, price: {}", regionCodes, price, e);
			throw e;
		}
	}

	public List<ProductResponseDTO> searchMakeup(List<String> regionCodes, Integer price,
		List<MakeupStyle> makeupStyles,
		Boolean isStylistDesignationAvailable, Boolean hasPrivateRoom) {

		log.info("메이크업 검색 시작 - regionCodes: {}, price: {}, makeupStyles: {}, stylistDesignation: {}, privateRoom: {}",
			regionCodes, price, makeupStyles, isStylistDesignationAvailable, hasPrivateRoom);

		try {
			// 유효성 검증: 음수값 체크
			if (price != null && price <= 0) {
				throw new BadRequestException("가격은 0보다 커야 합니다.");
			}

			// 입력받은 지역 코드를 level 3 코드들로 확장
			List<String> expandedRegionCodes = expandToLevel3RegionCodes(regionCodes);

			List<VendorProductQueryRepository.VendorWithMinPrice> vendorsWithPrice =
				vendorProductQueryRepository.searchMakeupVendors(
					expandedRegionCodes, price, makeupStyles, isStylistDesignationAvailable, hasPrivateRoom);

			List<ProductResponseDTO> results = vendorsWithPrice.stream()
				.map(vendorWithPrice -> convertToProductResponseDTO(
					vendorWithPrice.vendor, vendorWithPrice.minPrice != null ? vendorWithPrice.minPrice : 0L))
				.toList();

			log.info("메이크업 검색 성공 - {} 개 업체 결과 반환 (확장된 지역: {}개)", results.size(),
				expandedRegionCodes == null ? 0 : expandedRegionCodes.size());
			return results;
		} catch (Exception e) {
			log.error("메이크업 검색 실패 - regionCodes: {}, price: {}", regionCodes, price, e);
			throw e;
		}
	}

	public List<ProductResponseDTO> searchDress(List<String> regionCodes, Integer price,
		List<DressStyle> dressStyles, List<DressOrigin> dressOrigins) {

		log.info("드레스 검색 시작 - regionCodes: {}, price: {}, dressStyles: {}, dressOrigins: {}",
			regionCodes, price, dressStyles, dressOrigins);

		try {
			// 유효성 검증: 음수값 체크
			if (price != null && price <= 0) {
				throw new BadRequestException("가격은 0보다 커야 합니다.");
			}

			// 입력받은 지역 코드를 level 3 코드들로 확장
			List<String> expandedRegionCodes = expandToLevel3RegionCodes(regionCodes);

			List<VendorProductQueryRepository.VendorWithMinPrice> vendorsWithPrice =
				vendorProductQueryRepository.searchDressVendors(
					expandedRegionCodes, price, dressStyles, dressOrigins);

			List<ProductResponseDTO> results = vendorsWithPrice.stream()
				.map(vendorWithPrice -> convertToProductResponseDTO(
					vendorWithPrice.vendor, vendorWithPrice.minPrice != null ? vendorWithPrice.minPrice : 0L))
				.toList();

			log.info("드레스 검색 성공 - {} 개 업체 결과 반환 (확장된 지역: {}개)", results.size(),
				expandedRegionCodes == null ? 0 : expandedRegionCodes.size());
			return results;
		} catch (Exception e) {
			log.error("드레스 검색 실패 - regionCodes: {}, price: {}", regionCodes, price, e);
			throw e;
		}
	}

	private ProductResponseDTO convertToProductResponseDTO(Vendor vendor, Long minPrice) {
        return ProductResponseDTO.builder()
                .basePrice(minPrice)
                .vendorId(vendor.getId())
                .vendorName(vendor.getName())
                .averageRating(vendor.getAverageRating())
                .reviewCount(vendor.getReviewCount())
                .logoMediaUrl(vendor.getLogoMedia() != null ?
                        s3Service.toCdnUrl(vendor.getLogoMedia().getMediaKey()) : null)
                .fullAddress(vendor.getFullAddress())
                .addressDetail(vendor.getAddressDetail())
                .latitude(vendor.getLatitude())
                .longitude(vendor.getLongitude())
                .build();
	}

	@Transactional(readOnly = true)
	public VendorAddressResponseDTO getVendorAddressByName(String name) {
		log.debug("업체명으로 주소 조회 시작 - vendorName: {}", name);

		Vendor vendor = vendorRepository.findFirstByName(name)
			.orElseThrow(() -> new NotFoundException("해당 이름의 업체를 찾을 수 없습니다: " + name));

		// 완전한 주소 생성 (기본주소 + 상세주소)
		String completeAddress = vendor.getFullAddress();
		if (vendor.getAddressDetail() != null && !vendor.getAddressDetail().trim().isEmpty()) {
			completeAddress += " " + vendor.getAddressDetail();
		}

		log.info("업체명으로 주소 조회 완료 - vendorName: {}, completeAddress: {}", name, completeAddress);

		return VendorAddressResponseDTO.builder()
			.vendorName(vendor.getName())
			.fullAddress(vendor.getFullAddress())
			.addressDetail(vendor.getAddressDetail())
			.completeAddress(completeAddress)
			.build();
	}
}
