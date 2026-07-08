package com.hrstack.dto.responseDto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImageUploadResponse {
    private String imageUrl;
    private String publicId;
}

