package com.ditto.example.spring.quickstart.service;

import jakarta.annotation.Nonnull;

public record Game(
        @Nonnull String id,
        @Nonnull String status
) { }
