package com.aeroklub.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.aeroklub.dto.Dto.GliderReq;
import com.aeroklub.model.Glider;
import com.aeroklub.repository.GliderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class GliderService {
    private final GliderRepository repo;

    @Transactional(readOnly = true)
    public List<Glider> findAll() { return repo.findAll(); }

    @Transactional(readOnly = true)
    public Glider findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Glider not found"));
    }

    public Glider create(GliderReq r) {
        String reg = norm(r.registrationMarks());
        if (repo.existsByRegistrationMarks(reg))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Registration marks already exist");
        Glider g = new Glider();
        apply(g, r, reg);
        return repo.save(g);
    }

    public Glider update(Long id, GliderReq r) {
        Glider g = findById(id);
        String reg = norm(r.registrationMarks());
        if (!g.getRegistrationMarks().equals(reg) && repo.existsByRegistrationMarks(reg))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Registration marks already exist");
        apply(g, r, reg);
        return g; // managed entity, flushed on commit
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Glider not found");
        repo.deleteById(id); // FK violation (existing flights) -> 409 via GlobalExceptionHandler
    }

    private static String norm(String s) { return s.trim().toUpperCase(); }

    private static void apply(Glider g, GliderReq r, String reg) {
        g.setRegistrationMarks(reg);
        g.setModel(r.model().trim());
        g.setYear(r.year());
        g.setStatus(r.status());
    }
}