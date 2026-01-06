package com.kuspid.artist.controller;

import com.kuspid.artist.model.Artist;
import com.kuspid.artist.model.Note;
import com.kuspid.artist.service.ArtistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/artists")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService service;

    @PostMapping
    public ResponseEntity<Artist> createArtist(@RequestBody Artist artist) {
        return ResponseEntity.ok(service.createArtist(artist));
    }

    @GetMapping
    public ResponseEntity<List<Artist>> getAllArtists() {
        return ResponseEntity.ok(service.getAllArtists());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Artist> getArtistById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getArtistById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Artist> updateArtist(@PathVariable Long id, @RequestBody Artist artist) {
        return ResponseEntity.ok(service.updateArtist(id, artist));
    }

    @PostMapping("/{id}/notes")
    public ResponseEntity<Note> addNote(@PathVariable Long id, @RequestBody String content) {
        return ResponseEntity.ok(service.addNote(id, content));
    }
}
