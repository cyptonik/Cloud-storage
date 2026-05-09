package org.cloud.storage.service;

import org.cloud.storage.dto.S3ResourceDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

import java.io.IOException;
import java.util.List;

@Service
public class ResourceService {
    @Value("${s3.bucket}")
    private String bucket;

    private final S3Client s3Client;

    public ResourceService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    // TODO : add return status
    public S3ResourceDto uploadS3Resource(String path, MultipartFile file) {
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(path)
                            .contentType(file.getContentType())
                            .contentLength(file.getSize())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "upload failed");
        }
        return findFile(path);
    }

    // TODO : add return status
    public void deleteS3Resource(String path) {
        if (path.endsWith("/")) {
            deleteFolder(path);
        } else {
            deleteFile(path);
        }
    }

    public void deleteFolder(String path) {
        String prefix = path.endsWith("/") ? path : path + "/";

        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefix)
                .build();

        ListObjectsV2Iterable paginator = s3Client.listObjectsV2Paginator(request);

        for (ListObjectsV2Response page : paginator) {
            List<ObjectIdentifier> objects = page.contents().stream()
                    .map(obj -> ObjectIdentifier.builder().key(obj.key()).build())
                    .toList();

            if (!objects.isEmpty()) {
                s3Client.deleteObjects(DeleteObjectsRequest.builder()
                        .bucket(bucket)
                        .delete(Delete.builder().objects(objects).build())
                        .build());
            }
        }
    }

    public void deleteFile(String path) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(path)
                        .build());
    }

    // TODO : add return status
    public S3ResourceDto findS3Resource(String path) {
        S3ResourceDto file = findFile(path);
        S3ResourceDto folder = findFolder(path);
        return (file != null) ? file : folder;
    }

    public S3ResourceDto findFolder(String path) {
        if (!path.endsWith("/") || !folderExists(path)) {
            return null;
        }

        String trimmed = path.substring(0, path.length() - 1);
        String name = trimmed.contains("/")
                ? trimmed.substring(trimmed.lastIndexOf("/") + 1)
                : trimmed;
        String exactPath = trimmed.contains("/")
                ? trimmed.substring(0, trimmed.lastIndexOf("/") + 1)
                : "/";

        return new S3ResourceDto(exactPath, name, null, "DIRECTORY");
    }

    private boolean folderExists(String path) {
        ListObjectsV2Response res = s3Client.listObjectsV2(ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(path)
                .build());
        return !res.contents().isEmpty() || !res.commonPrefixes().isEmpty();
    }

    public S3ResourceDto findFile(String path) {
        if (path.endsWith("/") || path.isBlank()) {
            return null;
        }

        try {
            HeadObjectResponse head = s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(path)
                    .build());

            if (head == null){
                return null;
            }

            String name = path.contains("/")
                    ? path.substring(path.lastIndexOf("/") + 1)
                    : path;
            String exactPath = path.contains("/")
                    ? path.substring(0, path.lastIndexOf("/") + 1)
                    : "/";

            return new S3ResourceDto(exactPath, name, head.contentLength(), "FILE");
        } catch (NoSuchKeyException e) {
            return null;
        }
    }
}
