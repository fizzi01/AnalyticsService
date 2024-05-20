package it.unisalento.pasproject.analyticsservice.service;

import org.springframework.stereotype.Service;

@Service
public class AnalyticsDataHandler {
    /**
     * La classe gestisce la ricezione dei dati necessari al servizio.
     * In particolare ha bisogno di ricevere:
     * - i dati dei membri
     * - - Potenza di calcolo
     * - - Consumo di energia per ora
     * - - Tempo di lavoro nella task assegnata
     * <p>
     * Questi valori vengono ricevuti per ogni assegnazione di una risorsa del membro
     *
     * - i dati dell'utente
     * - - Quando la task è stata completata ( Risorse dei membri assegnate ) -> Si somma la potenza di calcolo e il consumo di energia per ora ( * tempo di lavoro )
     * - - Quando la task viene avviata (Per tenere traccia delle task submitted)
     */
}
