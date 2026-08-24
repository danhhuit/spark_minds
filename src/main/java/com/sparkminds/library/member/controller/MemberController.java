package com.sparkminds.library.member.controller;

import com.sparkminds.library.common.api.PageResponse;
import com.sparkminds.library.member.dto.request.CreateMemberRequest;
import com.sparkminds.library.member.dto.request.MemberSearchRequest;
import com.sparkminds.library.member.dto.request.UpdateMemberRequest;
import com.sparkminds.library.member.dto.response.MemberResponse;
import com.sparkminds.library.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Member Management")
@SecurityRequirement(name = "bearerAuth")
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    @Operation(summary = "Search members")
    public ResponseEntity<PageResponse<MemberResponse>> search(
            @Valid @ModelAttribute
            MemberSearchRequest request,

            @RequestParam(defaultValue = "0")
            @Min(0)
            int page,

            @RequestParam(defaultValue = "10")
            @Min(1)
            @Max(
                value = 10,
                message = "Each page contains at most 10 records"
            )
            int size,

            @RequestParam(defaultValue = "id")
            String sortBy,

            @RequestParam(defaultValue = "desc")
            String direction
    ) {
        return ResponseEntity.ok(
                memberService.search(
                    request,
                    page,
                    size,
                    sortBy,
                    direction
                )
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get member details")
    public ResponseEntity<MemberResponse> getById(
            @PathVariable
            @Positive
            Long id
    ) {
        return ResponseEntity.ok(
                memberService.getById(id)
        );
    }

    @PostMapping
    @Operation(summary = "Create member")
    public ResponseEntity<MemberResponse> create(
            @Valid @RequestBody CreateMemberRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(memberService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update member")
    public ResponseEntity<MemberResponse> update(
            @PathVariable
            @Positive
            Long id,

            @Valid @RequestBody
            UpdateMemberRequest request
    ) {
        return ResponseEntity.ok(
                memberService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate member")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(
            @PathVariable
            @Positive
            Long id
    ) {
        memberService.deactivate(id);
    }
}