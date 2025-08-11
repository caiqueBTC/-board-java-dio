package br.com.dio.ui;

import br.com.dio.persistence.entity.BoardColumnEntity;
import br.com.dio.persistence.entity.BoardEntity;
import br.com.dio.service.BoardQueryService;
import br.com.dio.service.BoardService;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import static br.com.dio.persistence.config.ConnectionConfig.getConnection;
import static br.com.dio.persistence.entity.BoardColumnKindEnum.*;

public class MainMenu {
    private final Scanner scanner = new Scanner(System.in).useDelimiter("\n");

    public void execute() {
        try {
            System.out.println("--- Bem-vindo ao seu Gestor de Projetos (Quadros Kanban) ---");
            while (true) {
                System.out.println("\n== MENU PRINCIPAL ==");
                System.out.println("1 - Criar um novo Quadro (Projeto)");
                System.out.println("2 - Listar todos os Quadros");
                System.out.println("3 - Entrar num Quadro (pelo ID)");
                System.out.println("4 - Excluir um Quadro (pelo ID)");
                System.out.println("5 - Sair da Aplicação");
                System.out.print("Escolha uma opção: ");
                var option = scanner.nextInt();
                switch (option) {
                    case 1 -> createBoard();
                    case 2 -> listAllBoards();
                    case 3 -> selectBoard();
                    case 4 -> deleteBoard();
                    case 5 -> {
                        System.out.println("Até logo!");
                        System.exit(0);
                    }
                    default -> System.out.println("Opção inválida. Por favor, tente novamente.");
                }
            }
        } catch (SQLException ex) {
            System.err.println("Ocorreu um erro crítico na base de dados. A aplicação será encerrada.");
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private void listAllBoards() throws SQLException {
        try (var connection = getConnection()) {
            var service = new BoardQueryService(connection);
            var boards = service.findAllBoards();

            if (boards.isEmpty()) {
                System.out.println("\nNenhum quadro foi criado ainda.");
            } else {
                System.out.println("\n--- Quadros Existentes ---");
                boards.forEach(board -> System.out.printf("ID: %d | Nome: %s\n", board.getId(), board.getName()));
                System.out.println("--------------------------");
            }
        }
    }

    private void createBoard() throws SQLException {
        var entity = new BoardEntity();
        System.out.println("\n--- Criando um novo Quadro ---");
        System.out.print("Qual é o nome do seu projeto? ");
        entity.setName(scanner.next());

        System.out.print("Quantas colunas de 'Tarefas Pendentes' você quer entre a coluna Inicial e a Final? (Digite 0 se não quiser nenhuma): ");
        var additionalColumns = scanner.nextInt();

        List<BoardColumnEntity> columns = new ArrayList<>();

        System.out.print("Qual o nome da sua primeira coluna (Ex: A Fazer)? ");
        var initialColumnName = scanner.next();
        var initialColumn = new BoardColumnEntity();
        initialColumn.setName(initialColumnName);
        initialColumn.setKind(INITIAL);
        initialColumn.setOrder(0);
        columns.add(initialColumn);

        for (int i = 0; i < additionalColumns; i++) {
            System.out.printf("Qual o nome da %dª coluna pendente (Ex: Em Progresso, Validação)? ", i + 1);
            var pendingColumnName = scanner.next();
            var pendingColumn = new BoardColumnEntity();
            pendingColumn.setName(pendingColumnName);
            pendingColumn.setKind(PENDING);
            pendingColumn.setOrder(i + 1);
            columns.add(pendingColumn);
        }

        System.out.print("Qual o nome da sua coluna final (Ex: Concluído)? ");
        var finalColumnName = scanner.next();
        var finalColumn = new BoardColumnEntity();
        finalColumn.setName(finalColumnName);
        finalColumn.setKind(FINAL);
        finalColumn.setOrder(additionalColumns + 1);
        columns.add(finalColumn);

        System.out.print("E o nome da coluna para tarefas canceladas (Ex: Arquivado)? ");
        var cancelColumnName = scanner.next();
        var cancelColumn = new BoardColumnEntity();
        cancelColumn.setName(cancelColumnName);
        cancelColumn.setKind(CANCEL);
        cancelColumn.setOrder(additionalColumns + 2);
        columns.add(cancelColumn);

        entity.setBoardColumns(columns);
        try (var connection = getConnection()) {
            var service = new BoardService(connection);
            service.insert(entity);
            System.out.printf("\nQuadro '%s' criado com sucesso! O seu ID é: %d\n", entity.getName(), entity.getId());
        }
    }

    private void selectBoard() throws SQLException {
        System.out.print("\nPor favor, informe o ID do quadro que deseja aceder: ");
        var id = scanner.nextLong();
        try (var connection = getConnection()) {
            var queryService = new BoardQueryService(connection);
            var optional = queryService.findById(id);
            optional.ifPresentOrElse(
                    b -> new BoardMenu(b).execute(),
                    () -> System.out.printf("Erro: Não foi encontrado nenhum quadro com o ID %s.\n", id)
            );
        }
    }

    private void deleteBoard() throws SQLException {
        System.out.print("\nATENÇÃO: Esta ação é irreversível.\nQual o ID do quadro que deseja excluir? ");
        var id = scanner.nextLong();
        try (var connection = getConnection()) {
            var service = new BoardService(connection);
            if (service.delete(id)) {
                System.out.printf("O quadro com ID %s foi excluído com sucesso.\n", id);
            } else {
                System.out.printf("Erro: Não foi encontrado nenhum quadro com o ID %s.\n", id);
            }
        }
    }
}