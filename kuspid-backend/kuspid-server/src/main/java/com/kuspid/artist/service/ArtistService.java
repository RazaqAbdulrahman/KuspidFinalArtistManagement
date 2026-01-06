package com.kuspid.artist.service;

import com.kuspid.artist.model.Artist;
import com.kuspid.artist.model.Note;
import com.kuspid.artist.repository.ArtistRepository;
import com.kuspid.artist.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository repository;
    private final NoteRepository noteRepository;

    public Artist createArtist(Artist artist) {
        return repository.save(artist);
    }

    public List<Artist> getAllArtists() {
        return repository.findAll();
    }

    public Artist getArtistById(Long id) {
        return repository.findById(id).orElseThrow();
    }

    public Artist updateArtist(Long id, Artist artistDetails) {
        Artist artist = getArtistById(id);
        artist.setName(artistDetails.getName());
        artist.setEmail(artistDetails.getEmail());
        artist.setPhone(artistDetails.getPhone());
        artist.setGenre(artistDetails.getGenre());
        artist.setBio(artistDetails.getBio());
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
