package br.com.dio.persistence.dao;

import lombok.AllArgsConstructor;
import java.sql.Connection;
import java.sql.SQLException;

@AllArgsConstructor
public class CardMovementDAO {
    private final Connection connection;

    public void insert(Long cardId, Long sourceColumnId, Long destinationColumnId) throws SQLException {
        var sql = "INSERT INTO CARD_MOVEMENTS (card_id, source_column_id, destination_column_id) VALUES (?, ?, ?)";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, cardId);
            if (sourceColumnId != null) {
                statement.setLong(2, sourceColumnId);
            } else {
                statement.setNull(2, java.sql.Types.BIGINT);
            }
            statement.setLong(3, destinationColumnId);
            statement.executeUpdate();
        }
    }
}