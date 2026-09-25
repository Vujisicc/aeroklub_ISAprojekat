package com.aeroklub.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.aeroklub.dto.FlightDto.*;
import com.aeroklub.dto.FlightDto.FlightReq;
import com.aeroklub.dto.FlightDto.FlightRes;
import com.aeroklub.model.Flight;
import com.aeroklub.model.Glider;
import com.aeroklub.model.User;
import com.aeroklub.repository.FlightRepository;
import com.aeroklub.repository.GliderRepository;
import com.aeroklub.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class FlightService {
    private final FlightRepository flights;
    private final GliderRepository gliders;
    private final UserRepository users;

    @Transactional(readOnly = true)
    public List<FlightRes> findAll() {
        return flights.findAll().stream().map(FlightService::toRes)
                .sorted(Comparator.comparing(FlightRes::date).reversed()).toList();
    }

    public FlightRes create(FlightReq r, String username) {
        User u = users.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unknown user"));
        Glider g = gliders.findById(r.gliderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Glider not found"));

        Flight f = new Flight();
        f.setDate(r.date());
        f.setDurationMinutes(r.durationMinutes());
        f.setLaunchType(r.launchType());
        f.setUser(u);
        f.setGlider(g);
        return toRes(flights.save(f));
    }

    private static FlightRes toRes(Flight f) {
        return new FlightRes(f.getId(), f.getDate(), f.getDurationMinutes(), f.getLaunchType(),
                f.getGlider().getId(), f.getGlider().getRegistrationMarks(), f.getUser().getUsername());
    }
}