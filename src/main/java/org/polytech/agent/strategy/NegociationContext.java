package org.polytech.agent.strategy;

import org.polytech.agent.Ticket;

public record NegociationContext(double budget, double offer, double lastOfferPrice, double initialOffer, int interest, Ticket ticket) {
}
