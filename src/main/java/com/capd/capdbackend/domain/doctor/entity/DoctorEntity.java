package com.capd.capdbackend.domain.doctor.entity;

import com.capd.capdbackend.domain.user.entity.UserEntity;
import com.capd.capdbackend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "doctors")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class DoctorEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long doctorId; // 의사 고유번호

    // UserEntity에서 이름, 이메일, 비밀번호, 전화번호, 권한을 받을 수 있음 (user.get()...)
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user; // UserEntity의 userId와 1대1 매핑

    @Column(nullable = false, length = 20)
    private String licenseId; // 의사 면허번호
}
