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

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService implements AssetStorageProvider {

    private final Cloudinary cloudinary;

    @Override
    public String uploadAsset(MultipartFile file) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "auto",
                            "folder", "kuspid/beats",
                            "public_id", UUID.randomUUID().toString()));
            return (String) uploadResult.get("public_id");
        } catch (IOException e) {
            log.error("Cloudinary upload failed", e);
            throw new RuntimeException("Upload failed", e);
        }
    }

    @Override
    public String uploadAsset(File file, String folder) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file,
                    ObjectUtils.asMap(
                            "resource_type", "auto",
                            "folder", folder));
            return (String) uploadResult.get("public_id");
        } catch (IOException e) {
            log.error("Cloudinary file upload failed", e);
            throw new RuntimeException("File upload failed", e);
        }
    }

    @Override
    public String getAssetUrl(String assetId) {
        if (assetId == null)
            return null;
        return cloudinary.url().secure(true).generate(assetId);
    }

    @Override
    public void deleteAsset(String assetId) {
        try {
            cloudinary.uploader().destroy(assetId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            log.warn("Cloudinary delete failed for: {}", assetId, e);
        }
    }

    // Legacy support
    public String uploadFile(MultipartFile file) {
        return uploadAsset(file);
    }

    public String uploadFile(File file, String key) {
        return uploadAsset(file, "kuspid/waveforms");
    }

    public String getFileUrl(String fileName) {
        return getAssetUrl(fileName);
    }

    public void deleteFile(String fileName) {
        deleteAsset(fileName);
    }
}
