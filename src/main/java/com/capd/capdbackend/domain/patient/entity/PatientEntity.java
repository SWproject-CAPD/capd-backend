package com.capd.capdbackend.domain.patient.entity;

import com.capd.capdbackend.domain.doctor.entity.DoctorEntity;
import com.capd.capdbackend.domain.user.entity.UserEntity;
import com.capd.capdbackend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "patients")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PatientEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long patientId; // 환자 고유번호

    // UserEntity에서 이름, 이메일, 비밀번호, 전화번호, 권한을 받을 수 있음 (user.get()...)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user; // UserEntity의 userId와 1대1 매핑

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = true)
    private DoctorEntity doctor; // 담당 의사

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Sex sex; // 성별

    @Column(nullable = false)
    private LocalDate birthDate; // 환자 나이

    public void assignDoctor(DoctorEntity doctor) {
        this.doctor = doctor;
    }
}
