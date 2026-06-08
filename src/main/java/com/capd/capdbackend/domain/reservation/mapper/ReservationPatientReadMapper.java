package com.capd.capdbackend.domain.reservation.mapper;

import com.capd.capdbackend.domain.reservation.dto.response.ReservationPatientReadResponse;
import com.capd.capdbackend.domain.reservation.entity.ReservationEntity;
import org.springframework.stereotype.Component;

@Component
public class ReservationPatientReadMapper {

    public ReservationPatientReadResponse toResponse(ReservationEntity reservation) {
        return ReservationPatientReadResponse.builder()
                .reservationId(reservation.getReservationId())
                .doctorName(reservation.getDoctor().getUser().getUserName())
                .reservationDate(reservation.getReservationDate())
                .type(reservation.getType())
                .build();
    }
}
