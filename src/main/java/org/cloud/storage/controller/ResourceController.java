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
        return resourceService.findS3Resource(path);
    }

    @DeleteMapping("/api/resource")
    public void deleteResourceByPath(@RequestParam String path) {
        if (path.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid path");
        }

        resourceService.deleteS3Resource(path);
    }

    @PostMapping("/api/resource")
    @ResponseStatus(HttpStatus.CREATED)
    public S3ResourceDto uploadResourceByPath(@RequestParam String path, @RequestParam MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file is empty");
        }

        return resourceService.uploadS3Resource(path, file);
    }
}
