package quickstart.service;

import jakarta.annotation.Nonnull;

public record Player(
        @Nonnull String pid,
        @Nonnull String ip
) { }
