package com.hrstack.utils;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EditProfileRequest {
    @NotBlank(message = "Name should not be empty")
    @Pattern(regexp = "^[\\p{L}\\p{M}]+(?:[ '-][\\p{L}\\p{M}]+)*$",
            message = "Name can only contain letters, spaces, apostrophes, and hyphens.")
    @Column(updatable = false)
    private String firstName;

    @NotBlank(message = "Name should not be empty")
    @Pattern(regexp = "^[\\p{L}\\p{M}]+(?:[ '-][\\p{L}\\p{M}]+)*$",
            message = "Name can only contain letters, spaces, apostrophes, and hyphens.")
    @Column(updatable = false)
    private String lastName;

    @NotBlank(message = "Phone number should not be empty")
    @Pattern(regexp = "^\\+234(70|80|81|90|91)\\d{8}$", message = "Enter a valid phone number and ensure it starts with +234")
    private String phoneNumber;

}
