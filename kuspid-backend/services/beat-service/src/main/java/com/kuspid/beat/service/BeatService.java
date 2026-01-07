package com.kuspid.beat.service;

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

    public Beat uploadBeat(String title, String artistId, MultipartFile file) {
        String s3Key = storageService.uploadFile(file);

        Beat beat = Beat.builder()
                .title(title)
                .artistId(artistId)
                .s3Key(s3Key)
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
            String waveformKey = generateWaveform(file, beat.getS3Key());
            beat.setWaveformKey(waveformKey);
            repository.save(beat);

            // 2. No AI Analysis
            beat.setAnalysisStatus(Beat.AnalysisStatus.COMPLETED);
        } catch (Exception e) {
            log.error("Critical error processing audio for beat {}: {}", beatId, e.getMessage());
            beat.setAnalysisStatus(Beat.AnalysisStatus.FAILED);
        }
        repository.save(beat);
    }

    private String generateWaveform(MultipartFile file, String s3Key) {
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
                String waveformS3Key = s3Key + ".waveform.png";
                return storageService.uploadFile(tempOutput, waveformS3Key);
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
        return repository.findAll();
    }

    public Beat getBeatById(Long id) {
        return repository.findById(id).orElseThrow();
    }

    public void deleteBeat(Long id) {
        Beat beat = getBeatById(id);
        storageService.deleteFile(beat.getS3Key());
        if (beat.getWaveformKey() != null) {
            storageService.deleteFile(beat.getWaveformKey());
        }
        repository.delete(beat);
    }
}
