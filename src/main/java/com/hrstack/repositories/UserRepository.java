package com.hrstack.repositories;

import com.hrstack.entities.Company;
import com.hrstack.entities.User;
import com.hrstack.enums.InviteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    Page<User> findByCompanyAndStatus(Company company, InviteStatus inviteStatus, PageRequest pageRequest);
    Page<User> findByCompany(Company company, PageRequest pageRequest);
}
