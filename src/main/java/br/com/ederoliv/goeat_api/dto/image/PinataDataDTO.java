package br.com.ederoliv.goeat_api.dto.image;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PinataDataDTO(
        String id,
        @JsonProperty("user_id")
        String userId,
        String name,
        String network,
        boolean vectorized,
        @JsonProperty("created_at")
        String createdAt,
        @JsonProperty("updated_at")
        String updatedAt,
        @JsonProperty("accept_duplicates")
        boolean acceptDuplicates,
        boolean streamable,
        String cid,
        @JsonProperty("mime_type")
        String mimeType,
        long size,
        @JsonProperty("number_of_files")
        int numberOfFiles
) {}
