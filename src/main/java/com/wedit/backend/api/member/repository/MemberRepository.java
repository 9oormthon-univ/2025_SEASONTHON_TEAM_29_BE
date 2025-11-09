package com.wedit.backend.api.member.repository;

import com.wedit.backend.api.member.entity.Member;
import com.wedit.backend.api.member.entity.Role;
import com.wedit.backend.api.member.entity.Type;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, String> {

    // id로 Member 조회
    Optional<Member> findById(Long id);

    // 이메일로 회원 조회
    Optional<Member> findByEmail(String email);

    // 전화번호로 회원 조회
    Optional<Member> findByPhoneNumber(String phoneNumber);

    // OAuth ID로 회원 조회
    Optional<Member> findByOauthId(String oauthId);

    // 이메일 존재 여부 확인
    boolean existsByEmail(String email);

    // 전화번호 존재 여부 확인
    boolean existsByPhoneNumber(String phoneNumber);

    // 특정 상태의 회원 조회
    Optional<Member> findByIdAndType(Long id, Type status);

    // 권한에 따른 전체 사용자 조회
    Page<Member> findByRole(Role role, Pageable pageable);

    @Query("SELECT m FROM Member m " +
            "LEFT JOIN FETCH m.asGroom g " +  // 내가 신랑인 Couple 정보
            "LEFT JOIN FETCH g.bride " +      // 그 Couple의 신부 정보
            "LEFT JOIN FETCH m.asBride b " +  // 내가 신부인 Couple 정보
            "LEFT JOIN FETCH b.groom " +      // 그 Couple의 신랑 정보
            "WHERE m.id = :memberId")
    Optional<Member> findMemberWithCoupleInfoById(@Param("memberId") Long memberId);
}
