package com.kuspid.beat.service;

import com.kuspid.beat.client.AiClient;
import com.kuspid.beat.model.Beat;
import com.kuspid.beat.repository.BeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
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
    private final AiClient aiClient;

    public Beat uploadBeat(String title, String artistId, MultipartFile file) {
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
            beat.setWaveformKey(waveformKey);
            repository.save(beat);

            // 2. Trigger AI analysis
            try {
                var analysis = aiClient.analyzeBeat(beat.getAssetId());
                if (!"DEGRADED".equals(analysis.getStatus())) {
                    beat.setBpm(analysis.getBpm());
                    beat.setMusicalKey(analysis.getKey());
                    beat.setGenre(analysis.getGenre());
                    beat.setAnalysisStatus(Beat.AnalysisStatus.COMPLETED);
                } else {
                    log.warn("AI Analysis degraded for beat {}", beatId);
                    beat.setAnalysisStatus(Beat.AnalysisStatus.COMPLETED); // Treat as completed but without AI data
                }
            } catch (Exception aiEx) {
                log.error("AI Analysis failed (non-critical): {}", aiEx.getMessage());
                beat.setAnalysisStatus(Beat.AnalysisStatus.COMPLETED); // Still mark accessible, just no AI tags
            }
        } catch (Exception e) {
            log.error("Critical error processing audio for beat {}: {}", beatId, e.getMessage());
            beat.setAnalysisStatus(Beat.AnalysisStatus.FAILED);
        }
        repository.save(beat);
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

            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                String waveformCloudKey = cloudKey + ".waveform.png";
                return storageService.uploadFile(tempOutput, waveformCloudKey);
            }
        } catch (Exception e) {
            log.error("Failed to generate waveform: {}", e.getMessage());
        } finally {
            if (tempInput != null)
                tempInput.delete();
            if (tempOutput != null)
                tempOutput.delete();
        }
        return null;
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

    private Beat enrichBeatUrl(Beat beat) {
        if (beat.getAssetId() != null) {
            beat.setBeatUrl(storageService.getAssetUrl(beat.getAssetId()));
        }
        if (beat.getWaveformKey() != null) {
            beat.setWaveformUrl(storageService.getAssetUrl(beat.getWaveformKey()));
        }
        return beat;
    }

    public void deleteBeat(Long id) {
        Beat beat = getBeatById(id);
        storageService.deleteAsset(beat.getAssetId());
        if (beat.getWaveformKey() != null) {
            storageService.deleteAsset(beat.getWaveformKey());
        }
        repository.delete(beat);
    }
}
