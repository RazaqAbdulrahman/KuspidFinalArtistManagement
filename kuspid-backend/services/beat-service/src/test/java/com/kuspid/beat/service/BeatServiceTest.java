package com.kuspid.beat.service;

import com.kuspid.beat.model.Beat;
import com.kuspid.beat.repository.BeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BeatServiceTest {

    @Mock
    private BeatRepository repository;

    @Mock
    private StorageService storageService;

    @InjectMocks
    @Spy
    private BeatService beatService;

    private Beat testBeat;

    @BeforeEach
    void setUp() {
        testBeat = Beat.builder()
                .id(1L)
                .title("Test Beat")
                .artistId("artist-123")
                .s3Key("uploaded-file.mp3")
                .analysisStatus(Beat.AnalysisStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("Upload Beat - Should save and return immediately (Async flow)")
    void uploadBeat_ShouldSaveAndReturnInitially() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.mp3", "audio/mpeg", "data".getBytes());

        when(storageService.uploadFile(any())).thenReturn("uploaded-file.mp3");
        when(repository.save(any(Beat.class))).thenReturn(testBeat);

        // Mock the async method to do nothing during the unit test
        doNothing().when(beatService).processAudioAsync(anyLong(), any());

        Beat result = beatService.uploadBeat("Test Beat", "artist-123", file);

        assertThat(result).isNotNull();
        assertThat(result.getS3Key()).isEqualTo("uploaded-file.mp3");
        verify(storageService, times(1)).uploadFile(any());
        verify(repository, times(1)).save(any(Beat.class));
        verify(beatService, times(1)).processAudioAsync(anyLong(), any());
    }

    @Test
    @DisplayName("Get All Beats - Should return list")
    void getAllBeats_ShouldReturnList() {
        when(repository.findAll()).thenReturn(Arrays.asList(testBeat));

        List<Beat> result = beatService.getAllBeats();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Beat");
    }

    @Test
    @DisplayName("Delete Beat - Should remove from storage and DB")
    void deleteBeat_ShouldRemoveFromStorageAndDb() {
        when(repository.findById(1L)).thenReturn(Optional.of(testBeat));

        beatService.deleteBeat(1L);

        verify(storageService, times(1)).deleteFile("uploaded-file.mp3");
        verify(repository, times(1)).delete(testBeat);
    }
}
