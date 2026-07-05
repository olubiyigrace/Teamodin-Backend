package com.hrstack.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hrstack.enums.InviteStatus;
import com.hrstack.enums.ReportsTo;
import com.hrstack.enums.Role;
import com.hrstack.enums.UserProfileStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Enumerated(EnumType.STRING)
    private InviteStatus status;

    @Enumerated(EnumType.STRING)
    private ReportsTo reportsTo;

    @Column(nullable = false)
    private String jobTitle;

    @Column(nullable = false)
    private String department;
    private String phoneNumber;
    private String imageUrl;
    private String imagePublicId;

    @Column(name = "invite_token", columnDefinition = "text")
    private String inviteToken;

    @Column(nullable = false)
    private String password;

    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private UserProfileStatus userProfileStatus;

    @CreatedDate
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S]")
    private LocalDateTime createdAt;

    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S]")
    private LocalDateTime expiresAt;

    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S]")
    private  LocalDateTime updatedAt;

    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S]")
    private LocalDateTime editedAt;

    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S]")
    private  LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", foreignKey = @ForeignKey(name = "fk_user_company_id"))
    private Company company;

    public String getCompanyId(){
        if(this.company != null){
            return this.company.getId();
        }
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }
}
