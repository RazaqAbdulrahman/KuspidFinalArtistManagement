package com.kuspid.beat.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * Production-hardened Cloudinary implementation.
 * Leveraging Netflix-style error propagation and logging.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService implements AssetStorageProvider {

    private final Cloudinary cloudinary;

    @Override
    public String uploadAsset(MultipartFile file) {
        try {
            log.info("Initiating upload for asset: {} size: {}", file.getOriginalFilename(), file.getSize());

            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "auto",
                            "folder", "kuspid/beats",
                            "public_id", UUID.randomUUID().toString()));

            String publicId = (String) uploadResult.get("public_id");
            log.info("Asset successfully persisted. PublicID: {}", publicId);
            return publicId;
        } catch (IOException e) {
            log.error("IO Failure during Cloudinary egress. Payload: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Media persistence failure", e);
        }
    }

    @Override
    public String uploadAsset(File file, String folder) {
        try {
            log.info("Persisting local binary to Cloudinary. Path: {} -> Folder: {}", file.getAbsolutePath(), folder);

            Map<?, ?> uploadResult = cloudinary.uploader().upload(file,
                    ObjectUtils.asMap(
                            "resource_type", "auto",
                            "folder", folder));

            return (String) uploadResult.get("public_id");
        } catch (IOException e) {
            log.error("Fatal exception during local file migration to Cloudinary", e);
            throw new RuntimeException("Secondary storage synchronization failed", e);
        }
    }

    @Override
    public String getAssetUrl(String assetId) {
        // Cloudinary handles delivery via their global CDN
        // We ensure we use the secure version
        return cloudinary.url().secure(true).generate(assetId);
    }

    @Override
    public void deleteAsset(String assetId) {
        try {
            log.info("Purging asset from CDN and storage. ID: {}", assetId);
            cloudinary.uploader().destroy(assetId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            log.warn("Non-critical cleanup failure for asset: {}. Metadata might be orphaned.", assetId, e);
        }
    }

    // Legacy support for methods used in BeatService before refactor
    public String uploadFile(MultipartFile file) {
        return uploadAsset(file);
    }

    public String uploadFile(File file, String key) {
        // In Cloudinary, the 'key' is the public_id. We strip folder if it exists or
        // use it.
        return uploadAsset(file, "kuspid/waveforms");
    }

    public void deleteFile(String fileName) {
        deleteAsset(fileName);
    }
}
