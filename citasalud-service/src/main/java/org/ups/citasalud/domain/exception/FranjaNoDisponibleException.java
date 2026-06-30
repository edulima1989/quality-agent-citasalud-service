package org.ups.citasalud.domain.exception;

import org.ups.citasalud.domain.model.FranjaHoraria;

import java.util.List;

public class FranjaNoDisponibleException extends CitaSaludDomainException {

    private final List<FranjaHoraria> alternativas;

    public FranjaNoDisponibleException(String message, List<FranjaHoraria> alternativas) {
        super(message);
        this.alternativas = List.copyOf(alternativas);
    }

    public List<FranjaHoraria> getAlternativas() {
        return alternativas;
    }
}
