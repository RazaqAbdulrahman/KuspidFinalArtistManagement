package com.kuspid.artist.service;

import com.kuspid.artist.model.Artist;
import com.kuspid.artist.model.Note;
import com.kuspid.artist.repository.ArtistRepository;
import com.kuspid.artist.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository repository;
    private final NoteRepository noteRepository;

    public Artist createArtist(Artist artist) {
        log.info("Creating new artist: {}", artist.getName());
        return repository.save(artist);
    }

    public List<Artist> getAllArtists() {
        return repository.findAll();
    }

    public Artist getArtistById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artist not found with id: " + id));
    }

    @Transactional
    public Artist updateArtist(Long id, Artist artistDetails) {
        Artist artist = getArtistById(id);

        if (artistDetails.getName() != null)
            artist.setName(artistDetails.getName());
        if (artistDetails.getEmail() != null)
            artist.setEmail(artistDetails.getEmail());
        if (artistDetails.getPhone() != null)
            artist.setPhone(artistDetails.getPhone());
        if (artistDetails.getGenre() != null)
            artist.setGenre(artistDetails.getGenre());
        if (artistDetails.getBio() != null)
            artist.setBio(artistDetails.getBio());
        if (artistDetails.getStatus() != null)
            artist.setStatus(artistDetails.getStatus());

        return repository.save(artist);
    }

    public void deleteArtist(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Artist not found with id: " + id);
        }
        repository.deleteById(id);
    }

    public Note addNote(Long artistId, String content) {
        Artist artist = getArtistById(artistId);
        Note note = Note.builder()
                .content(content)
                .artist(artist)
                .build();
        return noteRepository.save(note);
    }

    public List<Note> getNotesByArtistId(Long artistId) {
        Artist artist = getArtistById(artistId);
        return artist.getNotes();
    }
}
