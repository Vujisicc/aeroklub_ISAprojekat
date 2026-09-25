package com.aeroklub.dto;

import java.time.LocalDate;

import com.aeroklub.model.Flight;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public final class FlightDto {
    private FlightDto() {}

    public record FlightReq(@NotNull LocalDate date,
                            @Min(1) @Max(1440) int durationMinutes,
                            @NotNull Flight.LaunchType launchType,
                            @NotNull Long gliderId) {}

    public record FlightRes(Long id, LocalDate date, int durationMinutes, Flight.LaunchType launchType,
                            Long gliderId, String registrationMarks, String username) {}
}