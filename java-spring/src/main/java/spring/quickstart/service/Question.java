package quickstart.service;

import jakarta.annotation.Nonnull;

public record Question(
        @Nonnull String qid,
        @Nonnull String content
) { }
