package com.kuspid.beat.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.File;

public interface AssetStorageProvider {
    String uploadAsset(MultipartFile file);

    String uploadAsset(File file, String folder);

    String getAssetUrl(String assetId);

    void deleteAsset(String assetId);
}
