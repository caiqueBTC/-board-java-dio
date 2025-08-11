package br.com.dio.persistence.entity;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class CardEntity {
    private Long id;
    private String title;
    private String description;
    private OffsetDateTime createdAt;

    private BoardColumnEntity boardColumn = new BoardColumnEntity();
}