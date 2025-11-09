package com.wedit.backend.api.member.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.wedit.backend.api.invitation.entity.Invitation;
import com.wedit.backend.api.member.jwt.entity.RefreshToken;
import com.wedit.backend.api.reservation.entity.Reservation;
import com.wedit.backend.api.review.entity.Review;
import com.wedit.backend.common.entity.BaseTimeEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id", callSuper = false)
public class Member extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 가입 이메일(유니크)
	@Column(nullable = false, unique = true, length = 100)
	private String email;

	// 비밀번호 (OAuth 사용시 nullable)
	@Column(nullable = true, length = 255)
	private String password;

	// 소셜 로그인 유니크 키(구글 ID 등)
	@Column(nullable = true, unique = true, length = 100)
	private String oauthId;

	// 회원명(본명)
	@Column(nullable = false, length = 10)
	private String name;

	// 전화번호
	// @Column(nullable = false, unique = true, length = 20)
	private String phoneNumber;

	// 생년월일
	// @Column(nullable = false)
	private LocalDate birthDate;

	// 결혼예정일
    @Column(nullable = true)
	private LocalDate weddingDate;

	// 사용자 권한 (USER, ADMIN)
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	@Enumerated(EnumType.STRING)
	private Type type;

	// Couple - Member, 1:1 Groom on Couple perspective
	@OneToOne(mappedBy = "groom")
	private Couple asGroom;

	// Couple - Member, 1:1 Bride on Couple perspective
	@OneToOne(mappedBy = "bride")
	private Couple asBride;

	// Member - RefreshToken, 1:N on Member perspective
	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<RefreshToken> refreshTokens = new ArrayList<>();

	// Member - Reservation, 1:N on Member perspective
	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<Reservation> reservations = new ArrayList<>();

	// 🎯 Member - Invitation, 1:1 관계로 다시 복원
	@OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
	private Invitation invitation;

	// Member - Review, 1:N on Member perspective
	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<Review> reviews = new ArrayList<>();

	public void changeEmail(String newEmail) {
		this.email = newEmail;
	}

	public void updateStatus(Type newStatus) {
		this.type = newStatus;
	}

	public void updateWeddingDate(LocalDate newDate) {
		this.weddingDate = newDate;
	}

	public void updatePhoneNumber(String newPhoneNumber) {
		this.phoneNumber = newPhoneNumber;
	}

	public void updateOauthId(String newOauthId) {
		this.oauthId = newOauthId;
	}

	// 리프레쉬 토큰 추가
	public void addRefreshToken(RefreshToken newRefreshToken) {
		this.refreshTokens.add(newRefreshToken);
	}

	// 리프레쉬 토큰 삭제
	public void removeRefreshToken(RefreshToken newRefreshToken) {
		this.refreshTokens.remove(newRefreshToken);
	}

	public void setAsGroom(Couple couple) {
		this.asGroom = couple;
	}

	public void setAsBride(Couple couple) {
		this.asBride = couple;
	}

	public Member update(String name) {
		this.name = name;
		return this;
	}

	// 현재 사용자가 커플인지 확인하는 메서드
	public Optional<Couple> getCouple() {
		if (asGroom != null) {
            return Optional.of(asGroom);
        }
        return Optional.ofNullable(asBride);
	}

    // 현재 사용자의 파트너를 반환하는 메서드
    public Optional<Member> getPartner() {
        return getCouple().map(couple -> couple.getPartner(this));
    }
}
