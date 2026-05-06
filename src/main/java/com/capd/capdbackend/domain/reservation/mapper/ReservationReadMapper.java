package com.capd.capdbackend.domain.reservation.mapper;

import com.capd.capdbackend.domain.reservation.dto.response.ReservationReadResponse;
import com.capd.capdbackend.domain.reservation.entity.ReservationEntity;
import org.springframework.stereotype.Component;

@Component
public class ReservationReadMapper {

    public ReservationReadResponse toResponse(ReservationEntity reservation) {
        return ReservationReadResponse.builder()
                .reservationId(reservation.getReservationId())
                .doctorName(reservation.getDoctor().getUser().getUserName())
                .reservationDate(reservation.getReservationDate())
                .build();
    }
}
