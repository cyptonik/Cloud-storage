package org.cloud.storage.controller;

import org.cloud.storage.dto.S3ResourceDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.Arrays;

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

        try {
            S3ResourceDto file = findFile(path);
            S3ResourceDto folder = findFolder(path);
            if (file == null && folder == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "not found");
            }
            return (file != null) ? file : folder;
        } catch (S3Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.awsErrorDetails().errorMessage());
        }
    }

    private S3ResourceDto findFolder(String path) throws S3Exception {
        String prefix = findS3Prefix(path);
        if (prefix == null) {
            return null;
        }

        if (!path.endsWith("/")) {
            return null;
        }

        String folderName = path.equals("/") ? path : Arrays.stream(path.split("/")).toList().getLast();
        return new S3ResourceDto(prefix, folderName, null, "DIRECTORY");
    }

    private S3ResourceDto findFile(String path) throws S3Exception {
        S3Object fileObject = findS3Object(path);
        if (fileObject == null) {
            return null;
        }

        if (path.endsWith("/") || path.isEmpty()) {
            return null;
        }

        String fileName = path.contains("/") ? path.substring(path.lastIndexOf("/") + 1) : path;

        String exactPath = path.length() - fileName.length() > 0 ? path.substring(0, path.length() - fileName.length()) : "/";
        return new S3ResourceDto(exactPath, fileName, fileObject.size(), "FILE");
    }

    private S3Object findS3Object(String path) {
        ListObjectsV2Response res = s3Client.listObjectsV2(ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(path)
                .maxKeys(1)
                .build());

        return res.contents().stream().filter(obj -> obj.key().equals(path)).findFirst().orElse(null);
    }

    private String findS3Prefix(String path) {
        ListObjectsV2Response res = s3Client.listObjectsV2(ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(path)
                .maxKeys(1)
                .build());

        return res.prefix();
    }
}
