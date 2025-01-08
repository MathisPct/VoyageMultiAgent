package org.polytech.agent;

import org.polytech.agent.strategy.NegociationContext;
import org.polytech.agent.strategy.NegociationStrategy;

import java.time.LocalDateTime;

public class Buyer extends Agent implements Runnable {
    private final Ticket ticket;
    private double lastOfferPrice = 0.0;
    private double initialOffer;
    private double budget;
    private Provider currentProvider;
    private int negotiationRounds = 0;
    private final int MAX_ROUNDS = 8;

    public Buyer(double budget, Ticket ticket) {
        super();
        this.interest = 3;
        this.budget = budget;
        this.ticket = ticket;
        Agent.buyers.add(this);
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
                offerPrice = calculateInitialOffer(new NegociationContext(budget, 0, lastOfferPrice, initialOffer, this.interest, this.ticket));
                lastOfferPrice = offerPrice;
                initialOffer = offerPrice;
                Agent.publishToMessageQueue(this.currentProvider, 
                    new Message(this, this.currentProvider,
                              new Offer(offerPrice, this.ticket, TypeOffer.INITIAL),
                              LocalDateTime.now()));
            }

            Message response = waitUntilReceiveMessage(this);
            negotiationRounds++;

            switch (response.getOffer().getTypeOffer()) {
                case AGAINST_PROPOSITION -> {
                    double providerOffer = response.getOffer().getPrice();
                    if (this.budget >= providerOffer && this.shouldAcceptOffer(new NegociationContext(budget, providerOffer, lastOfferPrice, initialOffer, this.interest, this.ticket))) {
                        System.out.println("Buyer accepts offer of " + providerOffer);
                        Agent.publishToMessageQueue(this.currentProvider,
                                new Message(this, this.currentProvider,
                                          new Offer(providerOffer, this.ticket, TypeOffer.ACCEPT),
                                          LocalDateTime.now()));
                        return;
                    }
                    
                    double counterOffer = calculateCounterOffer(new NegociationContext(budget, providerOffer, lastOfferPrice, initialOffer, this.interest, this.ticket));
                    if (counterOffer <= budget) {
                        lastOfferPrice = counterOffer;
                        System.out.println("Buyer counter-offers " + counterOffer);
                        Agent.publishToMessageQueue(this.currentProvider,
                                new Message(this, this.currentProvider,
                                          new Offer(counterOffer, this.ticket, TypeOffer.AGAINST_PROPOSITION),
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
                case END_NEGOCIATION -> {
                    System.out.println("Buyer refused the offer.");
                    checkIfSuperiorToMaxRound();
                }
                case INITIAL -> {
                    System.out.println("Buyer received an INITIAL offer.");
                }
            }
        }
        Agent.publishToMessageQueue(this.currentProvider,
                new Message(this, this.currentProvider,
                        new Offer(-1, this.ticket, TypeOffer.END_NEGOCIATION),
                        LocalDateTime.now()));
        System.out.println("Buyer has concluded its negotiations.");
    }

    private void checkIfSuperiorToMaxRound() {
        if (negotiationRounds >= MAX_ROUNDS) {
            System.out.println("Buyer has reached the maximum number of negotiation rounds.");
        }
    }
}
