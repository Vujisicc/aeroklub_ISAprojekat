package com.aeroklub.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.aeroklub.dto.FlightDto.*;
import com.aeroklub.dto.FlightDto.FlightReq;
import com.aeroklub.dto.FlightDto.FlightRes;
import com.aeroklub.service.FlightService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {
    private final FlightService service;

    @GetMapping
    public List<FlightRes> all() { return service.findAll(); }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public FlightRes create(@Valid @RequestBody FlightReq r, Authentication auth) {
        return service.create(r, auth.getName());
    }
}