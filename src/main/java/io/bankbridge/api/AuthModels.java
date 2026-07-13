package io.bankbridge.api;

import jakarta.validation.constraints.NotBlank;

public class AuthModels {

    public record AuthRequest(
            @NotBlank String username,
            @NotBlank String password
    ) {}

    public record AuthResponse(
            String accessToken,
            String tokenType,
            long expiresIn,
            String username,
            String role
    ) {}

    public record MeResponse(
            String username,
            String role
    ) {}
}
