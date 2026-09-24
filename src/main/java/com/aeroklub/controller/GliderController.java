package com.aeroklub.controller;

import com.aeroklub.dto.Dto.GliderReq;
import com.aeroklub.model.Glider;
import com.aeroklub.service.GliderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/gliders")
@RequiredArgsConstructor
public class GliderController {
    private final GliderService service;

    @GetMapping
    public List<Glider> all() { return service.findAll(); }

    @GetMapping("/{id}")
    public Glider one(@PathVariable Long id) { return service.findById(id); }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','PILOT')")
    public Glider create(@Valid @RequestBody GliderReq r) { return service.create(r); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Glider update(@PathVariable Long id, @Valid @RequestBody GliderReq r) { return service.update(id, r); }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) { service.delete(id); }
}