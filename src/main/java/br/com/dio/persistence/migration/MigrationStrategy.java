package br.com.dio.persistence.migration;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import java.sql.SQLException;
import static br.com.dio.persistence.config.ConnectionConfig.getConnection;

public class MigrationStrategy {

    public void executeMigration() {
        try (var connection = getConnection(); var jdbcConnection = new JdbcConnection(connection)) {
            var liquibase = new Liquibase(
                    "db/changelog/db.changelog-master.yml",
                    new ClassLoaderResourceAccessor(),
                    jdbcConnection
            );
            liquibase.update();
        } catch (SQLException | LiquibaseException e) {
            throw new RuntimeException("Erro ao executar a migração do banco de dados", e);
        }
    }
}