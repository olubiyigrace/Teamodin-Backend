package com.hrstack.dto.responseDto;

import com.hrstack.enums.Role;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private String jobTitle;
    private String department;
    private String phoneNumber;
}
