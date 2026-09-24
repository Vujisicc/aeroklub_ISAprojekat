package com.aeroklub.repository;

import com.aeroklub.model.Glider;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GliderRepository extends JpaRepository<Glider, Long> {
    boolean existsByRegistrationMarks(String registrationMarks);
}