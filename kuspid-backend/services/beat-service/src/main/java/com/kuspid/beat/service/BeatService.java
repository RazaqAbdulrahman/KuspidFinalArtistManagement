package com.kuspid.beat.service;

import com.kuspid.beat.model.Beat;
import com.kuspid.beat.repository.BeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BeatService {

    private final BeatRepository repository;
    private final StorageService storageService;

    public Beat uploadBeat(String title, String artistId, MultipartFile file) {
        log.info("Uploading beat '{}' for artist {}", title, artistId);
        String cloudKey = storageService.uploadFile(file);

        Beat beat = Beat.builder()
                .title(title)
                .artistId(artistId)
                .assetId(cloudKey)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .analysisStatus(Beat.AnalysisStatus.PROCESSING)
                .build();

        beat = repository.save(beat);

        // Process audio in background (Waveform + AI Analysis)
        processAudioAsync(beat.getId(), file);

        return beat;
    }

    @Async
    public void processAudioAsync(Long beatId, MultipartFile file) {
        Beat beat = repository.findById(beatId).orElse(null);
        if (beat == null)
            return;

        try {
            // 1. Generate Waveform using FFmpeg
            String waveformKey = generateWaveform(file, beat.getAssetId());
            if (waveformKey != null) {
                beat.setWaveformKey(waveformKey);
            } else {
                log.warn("Waveform generation returned null for beat {}", beatId);
            }

            // 2. Future: AI Analysis (BPM, Key, Genre) placeholder
            beat.setAnalysisStatus(Beat.AnalysisStatus.COMPLETED);
            repository.save(beat);
        } catch (Exception e) {
            log.error("Critical error processing audio for beat {}: {}", beatId, e.getMessage());
            beat.setAnalysisStatus(Beat.AnalysisStatus.FAILED);
            repository.save(beat);
        }
    }

    private String generateWaveform(MultipartFile file, String cloudKey) {
        File tempInput = null;
        File tempOutput = null;
        try {
            tempInput = Files.createTempFile("wave-in-", ".tmp").toFile();
            tempOutput = Files.createTempFile("wave-out-", ".png").toFile();
            file.transferTo(tempInput);

            // FFmpeg command to generate waveform PNG
            // showwavespic: color=white, dimensions 800x200
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg", "-i", tempInput.getAbsolutePath(),
                    "-filter_complex", "showwavespic=s=800x200:colors=white",
                    "-vframes", "1",
                    tempOutput.getAbsolutePath());

            pb.redirectErrorStream(true); // Merge stderr into stdout to see logs if needed
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                String waveformCloudKey = cloudKey + ".waveform.png";
                return storageService.uploadFile(tempOutput, waveformCloudKey);
            } else {
                log.error("FFmpeg exited with error code: {}", exitCode);
            }
        } catch (Exception e) {
            log.error("Failed to generate waveform: {}", e.getMessage());
        } finally {
            if (tempInput != null && tempInput.exists())
                tempInput.delete();
            if (tempOutput != null && tempOutput.exists())
                tempOutput.delete();
        }
        return null; // Return null on failure instead of crashing
    }

    public List<Beat> getAllBeats() {
        return repository.findAll().stream()
                .map(this::enrichBeatUrl)
                .toList();
    }

    public Beat getBeatById(Long id) {
        return repository.findById(id)
                .map(this::enrichBeatUrl)
                .orElseThrow(() -> new RuntimeException("Beat not found with id: " + id));
    }

    // Helper to generate CDN URLs for the client
    private Beat enrichBeatUrl(Beat beat) {
        if (beat.getAssetId() != null) {
            beat.setBeatUrl(storageService.getAssetUrl(beat.getAssetId()));
        }
        if (beat.getWaveformKey() != null) {
            beat.setWaveformUrl(storageService.getAssetUrl(beat.getWaveformKey()));
        }
        return beat;
    }

    @Transactional
    public Beat updateBeat(Long id, Beat beatDetails) {
        Beat beat = getBeatById(id);
        if (beatDetails.getTitle() != null)
            beat.setTitle(beatDetails.getTitle());
        if (beatDetails.getGenre() != null)
            beat.setGenre(beatDetails.getGenre());
        if (beatDetails.getBpm() != null)
            beat.setBpm(beatDetails.getBpm());
        if (beatDetails.getMusicalKey() != null)
            beat.setMusicalKey(beatDetails.getMusicalKey());

        return enrichBeatUrl(repository.save(beat));
    }

    public void deleteBeat(Long id) {
        Beat beat = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Beat not found with id: " + id));

        try {
            storageService.deleteFile(beat.getAssetId());
            if (beat.getWaveformKey() != null) {
                storageService.deleteFile(beat.getWaveformKey());
            }
        } catch (Exception e) {
            log.warn("Failed to delete files from cloud for beat {}: {}", id, e.getMessage());
            // Continue to delete metadata
        }

        repository.delete(beat);
    }
}
