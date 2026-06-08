package com.capd.capdbackend.domain.reservation.mapper;

import com.capd.capdbackend.domain.reservation.dto.response.ReservationDoctorReadResponse;
import com.capd.capdbackend.domain.reservation.entity.ReservationEntity;
import org.springframework.stereotype.Component;

@Component
public class ReservationDoctorReadMapper {

    public ReservationDoctorReadResponse toResponse(ReservationEntity reservation) {
        return ReservationDoctorReadResponse.builder()
                .reservationId(reservation.getReservationId())
                .patientId(reservation.getPatient().getPatientId())
                .patientName(reservation.getPatient().getUser().getUserName())
                .phone(reservation.getPatient().getUser().getPhone())
                .reservationDate(reservation.getReservationDate())
                .type(reservation.getType())
                .build();
    }
}
