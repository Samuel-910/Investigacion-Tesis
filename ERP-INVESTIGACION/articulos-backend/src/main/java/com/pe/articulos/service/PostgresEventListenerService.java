package com.pe.articulos.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostgresEventListenerService {

    private final ColaEventoRepository colaEventoRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final DataSource dataSource;
    private Thread listenerThread;

    @PostConstruct
    public void init() {
        listenerThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try (Connection conn = dataSource.getConnection()) {
                    PGConnection pgConn = conn.unwrap(PGConnection.class);
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute("LISTEN nuevo_evento_outbox");
                    }
                    while (!Thread.currentThread().isInterrupted()) {
                        PGNotification[] notifications = pgConn.getNotifications(10000);
                        if (notifications != null && notifications.length > 0) {
                            processPendingEvents();
                        }
                    }
                } catch (Exception e) {
                    log.error("Error en listener de PostgreSQL. Reconectando en 10s...", e);
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
        listenerThread.setName("PostgresOutboxListener");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    @PreDestroy
    public void destroy() {
        if (listenerThread != null) {
            listenerThread.interrupt();
        }
    }

    @Scheduled(fixedDelay = 30000)
    public void pollingFallback() {
        processPendingEvents();
    }

    @Transactional
    public synchronized void processPendingEvents() {
        List<ColaEvento> pendientes = colaEventoRepository.findByEstadoOrderByFechaCreacionAsc("PENDIENTE");
        for (ColaEvento evento : pendientes) {
            try {
                eventPublisher.publishEvent(new OutboxEvent(this, evento));
                evento.setEstado("PROCESADO");
                evento.setFechaProcesado(LocalDateTime.now());
                colaEventoRepository.save(evento);
            } catch (Exception e) {
                log.error("Error al procesar evento " + evento.getId(), e);
                evento.setEstado("ERROR");
                colaEventoRepository.save(evento);
            }
        }
    }
}
