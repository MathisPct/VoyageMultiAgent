package org.polytech.messaging;

import org.polytech.agent.Agent;
import org.polytech.agent.Offer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Message {
    private String id;
    private Agent issuer;
    private Agent receiver;
    private Offer offer;
    private LocalDateTime dateEmission;
    private boolean read = false;

    public Message(Agent issuer, Agent receiver, Offer offer, LocalDateTime dateEmission) {
        this.id = UUID.randomUUID().toString();
        this.issuer = issuer;
        this.receiver = receiver;
        this.offer = offer;
        this.dateEmission = dateEmission;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isRead() {
        return read;
    }

    public String getId() {
        return id;
    }

    public Agent getIssuer() {
        return issuer;
    }

    public Agent getReceiver() {
        return receiver;
    }

    public Offer getOffer() {
        return offer;
    }

    public LocalDateTime getDateEmission() {
        return dateEmission;
    }

    @Override
    public String toString() {
        return dateEmission.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss.SSS")) +  " Emetteur : " + issuer.getName() + " - Destinataire : " + receiver.getName() + " - Offre : " + offer.toString();
    }
}
