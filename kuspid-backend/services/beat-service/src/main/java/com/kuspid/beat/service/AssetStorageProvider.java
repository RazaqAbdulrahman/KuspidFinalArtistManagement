package com.kuspid.beat.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.File;

/**
 * Enterprise-grade contract for asset storage.
 * Designed for horizontal scalability and provider agnosticism.
 */
public interface AssetStorageProvider {

    /**
     * Upload an asset and return the unique identifier (URL or PublicID).
     */
    String uploadAsset(MultipartFile file);

    /**
     * Upload a local file and return the identifier.
     */
    String uploadAsset(File file, String folder);

    /**
     * Retrieve the public access URL for an asset.
     */
    String getAssetUrl(String assetId);

    /**
     * Permanent removal of an asset from the provider.
     */
    void deleteAsset(String assetId);
}
