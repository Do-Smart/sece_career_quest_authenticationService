package com.example.authenticationService.Utils;


public interface Constants {
    String STUDENT = "STUDENT";
    String USER_NOT_FOUND = "User not found with username: ";
    String ADMIN = "ADMIN";
    String STAFF = "STAFF";
    String DEFAULT_USER = "javai";
    String DEFAULT_PASSWORD = "password";
    String ADMIN_ACCESS = "hasAuthority('ADMIN')";
    String STAFF_ACCESS = "hasAuthority('STAFF')";

    String AUTHORIZATION = "Authorization";

    String STUDENT_ACCESS = "hasAuthority('STUDENT')";
    String CLAIMS_ATTR = "claims";
    String PASSWORD_VALIDATION = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$";
    String SUB = "sub";

    String OR = " or ";




}
