package com.sparkminds.library.systemconfig.controller;

import com.sparkminds.library.systemconfig.dto.request.MaintenanceUpdateRequest;
import com.sparkminds.library.systemconfig.dto.response.SystemConfigResponse;
import com.sparkminds.library.systemconfig.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/system-config")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "System Configuration")
@SecurityRequirement(name = "bearerAuth")
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @GetMapping
    @Operation(summary = "Get current system configuration")
    public ResponseEntity<SystemConfigResponse> getCurrentConfig() {
        return ResponseEntity.ok(
                systemConfigService.getCurrentConfig());
    }

    @PutMapping("/maintenance")
    @Operation(summary = "Enable or disable maintenance mode")
    public ResponseEntity<SystemConfigResponse> updateMaintenanceMode(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody MaintenanceUpdateRequest request) {
        return ResponseEntity.ok(
                systemConfigService.updateMaintenanceMode(
                        jwt,
                        request));
    }
}