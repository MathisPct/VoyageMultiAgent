package org.polytech.agent.strategy;

public interface NegociationStrategy {
    double calculateInitialOffer(NegociationContext negociationContext);
    double calculateCounterOffer(NegociationContext negociationContext);
    boolean shouldAcceptOffer(NegociationContext negociationContext);
}
