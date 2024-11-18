package org.polytech.agent;

import org.polytech.agent.strategy.NegociationContext;
import org.polytech.agent.strategy.NegociationStrategy;

import java.time.LocalDateTime;

public class Buyer extends Agent implements Runnable, NegociationStrategy {
    private double lastOfferPrice = 0.0;
    private double budget;
    private Provider currentProvider;
    private int negotiationRounds = 0;
    private final int MAX_ROUNDS = 8;

    private NegociationStrategy negociationStrategy;

    public Buyer(double budget) {
        super();
        this.budget = budget;
        Agent.buyers.add(this);
    }

    public void setNegociationStrategy(NegociationStrategy negociationStrategy) {
        this.negociationStrategy = negociationStrategy;
    }

    @Override
    public double calculateInitialOffer(NegociationContext negociationContext) {
        return this.negociationStrategy.calculateInitialOffer(negociationContext);
    }

    @Override
    public double calculateCounterOffer(NegociationContext negociationContext) {
        return this.negociationStrategy.calculateCounterOffer(negociationContext);
    }

    @Override
    public boolean shouldAcceptOffer(NegociationContext negociationContext) {
        return this.negociationStrategy.shouldAcceptOffer(negociationContext);
    }

    @Override
    public void run() {
        while (negotiationRounds < MAX_ROUNDS) {
            if (currentProvider == null && !Agent.providers.isEmpty()) {
                this.currentProvider = Agent.providers.get(0);
                System.out.println("Buyer selected a provider");
            }

            double offerPrice;
            if (negotiationRounds == 0) {
                offerPrice = calculateInitialOffer(new NegociationContext(budget, 0, lastOfferPrice));
                lastOfferPrice = offerPrice;
                Agent.publishToMessageQueue(this.currentProvider, 
                    new Message(this, this.currentProvider, 
                              new Offer(offerPrice, TypeOffer.INITIAL), 
                              LocalDateTime.now()));
            }

            Message response = waitUntilReceiveMessage(this);
            negotiationRounds++;

            switch (response.getOffer().getTypeOffer()) {
                case AGAINST_PROPOSITION -> {
                    double providerOffer = response.getOffer().getPrice();
                    if (this.budget <= providerOffer && this.shouldAcceptOffer(new NegociationContext(budget, providerOffer, lastOfferPrice))) {
                        System.out.println("Buyer accepts offer of " + providerOffer);
                        Agent.publishToMessageQueue(this.currentProvider,
                                new Message(this, this.currentProvider,
                                          new Offer(providerOffer, TypeOffer.ACCEPT),
                                          LocalDateTime.now()));
                        return;
                    }
                    
                    double counterOffer = calculateCounterOffer(new NegociationContext(budget, providerOffer, lastOfferPrice));
                    if (counterOffer <= budget) {
                        lastOfferPrice = counterOffer;
                        System.out.println("Buyer counter-offers " + counterOffer);
                        Agent.publishToMessageQueue(this.currentProvider,
                                new Message(this, this.currentProvider,
                                          new Offer(counterOffer, TypeOffer.AGAINST_PROPOSITION),
                                          LocalDateTime.now()));
                    } else {
                        System.out.println("Buyer cannot afford counter-offer");
                        negotiationRounds = MAX_ROUNDS;
                    }
                }
                case ACCEPT -> {
                    System.out.println("Buyer accepted the offer.");
                    checkIfSuperiorToMaxRound();
                }
                case REFUSE -> {
                    System.out.println("Buyer refused the offer.");
                    checkIfSuperiorToMaxRound();
                }
                case INITIAL -> {
                    System.out.println("Buyer received an INITIAL offer.");
                }
            }
        }
        System.out.println("Buyer has concluded its negotiations.");
    }

    private void checkIfSuperiorToMaxRound() {
        if (negotiationRounds >= MAX_ROUNDS) {
            System.out.println("Buyer has reached the maximum number of negotiation rounds.");
        }
    }
}
