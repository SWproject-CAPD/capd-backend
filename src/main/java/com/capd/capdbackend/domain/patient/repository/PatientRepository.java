package com.capd.capdbackend.domain.patient.repository;

import com.capd.capdbackend.domain.patient.entity.PatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<PatientEntity, Long> {
}
