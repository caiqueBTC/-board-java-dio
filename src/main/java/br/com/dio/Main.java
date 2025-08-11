package br.com.dio;

import br.com.dio.persistence.migration.MigrationStrategy;
import br.com.dio.ui.MainMenu;

public class Main {
    public static void main(String[] args) {
        new MigrationStrategy().executeMigration();
        new MainMenu().execute();
    }
}