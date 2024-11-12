package org.polytech.agent;

import java.time.LocalDateTime;

public class Buyer extends Agent implements Runnable {
    /**
     * Max increase price percent per negotiation round
     */
    private static final double MAX_PRICE_INCREASE_PERCENT = 0.10;
    private static final double INITIAL_OFFER_PERCENT = 0.80;
    private double lastOfferPrice = 0.0;
    private double budget;
    private Provider currentProvider;
    private int negotiationRounds = 0;
    private final int MAX_ROUNDS = 8;

    public Buyer(double budget) {
        super();
        this.budget = budget;
        Agent.buyers.add(this);
    }

    private double calculateInitialOffer() {
        return budget * INITIAL_OFFER_PERCENT;
    }

    private double calculateCounterOffer(double providerOffer) {
        double maxIncrease = lastOfferPrice * MAX_PRICE_INCREASE_PERCENT;
        return Math.min(budget, lastOfferPrice + maxIncrease);
    }

    private boolean shouldAcceptOffer(double providerOffer) {
        return providerOffer <= budget && 
               (providerOffer - lastOfferPrice <= lastOfferPrice * MAX_PRICE_INCREASE_PERCENT);
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
                offerPrice = calculateInitialOffer();
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
                    if (shouldAcceptOffer(providerOffer)) {
                        System.out.println("Buyer accepts offer of " + providerOffer);
                        Agent.publishToMessageQueue(this.currentProvider,
                                new Message(this, this.currentProvider,
                                          new Offer(providerOffer, TypeOffer.ACCEPT),
                                          LocalDateTime.now()));
                        return;
                    }
                    
                    double counterOffer = calculateCounterOffer(providerOffer);
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
                    activeNegotiation();
                }
                case REFUSE -> {
                    System.out.println("Buyer refused the offer.");
                    activeNegotiation();
                }
                case INITIAL -> {
                    System.out.println("Buyer received an INITIAL offer.");
                }
            }
        }
        System.out.println("Buyer has concluded its negotiations.");
    }

    private void activeNegotiation() {
        if (negotiationRounds >= MAX_ROUNDS) {
            System.out.println("Buyer has reached the maximum number of negotiation rounds.");
        }
    }
}
