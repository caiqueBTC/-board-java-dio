package br.com.dio.dto;

import java.time.Duration;

public record BlockReportDTO(
    String blockReason,
    String unblockReason,
    Duration duration
) {}