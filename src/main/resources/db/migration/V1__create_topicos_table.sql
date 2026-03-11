CREATE TABLE topicos (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    titulo       VARCHAR(255) NOT NULL,
    mensagem     TEXT         NOT NULL,
    data_criacao DATETIME     NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'NAO_RESPONDIDO',
    autor        VARCHAR(255) NOT NULL,
    curso        VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);
