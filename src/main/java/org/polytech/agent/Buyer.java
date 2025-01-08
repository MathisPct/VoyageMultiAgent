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
    private final String buyerName;
    private boolean active;

    public Buyer(double budget, Ticket ticket, String buyerName, int interest) {
        super();
        this.interest = interest;
        this.budget = budget;
        this.ticket = ticket;
        this.buyerName = buyerName;

        this.active = true;
        Agent.buyers.add(this);
    }

    @Override
    public void run() {
        while ((negotiationRounds < MAX_ROUNDS) && active) {

            if (currentProvider == null && !Agent.providers.isEmpty()) {
                this.currentProvider = Agent.providers.get(0); // TODO: à changer plus tard
                System.out.println("[" + buyerName + "] selected provider: " + currentProvider);
            }

            // Premier round => on envoie une offre INITIAL
            if (negotiationRounds == 0) {
                double offerPrice = calculateInitialOffer(
                        new NegociationContext(
                                budget,
                                0, // prix de l'offre reçue du Provider (ici 0, car rien reçu encore)
                                lastOfferPrice,
                                initialOffer,
                                this.interest,
                                this.ticket
                        )
                );

                lastOfferPrice = offerPrice;
                initialOffer = offerPrice;

                System.out.println("[" + buyerName + "] sends INITIAL offer of: " + offerPrice);

                Agent.publishToMessageQueue(this.currentProvider,
                        new Message(this,
                                this.currentProvider,
                                new Offer(offerPrice, this.ticket, TypeOffer.INITIAL),
                                LocalDateTime.now()
                        )
                );
            }

            Message response = waitUntilReceiveMessage(this);
            negotiationRounds++;

            switch (response.getOffer().getTypeOffer()) {
                case AGAINST_PROPOSITION -> {
                    double providerOffer = response.getOffer().getPrice();
                    System.out.println("[" + buyerName + "] received AGAINST_PROPOSITION from Provider: " + providerOffer);

                    if (this.budget >= providerOffer &&
                            this.shouldAcceptOffer(new NegociationContext(
                                    budget,
                                    providerOffer,
                                    lastOfferPrice,
                                    initialOffer,
                                    this.interest,
                                    this.ticket
                            ))
                    ) {
                        System.out.println("[" + buyerName + "] accepts the offer of " + providerOffer);
                        Agent.publishToMessageQueue(
                                this.currentProvider,
                                new Message(
                                        this,
                                        this.currentProvider,
                                        new Offer(providerOffer, this.ticket, TypeOffer.ACCEPT),
                                        LocalDateTime.now()
                                )
                        );
                        this.active = false;
                    }
                    else {
                        double counterOffer = calculateCounterOffer(
                                new NegociationContext(
                                        budget,
                                        providerOffer,
                                        lastOfferPrice,
                                        initialOffer,
                                        this.interest,
                                        this.ticket
                                )
                        );

                        if (counterOffer <= budget) {
                            lastOfferPrice = counterOffer;
                            System.out.println("[" + buyerName + "] counters with " + counterOffer);

                            Agent.publishToMessageQueue(
                                    this.currentProvider,
                                    new Message(
                                            this,
                                            this.currentProvider,
                                            new Offer(counterOffer, this.ticket, TypeOffer.AGAINST_PROPOSITION),
                                            LocalDateTime.now()
                                    )
                            );
                        } else {
                            System.out.println("[" + buyerName + "] cannot afford the counter-offer. Ending negotiation.");
                            this.active = false;
                        }
                    }
                }
                case ACCEPT -> {
                    System.out.println("[" + buyerName + "] sees that the Provider ACCEPTED.");
                    checkIfSuperiorToMaxRound();
                    this.active = false;
                }
                case END_NEGOCIATION -> {
                    System.out.println("[" + buyerName + "] sees that the Provider ended the negotiation.");
                    this.active = false;
                }
                case INITIAL -> {
                    System.out.println("[" + buyerName + "] received an INITIAL offer (unexpected in this flow).");
                }
            }
        }

        System.out.println("[" + buyerName + "] concluded its negotiations.");

        Agent.publishToMessageQueue(
                this.currentProvider,
                new Message(
                        this,
                        this.currentProvider,
                        new Offer(this.lastOfferPrice, this.ticket, TypeOffer.END_NEGOCIATION),
                        LocalDateTime.now()
                )
        );
    }

    private void checkIfSuperiorToMaxRound() {
        if (negotiationRounds >= MAX_ROUNDS) {
            System.out.println("[" + buyerName + "] has reached the maximum number of negotiation rounds.");
        }
    }
}