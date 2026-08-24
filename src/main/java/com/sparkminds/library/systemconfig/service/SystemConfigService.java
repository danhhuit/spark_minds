package com.sparkminds.library.systemconfig.service;

import com.sparkminds.library.systemconfig.dto.request.MaintenanceUpdateRequest;
import com.sparkminds.library.systemconfig.dto.response.SystemConfigResponse;
import com.sparkminds.library.systemconfig.entity.SystemConfig;
import com.sparkminds.library.systemconfig.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private static final long SYSTEM_CONFIG_ID = 1L;

    private final SystemConfigRepository systemConfigRepository;

    @Transactional(readOnly = true)
    public SystemConfigResponse getCurrentConfig() {
        return toResponse(getConfigEntity());
    }

    @Transactional
    public SystemConfigResponse updateMaintenanceMode(
            Jwt jwt,
            MaintenanceUpdateRequest request) {
        SystemConfig config = getConfigEntity();

        config.setMaintenanceMode(request.enabled());

        if (request.message() != null
                && !request.message().isBlank()) {
            config.setMaintenanceMessage(
                    request.message().trim());
        }

        config.setUpdatedBy(jwt.getSubject());

        SystemConfig savedConfig = systemConfigRepository.save(config);

        return toResponse(savedConfig);
    }

    @Transactional(readOnly = true)
    public boolean isMaintenanceMode() {
        return getConfigEntity().isMaintenanceMode();
    }

    @Transactional(readOnly = true)
    public String getMaintenanceMessage() {
        return getConfigEntity().getMaintenanceMessage();
    }

    private SystemConfig getConfigEntity() {
        return systemConfigRepository
                .findById(SYSTEM_CONFIG_ID)
                .orElseThrow(() -> new IllegalStateException(
                        "System configuration was not initialized"));
    }

    private SystemConfigResponse toResponse(
            SystemConfig config) {
        return new SystemConfigResponse(
                config.getId(),
                config.isMaintenanceMode(),
                config.getMaintenanceMessage(),
                config.getUpdatedBy(),
                config.getUpdatedAt());
    }
}