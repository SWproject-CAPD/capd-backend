package com.capd.capdbackend.domain.capd.entity;

import com.capd.capdbackend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalTime;

@Entity
@Table(name = "capd_sessions")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CapdSessionEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long capdSessionId; // 세션 투석일지 고유번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "capd_id", nullable = false)
    private CapdCommonEntity capdCommon;

    @Column(nullable = false)
    private int sessionNumber; // 하루 중 몇 번째 교환인지 (1~5 사이 값)

    @Column(nullable = false)
    private LocalTime exchangeTime; // 해당 회차 교환을 시작한 시간

    @Column(nullable = false)
    private float drainVolume; // 배액량

    @Column(nullable = false)
    private float dialysateConcentration; // 포도당 농도 (1.5 , 2.5 , 4.5 중 선택)

    @Column(nullable = false)
    private float infusedFluidWeight; // 주입액 무게 (복강에 넣은 투석액의 양)

    @Column(nullable = false)
    private float ultrafiltration; // 초여과량 (제수량) : 배액량 - 주입량
}
