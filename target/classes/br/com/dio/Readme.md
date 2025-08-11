# Gestor de Quadros Kanban (Task Board) - Desafio DIO & GFT

Este projeto é uma aplicação de console em Java que implementa um sistema de gestão de tarefas no estilo Kanban. Foi desenvolvido como solução para o desafio de programação proposto pela Digital Innovation One (DIO) em parceria com a GFT.

A aplicação permite a criação e gestão de múltiplos quadros (projetos), com colunas personalizáveis e cartões (tarefas) que podem ser movidos, editados, bloqueados e cancelados, com todos os dados a serem guardados de forma persistente numa base de dados MySQL.
Como a Aplicação Funciona

O sistema baseia-se em dois conceitos principais:

    Quadro (Board): Representa um projeto ou uma área de trabalho. Cada quadro tem um nome e um conjunto de colunas que definem o fluxo de trabalho.

    Cartão (Card): Representa uma tarefa específica dentro de um quadro. Cada cartão possui um título, uma descrição e move-se através das colunas para indicar o seu progresso (ex: de "A Fazer" para "Em Progresso" e, finalmente, para "Concluído").

A interação com o utilizador é feita inteiramente através do terminal, com menus claros e intuitivos que guiam o utilizador através das várias funcionalidades.
Tecnologias e Escolhas Técnicas

    Java 21: Utilizado como linguagem principal, aproveitando funcionalidades modernas como records para DTOs, var para inferência de tipos e a API de Streams.

    MySQL: Base de dados relacional escolhida para garantir a persistência e a integridade dos dados, conforme solicitado no desafio.

    Liquibase: Ferramenta de controlo de versão para o esquema da base de dados. Todas as tabelas são criadas e atualizadas através de migrations em SQL, o que torna a configuração do ambiente automática e à prova de erros.

    Maven: Escolhi o Maven como ferramenta de gestão de dependências e de build. Embora o desafio original sugerisse Gradle, optei pelo Maven por ser uma ferramenta extremamente robusta, amplamente utilizada no mercado de trabalho e com a qual tenho mais familiaridade. Ele simplifica a gestão de bibliotecas e a criação do pacote final executável.

    Lombok: Utilizado para reduzir código repetitivo (boilerplate) nas classes de entidade e DTOs, tornando o código mais limpo e focado na lógica de negócio.

Funcionalidades Implementadas

A aplicação implementa todos os requisitos obrigatórios e opcionais do desafio, para além de funcionalidades extra que melhoram a experiência do utilizador.

    Gestão Completa de Quadros:

        Criar, listar, selecionar e excluir quadros.

        Colunas personalizáveis (Inicial, Pendente, Final, Cancelar).

    Gestão Completa de Tarefas (Cartões):

        Criar, editar, mover, bloquear, desbloquear e cancelar tarefas.

        Ao criar um quadro ou cartão, o ID gerado é imediatamente exibido ao utilizador.

    Relatórios e Histórico:

        Histórico de Movimentação: Cada movimento de um cartão entre colunas é registado na base de dados.

        Relatório de Bloqueios: É possível gerar um relatório detalhado para qualquer tarefa, mostrando o histórico de bloqueios, os motivos e a duração de cada um.

Como Executar a Aplicação
Pré-requisitos

    Java 21 (ou superior) instalado.

    Maven instalado.

    MySQL Server instalado e em execução.

Passos para a Execução

    Configurar a Base de Dados:

        Aceda ao seu servidor MySQL e execute os seguintes comandos para criar a base de dados e o utilizador necessários:

        CREATE DATABASE board;
        CREATE USER 'board'@'localhost' IDENTIFIED BY 'board';
        GRANT ALL PRIVILEGES ON board.* TO 'board'@'localhost';
        FLUSH PRIVILEGES;

    Compilar o Projeto:

        Abra um terminal na pasta raiz do projeto e execute o comando do Maven. Ele irá descarregar as dependências, compilar o código e criar um ficheiro .jar executável na pasta target/.

        mvn clean package

    Executar a Aplicação:

        Após a compilação ser bem-sucedida, execute o ficheiro JAR com o seguinte comando:

        java -jar target/board-1.0-SNAPSHOT-jar-with-dependencies.jar

        A aplicação será iniciada no seu terminal, e o menu principal será exibido.

Dificuldades e Soluções

Durante o desenvolvimento, enfrentei alguns desafios que foram importantes para a minha aprendizagem:

    Erros de Compilação em Cascata: Inicialmente, tive vários erros de compilação com a mensagem bad source file. Descobri que a causa era um erro ao copiar/colar em algumas classes centrais (como as Entidades e os DAOs). Ao corrigir esses ficheiros-chave, os outros erros desapareceram. A lição foi a importância de verificar o código base com atenção, pois um pequeno erro pode propagar-se por todo o projeto.

    Falhas de Usabilidade: Após implementar as funcionalidades básicas, percebi que a aplicação era funcional, mas não era intuitiva. O sistema gerava IDs para os quadros e cartões, mas não os mostrava ao utilizador, tornando impossível usar funcionalidades como "Selecionar Quadro". Solução: Modifiquei as classes da UI (MainMenu e BoardMenu) para exibir mensagens de sucesso mais informativas, incluindo sempre o ID do item recém-criado.

    Gestão de Transações: Para garantir a integridade dos dados, implementei a gestão de transações (connection.commit() e connection.rollback()) na camada de Serviço. Isto assegura que, se uma operação falhar a meio (por exemplo, ao criar um quadro e as suas colunas), nenhuma alteração parcial é guardada na base de dados.

Próximos Passos e Melhorias Futuras

Embora o projeto esteja muito completo, há sempre espaço para evoluir:

    Implementar o Relatório de Tempo por Tarefa: A base de dados já guarda todo o histórico de movimentação. O próximo passo seria criar a lógica no BoardQueryService para calcular o tempo que cada tarefa permaneceu em cada coluna e exibir essa informação num novo relatório.

    Adicionar Testes Unitários: Usar uma framework como o JUnit para criar testes automatizados para a camada de serviço, garantindo que a lógica de negócio se mantém correta à medida que o projeto cresce.

    Refatorar a Entrada de Dados: A lógica para receber dados do utilizador nas classes de UI poderia ser extraída para uma classe utilitária, tornando o código dos menus ainda mais limpo e focado apenas na apresentação das opções.