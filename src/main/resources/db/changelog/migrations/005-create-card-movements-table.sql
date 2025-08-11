--liquibase formatted sql
--changeset caiqueBTC:5
CREATE TABLE CARD_MOVEMENTS(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_id BIGINT NOT NULL,
    source_column_id BIGINT, -- Pode ser nulo para a primeira entrada
    destination_column_id BIGINT NOT NULL,
    moved_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT cards__card_movements_fk FOREIGN KEY (card_id) REFERENCES CARDS(id) ON DELETE CASCADE
) ENGINE=InnoDB;