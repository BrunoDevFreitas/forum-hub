package com.tec.forumhub.infra.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<DadosErroValidacao>> tratarErroValidacao(
            MethodArgumentNotValidException ex) {

        var erros = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(DadosErroValidacao::new)
                .toList();

        return ResponseEntity.badRequest().body(erros);
    }

    private record DadosErroValidacao(String campo, String mensagem) {
        DadosErroValidacao(FieldError fieldError) {
            this(fieldError.getField(), fieldError.getDefaultMessage());
        }
    }
}
