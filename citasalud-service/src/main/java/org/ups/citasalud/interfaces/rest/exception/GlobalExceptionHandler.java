package org.ups.citasalud.interfaces.rest.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.ups.citasalud.domain.exception.CitaSaludDomainException;
import org.ups.citasalud.domain.exception.FranjaNoDisponibleException;
import org.ups.citasalud.interfaces.rest.dto.ErrorResponseDto;
import org.ups.citasalud.interfaces.rest.dto.FranjaDisponibleInfoDto;
import org.ups.citasalud.interfaces.rest.dto.FranjaNoDisponibleResponseDto;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(FranjaNoDisponibleException.class)
    public ResponseEntity<FranjaNoDisponibleResponseDto> handleFranjaNoDisponible(
            FranjaNoDisponibleException ex) {
        List<FranjaDisponibleInfoDto> alternativas = ex.getAlternativas().stream()
            .map(f -> new FranjaDisponibleInfoDto(f.getId(), f.getFecha(), f.getHoraInicio(), f.getHoraFin()))
            .toList();

        FranjaNoDisponibleResponseDto body = new FranjaNoDisponibleResponseDto(
            "FRANJA_NO_DISPONIBLE",
            ex.getMessage(),
            alternativas
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(CitaSaludDomainException.class)
    public ResponseEntity<ErrorResponseDto> handleDomainException(CitaSaludDomainException ex) {
        return ResponseEntity.badRequest().body(
            new ErrorResponseDto("DOMAIN_ERROR", ex.getMessage(), Instant.now())
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(
            new ErrorResponseDto("INVALID_REQUEST", ex.getMessage(), Instant.now())
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGeneric(Exception ex) {
        log.error("Error no controlado: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            new ErrorResponseDto("INTERNAL_ERROR", "Error interno del servidor", Instant.now())
        );
    }
}
