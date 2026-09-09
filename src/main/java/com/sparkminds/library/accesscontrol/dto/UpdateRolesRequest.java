package com.sparkminds.library.accesscontrol.dto;

import com.sparkminds.library.member.entity.RoleName;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record UpdateRolesRequest(
    @NotNull @Size(min = 1, message = "A user must have at least one role") Set<RoleName> roles) {}
