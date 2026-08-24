package com.sparkminds.library.member.dto.request;

import com.sparkminds.library.member.entity.RoleName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class MemberSearchRequest {

    private String keyword;
    private String fullName;
    private String email;
    private String membershipCode;
    private Long bookId;
    private String bookTitle;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirthFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirthTo;

    private Boolean enabled;
    private Boolean emailVerified;
    private Boolean accountNonLocked;
    private RoleName role;
}