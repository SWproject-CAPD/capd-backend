package com.capd.capdbackend.domain.reservation.mapper;

import com.capd.capdbackend.domain.doctor.entity.DoctorEntity;
import com.capd.capdbackend.domain.patient.entity.PatientEntity;
import com.capd.capdbackend.domain.reservation.dto.request.ReservationCreateRequest;
import com.capd.capdbackend.domain.reservation.dto.response.ReservationCreateResponse;
import com.capd.capdbackend.domain.reservation.entity.ReservationEntity;
import org.springframework.stereotype.Component;

@Component
public class ReservationCreateMapper {

    // dto -> entity
    public ReservationEntity toEntity(ReservationCreateRequest request, DoctorEntity doctor, PatientEntity patient) {
        return ReservationEntity.builder()
                .doctor(doctor)
                .patient(patient)
                .reservationDate(request.getReservationDate())
                .type(request.getType())
                .build();
    }

    // entity -> dto
    public ReservationCreateResponse toResponse(ReservationEntity reservation) {
        return ReservationCreateResponse.builder()
                .reservationId(reservation.getReservationId())
                .patientId(reservation.getPatient().getPatientId())
                .patientName(reservation.getPatient().getUser().getUserName())
                .phone(reservation.getPatient().getUser().getPhone())
                .doctorId(reservation.getDoctor().getDoctorId())
                .doctorName(reservation.getDoctor().getUser().getUserName())
                .reservationDate(reservation.getReservationDate())
                .type(reservation.getType())
                .build();
    }
}
