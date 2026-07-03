package com.hrstack.mappers;

import com.hrstack.dto.requestDto.RegisterCompanyRequest;
import com.hrstack.entities.Company;
import com.hrstack.properties.WorkspaceProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompanyMapper {
    private final WorkspaceProperties workspaceProperties;

    public Company toEntity(RegisterCompanyRequest request) {
        return Company.builder()
                .companyName(request.getCompanyName())
                .workspaceUrl(workspaceProperties.getBaseUrl() + request.getWorkspaceUrl())
                .adminEmail(request.getAdminEmail())
                .build();
    }
}
