package com.capd.capdbackend.domain.report.service;

import com.google.cloud.storage.Storage;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.StorageOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;

@Service
@Slf4j
public class GcpStorageService {

    private final Storage storage;
    private final String bucketName;

    public GcpStorageService(
            @Value("${gcp.storage.credentials-path}") String credentialsPath,
            @Value("${gcp.storage.bucket-name}") String bucketName) throws IOException {

        this.bucketName = bucketName;
        this.storage = StorageOptions.newBuilder()
                .setCredentials(GoogleCredentials.fromStream(
                        new FileInputStream(credentialsPath)))
                .build()
                .getService();

        log.info("GCP Storage 연결 완료: bucket={}", bucketName);
    }

    // PDF 파일 업로드
    public String uploadPdf(byte[] pdfBytes, String fileName) {
        try {
            BlobId blobId = BlobId.of(bucketName, fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType("application/pdf")
                    .build();

            storage.create(blobInfo, pdfBytes);

            // 공개 URL 반환
            String url = String.format(
                    "https://storage.googleapis.com/%s/%s",
                    bucketName, fileName);

            log.info("PDF 업로드 완료: {}", url);
            return url;

        } catch (Exception e) {
            log.error("PDF 업로드 실패: {}", e.getMessage());
            throw new RuntimeException("PDF 업로드 실패: " + e.getMessage());
        }
    }
}
