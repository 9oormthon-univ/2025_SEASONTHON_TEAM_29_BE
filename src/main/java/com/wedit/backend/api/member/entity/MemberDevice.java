package com.wedit.backend.api.member.entity;

import com.wedit.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "member_device", indexes = {
        @Index(name = "idx_member_device_member_id", columnList = "member_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberDevice extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_device_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, unique = true)
    private String fcmToken;

    @Column(nullable = false)
    @ColumnDefault("true")
    private boolean isActive = true;

    @Builder
    public MemberDevice(Member member, String fcmToken) {
        this.member = member;
        this.fcmToken = fcmToken;
    }

    public void updateToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public void deactivate() {
        this.isActive = false;
    }
}
