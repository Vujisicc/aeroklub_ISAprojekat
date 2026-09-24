package com.aeroklub.service;

import com.aeroklub.dto.Dto.*;
import com.aeroklub.model.User;
import com.aeroklub.repository.UserRepository;
import com.aeroklub.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtTokenProvider tokens;

    @Transactional
    public void register(RegisterReq r) {
        if (users.existsByUsername(r.username()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
        if (users.existsByEmail(r.email()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        User u = new User();
        u.setUsername(r.username());
        u.setEmail(r.email());
        u.setPassword(encoder.encode(r.password()));
        u.setRole(r.role() == User.Role.PILOT ? User.Role.PILOT : User.Role.USER); // ADMIN can never be self-assigned
        users.save(u);
    }

    @Transactional
    public AuthRes login(LoginReq r) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(r.username(), r.password()));
        return issue(users.findByUsername(r.username()).orElseThrow());
    }

    /** Rotates the refresh token; a reused/stale token invalidates the whole session. */
    @Transactional(noRollbackFor = ResponseStatusException.class)
    public AuthRes refresh(String refreshToken) {
        Claims c;
        try { c = tokens.parse(refreshToken); }
        catch (JwtException | IllegalArgumentException e) { throw unauthorized(); }

        User u = users.findByUsername(c.getSubject()).orElseThrow(AuthService::unauthorized);
        if (!"refresh".equals(c.get("type", String.class)) || !Objects.equals(c.getId(), u.getRefreshTokenId())) {
            u.setRefreshTokenId(null);
            throw unauthorized();
        }
        return issue(u);
    }

    @Transactional
    public void logout(String username) {
        users.findByUsername(username).ifPresent(u -> u.setRefreshTokenId(null));
    }

    private AuthRes issue(User u) {
        String jti = UUID.randomUUID().toString();
        u.setRefreshTokenId(jti);
        return new AuthRes(tokens.createAccessToken(u.getUsername(), u.getRole().name()),
                tokens.createRefreshToken(u.getUsername(), jti), u.getUsername(), u.getRole().name());
    }

    private static ResponseStatusException unauthorized() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
    }
}