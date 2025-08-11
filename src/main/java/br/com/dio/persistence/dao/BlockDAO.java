package br.com.dio.persistence.dao;

import br.com.dio.persistence.entity.BlockEntity;
import lombok.AllArgsConstructor;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import static br.com.dio.persistence.converter.OffsetDateTimeConverter.toOffsetDateTime;
import static br.com.dio.persistence.converter.OffsetDateTimeConverter.toTimestamp;

@AllArgsConstructor
public class BlockDAO {
    private final Connection connection;

    public void block(final String reason, final Long cardId) throws SQLException {
        var sql = "INSERT INTO BLOCKS (blocked_at, block_reason, card_id) VALUES (?, ?, ?);";
        try (var statement = connection.prepareStatement(sql)) {
            var i = 1;
            statement.setTimestamp(i++, toTimestamp(OffsetDateTime.now()));
            statement.setString(i++, reason);
            statement.setLong(i, cardId);
            statement.executeUpdate();
        }
    }

    public void unblock(final String reason, final Long cardId) throws SQLException {
        var sql = "UPDATE BLOCKS SET unblocked_at = ?, unblock_reason = ? WHERE card_id = ? AND unblocked_at IS NULL;";
        try (var statement = connection.prepareStatement(sql)) {
            var i = 1;
            statement.setTimestamp(i++, toTimestamp(OffsetDateTime.now()));
            statement.setString(i++, reason);
            statement.setLong(i, cardId);
            statement.executeUpdate();
        }
    }

    public List<BlockEntity> findByCardId(Long cardId) throws SQLException {
        var list = new ArrayList<BlockEntity>();
        var sql = "SELECT * FROM BLOCKS WHERE card_id = ? ORDER BY blocked_at";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, cardId);
            var resultSet = statement.executeQuery();
            while (resultSet.next()) {
                var entity = new BlockEntity();
                entity.setId(resultSet.getLong("id"));
                entity.setBlockReason(resultSet.getString("block_reason"));
                entity.setUnblockReason(resultSet.getString("unblock_reason"));
                entity.setBlockedAt(toOffsetDateTime(resultSet.getTimestamp("blocked_at")));
                entity.setUnblockedAt(toOffsetDateTime(resultSet.getTimestamp("unblocked_at")));
                list.add(entity);
            }
        }
        return list;
    }
}