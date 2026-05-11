package org.cloud.storage.service;

import org.cloud.storage.dto.S3ResourceDto;
import org.cloud.storage.repository.S3Repository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;

@Service
public class ResourceService {
    private final S3Repository s3Repository;

    public ResourceService(S3Repository s3Repository) {
        this.s3Repository = s3Repository;
    }

    public S3ResourceDto uploadS3Resource(String path, MultipartFile file) {
        if (path.endsWith("/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file path cannot end with /");
        }

        if (s3Repository.fileExists(path) || s3Repository.folderExists(path)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "this path is already occupied");
        }

        try {
            s3Repository.putObject(path, file);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "upload failed");
        }
        return findFile(path);
    }

    public void deleteS3Resource(String path) {
        if (path.endsWith("/")) {
            deleteFolder(path);
        } else {
            deleteFile(path);
        }
    }

    private void deleteFolder(String path) {
        if (!s3Repository.folderExists(path)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "folder does not exist");
        }

        s3Repository.removeObjects(path);
    }

    private void deleteFile(String path) {
        if (!s3Repository.fileExists(path)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "file does not exist");
        }

        s3Repository.deleteObject(path);
    }

    public S3ResourceDto findS3Resource(String path) {
        if (path.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "invalid path");
        }

        return (path.endsWith("/")) ? findFolder(path) : findFile(path);
    }

    private S3ResourceDto findFolder(String path) {
        if (!s3Repository.folderExists(path)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "folder does not exist");
        }

        String trimmed = path.substring(0, path.length() - 1);
        String name = trimmed.contains("/")
                ? trimmed.substring(trimmed.lastIndexOf("/") + 1)
                : trimmed;
        String exactPath = trimmed.contains("/")
                ? trimmed.substring(0, trimmed.lastIndexOf("/") + 1)
                : "";

        return new S3ResourceDto(exactPath, name, null, "DIRECTORY");
    }


    private S3ResourceDto findFile(String path) {
        if (!s3Repository.fileExists(path)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "file does not exist");
        }

        String name = path.contains("/")
                ? path.substring(path.lastIndexOf("/") + 1)
                : path;
        String exactPath = path.contains("/")
                ? path.substring(0, path.lastIndexOf("/") + 1)
                : "";

        return new S3ResourceDto(exactPath, name, s3Repository.getContentLength(path), "FILE");
    }
}
