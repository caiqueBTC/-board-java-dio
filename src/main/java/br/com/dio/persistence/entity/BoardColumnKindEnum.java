package br.com.dio.persistence.entity;

import java.util.stream.Stream;

public enum BoardColumnKindEnum {
    INITIAL,
    PENDING,
    FINAL,
    CANCEL;

    public static BoardColumnKindEnum findByName(final String name) {
        return Stream.of(BoardColumnKindEnum.values())
                .filter(b -> b.name().equals(name))
                .findFirst()
                .orElse(null);
    }
}
