package com.ditto.example.spring.quickstart.service;

import jakarta.annotation.Nonnull;

public record Active(
        @Nonnull String aid
) { }
