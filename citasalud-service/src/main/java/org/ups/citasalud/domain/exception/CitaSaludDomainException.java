package org.ups.citasalud.domain.exception;

public class CitaSaludDomainException extends RuntimeException {

    public CitaSaludDomainException(String message) {
        super(message);
    }

    public CitaSaludDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
