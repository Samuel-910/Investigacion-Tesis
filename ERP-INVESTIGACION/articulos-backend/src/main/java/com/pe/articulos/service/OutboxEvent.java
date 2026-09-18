package com.pe.articulos.service;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OutboxEvent extends ApplicationEvent {
    private final ColaEvento colaEvento;

    public OutboxEvent(Object source, ColaEvento colaEvento) {
        super(source);
        this.colaEvento = colaEvento;
    }
}
