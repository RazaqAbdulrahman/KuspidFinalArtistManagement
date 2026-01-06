package com.kuspid.artist.service;

import com.kuspid.artist.model.Artist;
import com.kuspid.artist.model.Note;
import com.kuspid.artist.repository.ArtistRepository;
import com.kuspid.artist.repository.NoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArtistServiceTest {

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private NoteRepository noteRepository;

    @InjectMocks
    private ArtistService artistService;

    private Artist testArtist;

    @BeforeEach
    void setUp() {
        testArtist = Artist.builder()
                .id(1L)
                .name("Test Artist")
                .email("artist@test.com")
                .status(Artist.Status.LEAD)
                .build();
    }

    @Test
    @DisplayName("Create Artist - Should save and return artist")
    void createArtist_ShouldSaveAndReturn() {
        when(artistRepository.save(any(Artist.class))).thenReturn(testArtist);

        Artist result = artistService.createArtist(testArtist);

        assertThat(result.getName()).isEqualTo("Test Artist");
        verify(artistRepository, times(1)).save(any(Artist.class));
    }

    @Test
    @DisplayName("Get All Artists - Should return list")
    void getAllArtists_ShouldReturnList() {
        when(artistRepository.findAll()).thenReturn(Arrays.asList(testArtist));

        List<Artist> result = artistService.getAllArtists();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Update Artist - Should update fields")
    void updateArtist_ShouldUpdateFields() {
        Artist updated = Artist.builder()
                .name("Updated Name")
                .email("updated@test.com")
                .status(Artist.Status.COLLABORATING)
                .build();

        when(artistRepository.findById(1L)).thenReturn(Optional.of(testArtist));
        when(artistRepository.save(any(Artist.class))).thenReturn(testArtist);

        Artist result = artistService.updateArtist(1L, updated);

        assertThat(result).isNotNull();
        verify(artistRepository, times(1)).save(any(Artist.class));
    }

    @Test
    @DisplayName("Add Note - Should create note for artist")
    void addNote_ShouldCreateNoteForArtist() {
        Note note = Note.builder()
                .id(1L)
                .content("Test note")
                .artist(testArtist)
                .build();

        when(artistRepository.findById(1L)).thenReturn(Optional.of(testArtist));
        when(noteRepository.save(any(Note.class))).thenReturn(note);

        Note result = artistService.addNote(1L, "Test note");

        assertThat(result.getContent()).isEqualTo("Test note");
        verify(noteRepository, times(1)).save(any(Note.class));
    }
}
