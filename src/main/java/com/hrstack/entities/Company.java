package com.hrstack.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "companies")
public class Company{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false, unique = true)
    private String workspaceUrl;

    @Column(nullable = false)
    private String adminPassword;

    @Column(nullable = false)
    private Boolean isVerified = false;

    @Email
    @Column(unique = true, nullable = false)
    private String adminEmail;

    @Builder.Default
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S]")
    private LocalDateTime createdAt = LocalDateTime.now();
}
