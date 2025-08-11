package br.com.dio.ui;

import br.com.dio.dto.BoardColumnInfoDTO;
import br.com.dio.persistence.dao.CardDAO;
import br.com.dio.persistence.entity.BoardEntity;
import br.com.dio.persistence.entity.CardEntity;
import br.com.dio.service.BoardQueryService;
import br.com.dio.service.CardService;
import lombok.AllArgsConstructor;
import java.sql.SQLException;
import java.util.Scanner;
import static br.com.dio.persistence.config.ConnectionConfig.getConnection;

@AllArgsConstructor
public class BoardMenu {
    private final Scanner scanner = new Scanner(System.in).useDelimiter("\n");
    private final BoardEntity entity;

    public void execute() {
        try {
            System.out.printf("\n>>> Você está no quadro: '%s' (ID: %d) <<<\n", entity.getName(), entity.getId());
            var option = -1;
            while (option != 10) {
                System.out.println("\n== MENU DO QUADRO ==");
                System.out.println("1 - Adicionar Tarefa (Card)");
                System.out.println("2 - Avançar Tarefa (Mover Card)");
                System.out.println("3 - Bloquear Tarefa");
                System.out.println("4 - Desbloquear Tarefa");
                System.out.println("5 - Cancelar Tarefa");
                System.out.println("6 - Ver Visão Geral do Quadro");
                System.out.println("7 - Ver Detalhes de uma Tarefa");
                System.out.println("8 - Gerar Relatório de Bloqueios de uma Tarefa");
                System.out.println("9 - Editar Tarefa");
                System.out.println("10 - Voltar para o Menu Principal");
                System.out.print("Escolha uma opção: ");
                option = scanner.nextInt();
                switch (option) {
                    case 1 -> createCard();
                    case 2 -> moveCardToNextColumn();
                    case 3 -> blockCard();
                    case 4 -> unblockCard();
                    case 5 -> cancelCard();
                    case 6 -> showBoard();
                    case 7 -> showCard();
                    case 8 -> showBlockReport();
                    case 9 -> editCard();
                    case 10 -> System.out.println("\nA voltar para o menu principal...");
                    default -> System.out.println("Opção inválida. Por favor, tente novamente.");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }

    private void createCard() throws SQLException {
        var card = new CardEntity();
        System.out.println("\n--- Adicionar Nova Tarefa ---");
        System.out.print("Título da tarefa: ");
        card.setTitle(scanner.next());
        System.out.print("Descrição da tarefa: ");
        card.setDescription(scanner.next());
        card.setBoardColumn(entity.getInitialColumn());
        try (var connection = getConnection()) {
            new CardService(connection).create(card);
            System.out.printf("\nTarefa '%s' criada com sucesso! O seu ID é: %d\n", card.getTitle(), card.getId());
        }
    }

    private void moveCardToNextColumn() throws SQLException {
        System.out.print("\nQual o ID da tarefa que você quer mover para a próxima coluna? ");
        var cardId = scanner.nextLong();
        var boardColumnsInfo = entity.getBoardColumns().stream()
                .map(bc -> new BoardColumnInfoDTO(bc.getId(), bc.getOrder(), bc.getKind()))
                .toList();
        try (var connection = getConnection()) {
            new CardService(connection).moveToNextColumn(cardId, boardColumnsInfo);
            System.out.println("Tarefa movida com sucesso!");
        } catch (RuntimeException ex) {
            System.out.println("Erro: " + ex.getMessage());
        }
    }

    private void blockCard() throws SQLException {
        System.out.print("\nQual o ID da tarefa que deseja bloquear? ");
        var cardId = scanner.nextLong();
        System.out.print("Por qual motivo esta tarefa será bloqueada? ");
        var reason = scanner.next();
        var boardColumnsInfo = entity.getBoardColumns().stream()
                .map(bc -> new BoardColumnInfoDTO(bc.getId(), bc.getOrder(), bc.getKind()))
                .toList();
        try (var connection = getConnection()) {
            new CardService(connection).block(cardId, reason, boardColumnsInfo);
            System.out.println("Tarefa bloqueada com sucesso!");
        } catch (RuntimeException ex) {
            System.out.println("Erro: " + ex.getMessage());
        }
    }

    private void unblockCard() throws SQLException {
        System.out.print("\nQual o ID da tarefa que deseja desbloquear? ");
        var cardId = scanner.nextLong();
        System.out.print("Qual a justificação para o desbloqueio? ");
        var reason = scanner.next();
        try (var connection = getConnection()) {
            new CardService(connection).unblock(cardId, reason);
            System.out.println("Tarefa desbloqueada com sucesso!");
        } catch (RuntimeException ex) {
            System.out.println("Erro: " + ex.getMessage());
        }
    }

    private void cancelCard() throws SQLException {
        System.out.print("\nQual o ID da tarefa que deseja cancelar? ");
        var cardId = scanner.nextLong();
        var cancelColumn = entity.getCancelColumn();
        try (var connection = getConnection()) {
            new CardService(connection).cancel(cardId, cancelColumn.getId());
            System.out.println("Tarefa cancelada com sucesso!");
        } catch (RuntimeException ex) {
            System.out.println("Erro: " + ex.getMessage());
        }
    }

    private void showBoard() throws SQLException {
        try (var connection = getConnection()) {
            var optional = new BoardQueryService(connection).showBoardDetails(entity.getId());
            optional.ifPresent(b -> {
                System.out.printf("\n--- Visão Geral do Quadro: %s ---\n", b.name());
                b.columns().forEach(c ->
                        System.out.printf("Coluna: %-20s | Tipo: %-7s | Tarefas: %d\n", c.name(), c.kind(), c.cardsAmount())
                );
                System.out.println("-------------------------------------------------");
            });
        }
    }

    private void showCard() throws SQLException {
        System.out.print("\nQual o ID da tarefa que deseja visualizar? ");
        var selectedCardId = scanner.nextLong();
        try (var connection = getConnection()) {
            var dao = new CardDAO(connection);
            dao.findById(selectedCardId)
                    .ifPresentOrElse(
                            c -> {
                                System.out.println("\n--- Detalhes da Tarefa ---");
                                System.out.printf("ID: %d\n", c.id());
                                System.out.printf("Título: %s\n", c.title());
                                System.out.printf("Descrição: %s\n", c.description());
                                System.out.printf("Data de Criação: %s\n", c.createdAt().toLocalDate());
                                System.out.printf("Coluna Atual: %s\n", c.columnName());
                                System.out.println(c.blocked() ?
                                        "Status: Bloqueado | Motivo: " + c.blockReason() :
                                        "Status: Desbloqueado");
                                System.out.printf("Total de Bloqueios no Histórico: %d\n", c.blocksAmount());
                                System.out.println("--------------------------");
                            },
                            () -> System.out.printf("Erro: Não existe uma tarefa com o ID %s.\n", selectedCardId));
        }
    }

    private void showBlockReport() throws SQLException {
        System.out.print("\nQual o ID da tarefa para gerar o relatório de bloqueios? ");
        var cardId = scanner.nextLong();

        try (var connection = getConnection()) {
            var service = new BoardQueryService(connection);
            var report = service.generateBlockReport(cardId);

            if (report.isEmpty()) {
                System.out.println("Nenhum histórico de bloqueio encontrado para esta tarefa.");
                return;
            }

            System.out.println("\n--- Relatório de Bloqueios para a Tarefa " + cardId + " ---");
            report.forEach(r -> {
                System.out.println("  Motivo do Bloqueio: " + r.blockReason());
                System.out.println("  Motivo do Desbloqueio: " + (r.unblockReason() != null ? r.unblockReason() : "Ainda bloqueado"));
                String durationText = r.duration() != null ? r.duration().toMinutes() + " minutos" : "Em andamento";
                System.out.println("  Duração: " + durationText);
                System.out.println("  ------------------------------------");
            });
        }
    }

    private void editCard() throws SQLException {
        System.out.print("\nQual o ID da tarefa que deseja editar? ");
        var cardId = scanner.nextLong();

        System.out.print("Qual o novo título para a tarefa? ");
        var newTitle = scanner.next();

        System.out.print("Qual a nova descrição para a tarefa? ");
        var newDescription = scanner.next();

        try (var connection = getConnection()) {
            new CardService(connection).update(cardId, newTitle, newDescription);
            System.out.println("Tarefa atualizada com sucesso!");
        } catch (RuntimeException ex) {
            System.out.println("Erro: " + ex.getMessage());
        }
    }
}