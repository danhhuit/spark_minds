package com.sparkminds.library.auth.controller;

import com.sparkminds.library.auth.dto.response.TokenInfoResponse;
import com.sparkminds.library.auth.service.TokenInspectionService;
import com.sparkminds.library.common.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/super-admin/tokens")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasAuthority('ACCESS_CONTROL_MANAGE')")
@Tag(name = "Super Admin Token Inspection")
@SecurityRequirement(name = "bearerAuth")
public class TokenInspectionController {

  private final TokenInspectionService tokenInspectionService;

  @GetMapping("/access")
  @Operation(summary = "List registered access-token metadata")
  public ResponseEntity<PageResponse<TokenInfoResponse>> getAccessTokens(
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
    return ResponseEntity.ok(tokenInspectionService.getAccessTokens(page, size));
  }

  @GetMapping("/refresh")
  @Operation(summary = "List registered refresh-token metadata")
  public ResponseEntity<PageResponse<TokenInfoResponse>> getRefreshTokens(
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
    return ResponseEntity.ok(tokenInspectionService.getRefreshTokens(page, size));
  }
}
