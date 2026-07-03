package com.hrstack.repositories;

import com.hrstack.entities.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, String> {
    Optional<Company> findByWorkspaceUrl(String workspaceUrl);
    Company findByAdminEmail(String adminEmail);
}
