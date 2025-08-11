package br.com.dio.service;

import br.com.dio.dto.BoardDetailsDTO;
import br.com.dio.dto.BlockReportDTO;
import br.com.dio.persistence.dao.BoardColumnDAO;
import br.com.dio.persistence.dao.BoardDAO;
import br.com.dio.persistence.dao.BlockDAO;
import br.com.dio.persistence.entity.BoardEntity;
import lombok.AllArgsConstructor;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class BoardQueryService {
    private final Connection connection;

    public Optional<BoardEntity> findById(final Long id) throws SQLException {
        var dao = new BoardDAO(connection);
        var boardColumnDAO = new BoardColumnDAO(connection);
        var optional = dao.findById(id);
        if (optional.isPresent()) {
            var entity = optional.get();
            entity.setBoardColumns(boardColumnDAO.findByBoardId(entity.getId()));
            return Optional.of(entity);
        }
        return Optional.empty();
    }

    public Optional<BoardDetailsDTO> showBoardDetails(final Long id) throws SQLException {
        var dao = new BoardDAO(connection);
        var boardColumnDAO = new BoardColumnDAO(connection);
        var optional = dao.findById(id);
        if (optional.isPresent()) {
            var entity = optional.get();
            var columns = boardColumnDAO.findByBoardIdWithDetails(entity.getId());
            var dto = new BoardDetailsDTO(entity.getId(), entity.getName(), columns);
            return Optional.of(dto);
        }
        return Optional.empty();
    }

    public List<BlockReportDTO> generateBlockReport(Long cardId) throws SQLException {
        var blockDAO = new BlockDAO(connection);
        var blocks = blockDAO.findByCardId(cardId);

        return blocks.stream().map(block -> {
            Duration duration = null;
            if (block.getUnblockedAt() != null) {
                duration = Duration.between(block.getBlockedAt(), block.getUnblockedAt());
            }
            return new BlockReportDTO(block.getBlockReason(), block.getUnblockReason(), duration);
        }).toList();
    }

    public List<BoardEntity> findAllBoards() throws SQLException {
        var dao = new BoardDAO(connection);
        return dao.findAll();
    }
}