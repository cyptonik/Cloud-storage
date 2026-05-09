package org.cloud.storage.controller;

import org.cloud.storage.dto.S3ResourceDto;
import org.cloud.storage.service.ResourceService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.s3.model.*;

@RestController
public class ResourceController {
    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping("/api/resource")
    public S3ResourceDto getResourceByPath(@RequestParam String path) {
        if (path == null || path.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid path");
        }

        try {
            S3ResourceDto resourceDto = resourceService.findS3Resource(path);
            if (resourceDto == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "not found");
            }
            return resourceDto;
        } catch (S3Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.awsErrorDetails().errorMessage());
        }
    }

    @DeleteMapping("/api/resource")
    public void deleteResourceByPath(@RequestParam String path) {
        if (path == null || path.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid path");
        }

        try {
            resourceService.deleteS3Resource(path);
        } catch (S3Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.awsErrorDetails().errorMessage());
        }
    }

    @PostMapping("/api/resource")
    public S3ResourceDto uploadResourceByPath(@RequestParam String path, @RequestParam MultipartFile file) {
        if (path == null || path.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid path");
        }

        try {
            S3ResourceDto resourceDto = resourceService.uploadS3Resource(path, file);
            if (resourceDto == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "not found");
            }
            return resourceDto;
        } catch (S3Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.awsErrorDetails().errorMessage());
        }
    }
}
