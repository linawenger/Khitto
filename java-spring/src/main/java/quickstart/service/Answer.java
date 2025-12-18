package quickstart.service;

import jakarta.annotation.Nonnull;

public record Answer(
        @Nonnull String aid,
        @Nonnull String content,
        @Nonnull String answer,
        int count
) { }
