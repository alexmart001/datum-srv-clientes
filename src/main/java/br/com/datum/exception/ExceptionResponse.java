package br.com.datum.exception;

import java.util.Date;

public record ExceptionResponse(Date timestamp, String message, String details) {
}
