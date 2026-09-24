package com.aeroklub.dto;

import com.aeroklub.model.Glider;
import com.aeroklub.model.User;
import jakarta.validation.constraints.*;

public final class Dto {
    private Dto() {}

    public record RegisterReq(@NotBlank @Size(min = 3, max = 50) String username,
                              @NotBlank @Size(min = 6, max = 100) String password,
                              @NotBlank @Email String email,
                              User.Role role) {}

    public record LoginReq(@NotBlank String username, @NotBlank String password) {}

    public record RefreshReq(@NotBlank String refreshToken) {}

    public record AuthRes(String accessToken, String refreshToken, String username, String role) {}

    public record GliderReq(@NotBlank @Size(max = 20) String registrationMarks,
                            @NotBlank String model,
                            @Min(1900) @Max(2100) int year,
                            @NotNull Glider.Status status) {}
}