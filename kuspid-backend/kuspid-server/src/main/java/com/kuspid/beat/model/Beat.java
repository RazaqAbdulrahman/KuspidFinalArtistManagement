package com.kuspid.beat.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "beats")
public class Beat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String artistId;
    @Transient
    private String artistName;
    private String genre;
    private Integer bpm;
    private String musicalKey;
    private String assetId;
    private String waveformKey;
    private Long plays = 0L;
    private String duration;

    @Transient
    private String beatUrl;

    @Transient
    private String fileUrl;

    @Transient
    private String waveformUrl;

    private String fileName;
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    private AnalysisStatus analysisStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum AnalysisStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }
}
