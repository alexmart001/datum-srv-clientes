package br.com.datum.exception;

/**
 * Lançada quando a chamada a um serviço externo (ex.: datum-srv-score-cliente)
 * falha - seja por indisponibilidade, timeout, ou resposta de erro.
 */
public class ExternalServiceException extends RuntimeException {
    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
