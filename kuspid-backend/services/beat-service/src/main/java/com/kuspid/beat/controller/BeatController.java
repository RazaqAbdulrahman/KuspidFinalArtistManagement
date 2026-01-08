package com.kuspid.beat.controller;

import com.kuspid.beat.model.Beat;
import com.kuspid.beat.service.BeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/beats")
@RequiredArgsConstructor
public class BeatController {

    private final BeatService service;

    @PostMapping
    public ResponseEntity<Beat> uploadBeat(
            @RequestParam("title") String title,
            @RequestParam("artistId") String artistId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(service.uploadBeat(title, artistId, file));
    }

    @GetMapping
    public ResponseEntity<List<Beat>> getAllBeats() {
        return ResponseEntity.ok(service.getAllBeats());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Beat> getBeatById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getBeatById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Beat> updateBeat(@PathVariable Long id, @RequestBody Beat beat) {
        return ResponseEntity.ok(service.updateBeat(id, beat));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBeat(@PathVariable Long id) {
        service.deleteBeat(id);
        return ResponseEntity.noContent().build();
    }
}
