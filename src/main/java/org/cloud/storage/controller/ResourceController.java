package org.cloud.storage.controller;

import org.cloud.storage.dto.S3ResourceDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.S3Exception;

@RestController
public class ResourceController {
    @Value("${s3.bucket}")
    private String bucket;

    private final S3Client s3Client;

    public ResourceController(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @GetMapping("/api/resource")
    public S3ResourceDto get(@RequestParam(required = false) String path) {
        if (path == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid path");
        }

        return null;
    }
}
