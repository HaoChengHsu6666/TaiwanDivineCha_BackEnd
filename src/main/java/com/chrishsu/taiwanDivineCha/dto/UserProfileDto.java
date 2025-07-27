package com.chrishsu.taiwanDivineCha.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UserProfileDto {
    private String email;
    private String name;
    private String mobile;
    private LocalDate birth;
    private Boolean isProfileCompleted;
}
