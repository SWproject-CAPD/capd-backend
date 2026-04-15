package com.capd.capdbackend.domain.user.entity;

import com.capd.capdbackend.global.common.BaseTimeEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId; // 사용자 고유번호

    @Column(length = 100)
    private String licenseId; // 의사 면허번호

    @Column(nullable = false, length = 300, unique = true)
    private String email; // 사용자 이메일

    @Column(nullable = false, length = 300)
    private String password; // 사용자 비밀번호

    @Column(nullable = false, length = 50)
    private String name; // 사용자 이름

    @Column(nullable = false)
    private LocalDate birthdate; // 사용자 생년월일

    @Column(nullable = false, length = 100, unique = true)
    private String phone; // 사용자 전화번호

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender; // 사용자 성별

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role; // 환자 or 의사

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private UserEntity doctor; // 담당 의사 엔티티 참조

    @JsonIgnore
    @Column(name = "refresh_token")
    private String refreshToken; // 토큰 저장 필드

    // refreshToken 업데이트 메서드
    public void createRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // 로그아웃시 refreshToken Null로 변경
    public void expireRefreshToken() {
        this.refreshToken = null;
    }
}
