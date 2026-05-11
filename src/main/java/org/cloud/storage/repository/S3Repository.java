package org.cloud.storage.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

import java.io.IOException;
import java.util.List;

@Repository
public class S3Repository {
    @Value("${s3.bucket}")
    private String bucket;

    private final S3Client s3Client;

    public S3Repository(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public Long getContentLength(String path) {
        return headObject(path).contentLength();
    }

    public void putObject(String path, MultipartFile file) throws IOException{
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(path)
                        .contentType(file.getContentType())
                        .contentLength(file.getSize())
                        .build(),
                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );
    }

    public void deleteObject(String path) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(path)
                .build());
    }

    public void removeObjects(String prefix) {
        ListObjectsV2Request request = listObjectsV2Request(prefix);
        ListObjectsV2Iterable paginator = s3Client.listObjectsV2Paginator(request);

        for (ListObjectsV2Response page : paginator) {
            List<ObjectIdentifier> objects = page.contents().stream()
                    .map(obj -> ObjectIdentifier.builder().key(obj.key()).build())
                    .toList();

            if (!objects.isEmpty()) {
                deleteObjects(objects);
            }
        }
    }

    private ListObjectsV2Request listObjectsV2Request(String prefix) {
        return ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefix)
                .build();
    }

    private void deleteObjects(List<ObjectIdentifier> objects) {
        s3Client.deleteObjects(DeleteObjectsRequest.builder()
                .bucket(bucket)
                .delete(Delete.builder().objects(objects).build())
                .build());
    }

    public boolean fileExists(String path) {
        try {
            headObject(path);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    private HeadObjectResponse headObject(String path) {
        return s3Client.headObject(HeadObjectRequest.builder()
                .bucket(bucket)
                .key(path)
                .build());

    }

    public boolean folderExists(String path) {
        ListObjectsV2Response res = listObjectsV2Response(path);
        return !res.contents().isEmpty() || !res.commonPrefixes().isEmpty();
    }

    private ListObjectsV2Response listObjectsV2Response(String prefix) {
        return s3Client.listObjectsV2(ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefix)
                .delimiter("/")
                .build());
    }
}
