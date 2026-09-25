package com.aeroklub.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aeroklub.model.Flight;

public interface FlightRepository extends JpaRepository<Flight, Long> {}