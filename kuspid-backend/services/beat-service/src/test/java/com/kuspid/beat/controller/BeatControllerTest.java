package com.kuspid.beat.controller;

import com.kuspid.beat.model.Beat;
import com.kuspid.beat.service.BeatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BeatController.class)
class BeatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BeatService beatService;

    private Beat testBeat;

    @BeforeEach
    void setUp() {
        testBeat = Beat.builder()
                .id(1L)
                .title("Test Beat")
                .artistId("artist-123")
                .genre("Hip Hop")
                .bpm(140)
                .musicalKey("C")
                .s3Key("test-file.mp3")
                .fileName("test-file.mp3")
                .fileSize(1024L)
                .analysisStatus(Beat.AnalysisStatus.COMPLETED)
                .build();
    }

    @Test
    @DisplayName("GET /api/beats - Should return all beats")
    void getAllBeats_ShouldReturnList() throws Exception {
        List<Beat> beats = Arrays.asList(testBeat);
        when(beatService.getAllBeats()).thenReturn(beats);

        mockMvc.perform(get("/api/beats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Beat"))
                .andExpect(jsonPath("$[0].bpm").value(140));
    }

    @Test
    @DisplayName("GET /api/beats/{id} - Should return single beat")
    void getBeatById_ShouldReturnBeat() throws Exception {
        when(beatService.getBeatById(1L)).thenReturn(testBeat);

        mockMvc.perform(get("/api/beats/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.genre").value("Hip Hop"));
    }

    @Test
    @DisplayName("POST /api/beats - Should upload and return beat")
    void uploadBeat_ShouldReturnCreatedBeat() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.mp3", "audio/mpeg", "audio data".getBytes());

        when(beatService.uploadBeat(anyString(), anyString(), any())).thenReturn(testBeat);

        mockMvc.perform(multipart("/api/beats")
                .file(file)
                .param("title", "Test Beat")
                .param("artistId", "artist-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Beat"));
    }

    @Test
    @DisplayName("DELETE /api/beats/{id} - Should delete beat")
    void deleteBeat_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/beats/1"))
                .andExpect(status().isNoContent());
    }
}
