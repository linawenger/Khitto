package quickstart.service;

import jakarta.annotation.Nonnull;

public record Game(
        @Nonnull String id,
        @Nonnull String status
) { }
