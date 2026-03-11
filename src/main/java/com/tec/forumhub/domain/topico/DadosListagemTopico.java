package com.tec.forumhub.domain.topico;

import java.time.LocalDateTime;

public record DadosListagemTopico(
        Long id,
        String titulo,
        String autor,
        String curso,
        LocalDateTime dataCriacao,
        StatusTopico status
) {
    public DadosListagemTopico(Topico topico) {
        this(
                topico.getId(),
                topico.getTitulo(),
                topico.getAutor(),
                topico.getCurso(),
                topico.getDataCriacao(),
                topico.getStatus()
        );
    }
}
