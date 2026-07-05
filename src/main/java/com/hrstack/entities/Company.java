package com.hrstack.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hrstack.enums.UserProfileStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "companies")
@EntityListeners(AuditingEntityListener.class)
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

    @Enumerated(EnumType.STRING)
    private UserProfileStatus userProfileStatus;

    @Column(nullable = false)
    private Boolean isVerified = false;

    @Email
    @Column(unique = true, nullable = false)
    private String adminEmail;

   @CreatedDate
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S]")
    private LocalDateTime createdAt;
}
