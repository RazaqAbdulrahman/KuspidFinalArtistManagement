package com.kuspid.artist.service;

import com.kuspid.artist.model.Artist;
import com.kuspid.artist.model.Note;
import com.kuspid.artist.repository.ArtistRepository;
import com.kuspid.artist.repository.NoteRepository;
import com.kuspid.beat.repository.BeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository repository;
    private final NoteRepository noteRepository;
    private final BeatRepository beatRepository;

    public Artist createArtist(Artist artist) {
        return repository.save(artist);
    }

    public List<Artist> getAllArtists() {
        return repository.findAll().stream()
                .peek(this::enrichArtist)
                .toList();
    }

    public Artist getArtistById(Long id) {
        Artist artist = repository.findById(id).orElseThrow();
        enrichArtist(artist);
        return artist;
    }

    private void enrichArtist(Artist artist) {
        artist.setBeatCount(beatRepository.countByArtistId(artist.getId().toString()));
    }

    public Artist updateArtist(Long id, Artist artistDetails) {
        Artist artist = getArtistById(id);
        artist.setName(artistDetails.getName());
        artist.setEmail(artistDetails.getEmail());
        artist.setPhone(artistDetails.getPhone());
        artist.setGenre(artistDetails.getGenre());
        artist.setBio(artistDetails.getBio());
        artist.setImageUrl(artistDetails.getImageUrl());
        artist.setStatus(artistDetails.getStatus());
        return repository.save(artist);
    }

    public Note addNote(Long artistId, String content) {
        Artist artist = getArtistById(artistId);
        Note note = Note.builder()
                .content(content)
                .artist(artist)
                .build();
        return noteRepository.save(note);
    }
}
