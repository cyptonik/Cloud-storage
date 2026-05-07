package org.cloud.storage.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record S3ResourceDto(String path, String name, Long size, String type) {}
