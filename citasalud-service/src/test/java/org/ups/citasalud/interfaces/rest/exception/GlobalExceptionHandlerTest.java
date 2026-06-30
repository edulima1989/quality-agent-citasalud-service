package org.ups.citasalud.interfaces.rest.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.ups.citasalud.domain.exception.CitaSaludDomainException;
import org.ups.citasalud.domain.exception.FranjaNoDisponibleException;
import org.ups.citasalud.interfaces.rest.dto.ErrorResponseDto;
import org.ups.citasalud.interfaces.rest.dto.FranjaNoDisponibleResponseDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleFranjaNoDisponible_retorna409ConCodigo() {
        FranjaNoDisponibleException ex = new FranjaNoDisponibleException("ocupada", List.of());
        ResponseEntity<FranjaNoDisponibleResponseDto> resp = handler.handleFranjaNoDisponible(ex);

        assertEquals(HttpStatus.CONFLICT, resp.getStatusCode());
        assertEquals("FRANJA_NO_DISPONIBLE", resp.getBody().codigo());
    }

    @Test
    void handleDomainException_retorna400() {
        CitaSaludDomainException ex = new CitaSaludDomainException("error de dominio");
        ResponseEntity<ErrorResponseDto> resp = handler.handleDomainException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("DOMAIN_ERROR", resp.getBody().codigo());
    }

    @Test
    void handleIllegalArgument_retorna400() {
        ResponseEntity<ErrorResponseDto> resp = handler.handleIllegalArgument(
            new IllegalArgumentException("arg inválido")
        );

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("INVALID_REQUEST", resp.getBody().codigo());
    }

    @Test
    void handleGeneric_retorna500() {
        ResponseEntity<ErrorResponseDto> resp = handler.handleGeneric(
            new RuntimeException("error inesperado")
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertEquals("INTERNAL_ERROR", resp.getBody().codigo());
    }
}
