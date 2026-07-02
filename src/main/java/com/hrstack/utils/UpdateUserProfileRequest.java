package com.hrstack.utils;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateUserProfileRequest {

    @NotBlank(message = "Name should not be empty")
    @Pattern(regexp = "^[A-Za-z]+(?:[-\\s][A-Za-z]+)*(?:\\s[A-Za-z]+(?:[-\\s][A-Za-z]+)*)?\\s[A-Za-z]+(?:[-\\s][A-Za-z]+)*$",
            message = "Please enter first name, middle name(Optional), and last name separated by spaces")
    @Column(updatable = false)
    private String adminName;
}
