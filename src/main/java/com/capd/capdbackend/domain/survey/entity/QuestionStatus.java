package com.capd.capdbackend.domain.survey.entity;

public enum QuestionStatus {
    PENDING, // AI 생성 후 대기 중
    APPROVED, // 의사 승인
    REJECTED // 의사 거절
}
