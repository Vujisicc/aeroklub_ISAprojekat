package com.aeroklub.controller;

import com.aeroklub.dto.Dto.*;
import com.aeroklub.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService auth;

    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody RegisterReq r) { auth.register(r); }

    @PostMapping("/login")
    public AuthRes login(@Valid @RequestBody LoginReq r) { return auth.login(r); }

    @PostMapping("/refresh")
    public AuthRes refresh(@Valid @RequestBody RefreshReq r) { return auth.refresh(r.refreshToken()); }

    @PostMapping("/logout") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(Authentication a) { auth.logout(a.getName()); }
}