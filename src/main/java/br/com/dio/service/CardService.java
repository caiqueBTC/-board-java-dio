package br.com.dio.service;

import br.com.dio.dto.BoardColumnInfoDTO;
import br.com.dio.exception.CardBlockedException;
import br.com.dio.exception.CardFinishedException;
import br.com.dio.exception.EntityNotFoundException;
import br.com.dio.persistence.dao.BlockDAO;
import br.com.dio.persistence.dao.CardDAO;
import br.com.dio.persistence.dao.CardMovementDAO;
import br.com.dio.persistence.entity.CardEntity;
import lombok.AllArgsConstructor;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import static br.com.dio.persistence.entity.BoardColumnKindEnum.CANCEL;
import static br.com.dio.persistence.entity.BoardColumnKindEnum.FINAL;

@AllArgsConstructor
public class CardService {
    private final Connection connection;

    public CardEntity create(final CardEntity entity) throws SQLException {
        try {
            var dao = new CardDAO(connection);
            var movementDao = new CardMovementDAO(connection);

            dao.insert(entity);
            movementDao.insert(entity.getId(), null, entity.getBoardColumn().getId());

            connection.commit();
            return entity;
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        }
    }

    public void moveToNextColumn(final Long cardId, final List<BoardColumnInfoDTO> boardColumnsInfo) throws SQLException {
        var dao = new CardDAO(connection);
        var optional = dao.findById(cardId);
        var dto = optional.orElseThrow(
                () -> new EntityNotFoundException("O card de id %s não foi encontrado".formatted(cardId))
        );

        if (dto.blocked()) {
            var message = "O card %s está bloqueado, é necesário desbloquea-lo para mover".formatted(cardId);
            throw new CardBlockedException(message);
        }

        var currentColumn = boardColumnsInfo.stream()
                .filter(bc -> bc.id().equals(dto.columnId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("O card informado pertence a outro board"));

        if (currentColumn.kind().equals(FINAL)) {
            throw new CardFinishedException("O card já foi finalizado");
        }

        var nextColumn = boardColumnsInfo.stream()
                .filter(bc -> bc.order() == currentColumn.order() + 1)
                .findFirst().orElseThrow(() -> new IllegalStateException("O card está na penúltima coluna"));

        try {
            var movementDao = new CardMovementDAO(connection);
            dao.moveToColumn(nextColumn.id(), cardId);
            movementDao.insert(cardId, currentColumn.id(), nextColumn.id());
            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        }
    }

    public void cancel(final Long cardId, final Long cancelColumnId) throws SQLException {
        var dao = new CardDAO(connection);
        var optional = dao.findById(cardId);
        var dto = optional.orElseThrow(
                () -> new EntityNotFoundException("O card de id %s não foi encontrado".formatted(cardId))
        );

        if (dto.blocked()) {
            var message = "O card %s está bloqueado, é necesário desbloquea-lo para mover".formatted(cardId);
            throw new CardBlockedException(message);
        }

        try {
            var movementDao = new CardMovementDAO(connection);
            Long currentColumnId = dto.columnId();
            dao.moveToColumn(cancelColumnId, cardId);
            movementDao.insert(cardId, currentColumnId, cancelColumnId);
            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        }
    }

    public void block(final Long id, final String reason, final List<BoardColumnInfoDTO> boardColumnsInfo) throws SQLException {
        try {
            var dao = new CardDAO(connection);
            var optional = dao.findById(id);
            var dto = optional.orElseThrow(
                    () -> new EntityNotFoundException("O card de id %s não foi encontrado".formatted(id))
            );

            if (dto.blocked()) {
                var message = "O card %s já está bloqueado".formatted(id);
                throw new CardBlockedException(message);
            }

            var currentColumn = boardColumnsInfo.stream()
                    .filter(bc -> bc.id().equals(dto.columnId()))
                    .findFirst()
                    .orElseThrow();

            if (currentColumn.kind().equals(FINAL) || currentColumn.kind().equals(CANCEL)) {
                var message = "O card está em uma coluna do tipo %s e não pode ser bloqueado"
                        .formatted(currentColumn.kind());
                throw new IllegalStateException(message);
            }

            var blockDAO = new BlockDAO(connection);
            blockDAO.block(reason, id);
            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        }
    }

    public void unblock(final Long id, final String reason) throws SQLException {
        try {
            var dao = new CardDAO(connection);
            var optional = dao.findById(id);
            var dto = optional.orElseThrow(
                    () -> new EntityNotFoundException("O card de id %s não foi encontrado".formatted(id))
            );

            if (!dto.blocked()) {
                var message = "O card %s não está bloqueado".formatted(id);
                throw new CardBlockedException(message);
            }

            var blockDAO = new BlockDAO(connection);
            blockDAO.unblock(reason, id);
            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        }
    }

    public void update(Long cardId, String newTitle, String newDescription) throws SQLException {
        var dao = new CardDAO(connection);
        var dto = dao.findById(cardId).orElseThrow(
                () -> new EntityNotFoundException("O card de id %s não foi encontrado".formatted(cardId))
        );
    
        if (dto.blocked()) {
            throw new CardBlockedException("O card %s está bloqueado e não pode ser editado.".formatted(cardId));
        }
    
        try {
            var cardToUpdate = new CardEntity();
            cardToUpdate.setId(cardId);
            cardToUpdate.setTitle(newTitle);
            cardToUpdate.setDescription(newDescription);
            
            dao.update(cardToUpdate);
            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        }
    }
}