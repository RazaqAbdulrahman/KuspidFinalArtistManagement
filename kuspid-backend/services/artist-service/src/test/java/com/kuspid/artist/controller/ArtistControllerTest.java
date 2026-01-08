package com.kuspid.artist.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuspid.artist.model.Artist;
import com.kuspid.artist.model.Note;
import com.kuspid.artist.service.ArtistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ArtistController.class)
class ArtistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ArtistService artistService;

    private Artist testArtist;

    @BeforeEach
    void setUp() {
        testArtist = Artist.builder()
                .id(1L)
                .name("Test Artist")
                .email("artist@kuspid.com")
                .phone("1234567890")
                .genre("Hip Hop")
                .bio("Test bio")
                .status(Artist.Status.LEAD)
                .build();
    }

    @Test
    @DisplayName("POST /api/artists - Should create artist")
    void createArtist_ShouldReturnCreatedArtist() throws Exception {
        when(artistService.createArtist(any(Artist.class))).thenReturn(testArtist);

        mockMvc.perform(post("/api/artists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testArtist)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Artist"))
                .andExpect(jsonPath("$.status").value("LEAD"));
    }

    @Test
    @DisplayName("GET /api/artists - Should return all artists")
    void getAllArtists_ShouldReturnList() throws Exception {
        List<Artist> artists = Arrays.asList(testArtist);
        when(artistService.getAllArtists()).thenReturn(artists);

        mockMvc.perform(get("/api/artists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("artist@kuspid.com"));
    }

    @Test
    @DisplayName("GET /api/artists/{id} - Should return single artist")
    void getArtistById_ShouldReturnArtist() throws Exception {
        when(artistService.getArtistById(1L)).thenReturn(testArtist);

        mockMvc.perform(get("/api/artists/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.genre").value("Hip Hop"));
    }

    @Test
    @DisplayName("PUT /api/artists/{id} - Should update artist")
    void updateArtist_ShouldReturnUpdatedArtist() throws Exception {
        testArtist.setStatus(Artist.Status.COLLABORATING);
        when(artistService.updateArtist(eq(1L), any(Artist.class))).thenReturn(testArtist);

        mockMvc.perform(put("/api/artists/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testArtist)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COLLABORATING"));
    }

    @Test
    @DisplayName("POST /api/artists/{id}/notes - Should add note")
    void addNote_ShouldReturnCreatedNote() throws Exception {
        Note note = Note.builder()
                .id(1L)
                .content("Test note content")
                .build();
        when(artistService.addNote(eq(1L), anyString())).thenReturn(note);

        mockMvc.perform(post("/api/artists/1/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("Test note content"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Test note content"));
    }
}
