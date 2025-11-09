package com.wedit.backend.api.member.repository;

import com.wedit.backend.api.member.entity.Member;
import com.wedit.backend.api.member.entity.MemberDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberDeviceRepository extends JpaRepository<MemberDevice, Long> {

    @Query("SELECT md.fcmToken FROM MemberDevice md " +
            "WHERE md.member = :member AND md.isActive = true " +
            "ORDER BY md.updatedAt DESC " +
            "LIMIT 1")
    Optional<String> findActiveTokenByMember(@Param("member") Member member);

    Optional<MemberDevice> findByFcmToken(String fcmToken);
}
