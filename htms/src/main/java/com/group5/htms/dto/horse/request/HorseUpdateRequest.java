package com.group5.htms.dto.horse.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class HorseUpdateRequest {
    @Schema(hidden = true)
    private Integer ownerId;

    @Schema(description = "Only send this field when changing horse name", example = "Thunder")
    @Size(max = 100, message = "Horse name must not exceed 100 characters")
    private String name;

    @Schema(description = "Only send this field when changing breed", example = "Thoroughbred")
    @Size(max = 100, message = "Breed must not exceed 100 characters")
    private String breed;

    @Schema(description = "Only send this field when changing age", example = "4")
    @Min(value = 0, message = "Age must be greater than or equal to 0")
    private Integer age;

    @Schema(description = "Only send this field when changing weight", example = "450")
    private BigDecimal weightKg;

    @Schema(description = "Only send this field when changing rank group", example = "A")
    private String rankGroup;

    @Schema(description = "Only send this field when changing ranking points", example = "120")
    @Min(value = 0, message = "Ranking points must be greater than or equal to 0")
    private Integer rankingPoints;

    @Schema(description = "Only send this field when changing avatar url", example = "https://example.com/horse.png")
    private String avatarUrl;

    @Schema(description = "Only send this field when changing total wins", example = "3")
    @Min(value = 0, message = "Total wins must be greater than or equal to 0")
    private Integer totalWins;

    @Schema(description = "Only send this field when changing status", example = "active")
    @Size(max = 20, message = "Status must not exceed 20 characters")
    private String status;

    @Schema(description = "Only send this field when changing registered time", example = "2026-06-17T16:23:38.069Z")
    private Instant registeredAt;
}
