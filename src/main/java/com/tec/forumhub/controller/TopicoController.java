package com.tec.forumhub.controller;

import com.tec.forumhub.domain.topico.*;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/topicos")
public class TopicoController {

    @Autowired
    private TopicoRepository topicoRepository;

    @PostMapping
    @Transactional
    public ResponseEntity<DadosDetalhamentoTopico> cadastrar(
            @RequestBody @Valid DadosCadastroTopico dados,
            UriComponentsBuilder uriBuilder) {

        if (topicoRepository.existsByTituloAndMensagem(dados.titulo(), dados.mensagem())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Já existe um tópico com o mesmo título e mensagem.");
        }

        var topico = new Topico(dados);
        topicoRepository.save(topico);

        var uri = uriBuilder.path("/topicos/{id}").buildAndExpand(topico.getId()).toUri();
        return ResponseEntity.created(uri).body(new DadosDetalhamentoTopico(topico));
    }

    @GetMapping
    public ResponseEntity<List<DadosListagemTopico>> listar() {
        var lista = topicoRepository.findAll()
                .stream()
                .map(DadosListagemTopico::new)
                .toList();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DadosDetalhamentoTopico> detalhar(@PathVariable Long id) {
        var topico = topicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Tópico não encontrado."));
        return ResponseEntity.ok(new DadosDetalhamentoTopico(topico));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<DadosDetalhamentoTopico> atualizar(
            @PathVariable Long id,
            @RequestBody DadosAtualizacaoTopico dados) {

        var topico = topicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Tópico não encontrado."));

        if (dados.titulo() != null && dados.mensagem() != null
                && topicoRepository.existsByTituloAndMensagem(dados.titulo(), dados.mensagem())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Já existe um tópico com o mesmo título e mensagem.");
        }

        topico.atualizar(dados);
        return ResponseEntity.ok(new DadosDetalhamentoTopico(topico));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        if (!topicoRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Tópico não encontrado.");
        }
        topicoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
