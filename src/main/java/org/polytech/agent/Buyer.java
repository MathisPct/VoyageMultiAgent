package org.polytech.agent;

import org.polytech.agent.constraints.BuyerConstraints;
import org.polytech.agent.strategy.NegociationContext;

import java.time.LocalDateTime;

public class Buyer extends Agent implements Runnable {
    private final BuyerConstraints buyerConstraints;
    private Ticket chosenTicketToNegociateWith;
    private double lastOfferPrice = 0.0;
    private double initialOffer;
    private Provider currentProvider;
    private int negotiationRounds = 0;
    private final int MAX_ROUNDS = 8;
    private final String buyerName;
    private boolean active;

    public Buyer(BuyerConstraints buyerConstraints, String buyerName, int interest) {
        super();
        this.interest = interest;
        this.buyerConstraints = buyerConstraints;
        this.buyerName = buyerName;

        this.active = true;
        Agent.buyers.add(this);
    }

    @Override
    public void run() {
        this.chosenTicketToNegociateWith = findSuitableTicket();
        if (this.chosenTicketToNegociateWith == null) {
            System.out.println("[" + buyerName + "]" + " found no suitable ticket based on constraints. Stopping.");
            this.active = false;
            return;
        }

        while ((negotiationRounds < MAX_ROUNDS) && active) {

            if (currentProvider == null && !Agent.providers.isEmpty()) {
                this.currentProvider = Agent.providers.get(0); // TODO: à changer plus tard
                System.out.println("[" + buyerName + "] selected provider: " + currentProvider);
            }

            // Premier round => on envoie une offre INITIAL
            if (negotiationRounds == 0) {
                double offerPrice = calculateInitialOffer(
                        new NegociationContext(
                                buyerConstraints.getMaxBudget(),
                                0, // prix de l'offre reçue du Provider (ici 0, car rien reçu encore)
                                lastOfferPrice,
                                initialOffer,
                                this.interest,
                                this.chosenTicketToNegociateWith
                        )
                );

                lastOfferPrice = offerPrice;
                initialOffer = offerPrice;

                System.out.println("[" + buyerName + "] sends INITIAL offer of: " + offerPrice);

                Agent.publishToMessageQueue(this.currentProvider,
                        new Message(this,
                                this.currentProvider,
                                new Offer(offerPrice, this.chosenTicketToNegociateWith, TypeOffer.INITIAL),
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

                    if (this.buyerConstraints.getMaxBudget() >= providerOffer &&
                            this.shouldAcceptOffer(new NegociationContext(
                                    buyerConstraints.getMaxBudget(),
                                    providerOffer,
                                    lastOfferPrice,
                                    initialOffer,
                                    this.interest,
                                    this.chosenTicketToNegociateWith
                            ))
                    ) {
                        System.out.println("[" + buyerName + "] accepts the offer of " + providerOffer);
                        Agent.publishToMessageQueue(
                                this.currentProvider,
                                new Message(
                                        this,
                                        this.currentProvider,
                                        new Offer(providerOffer, this.chosenTicketToNegociateWith, TypeOffer.ACCEPT),
                                        LocalDateTime.now()
                                )
                        );
                        this.active = false;
                    }
                    else {
                        double counterOffer = calculateCounterOffer(
                                new NegociationContext(
                                        buyerConstraints.getMaxBudget(),
                                        providerOffer,
                                        lastOfferPrice,
                                        initialOffer,
                                        this.interest,
                                        this.chosenTicketToNegociateWith
                                )
                        );

                        if (counterOffer <= buyerConstraints.getMaxBudget()) {
                            lastOfferPrice = counterOffer;
                            System.out.println("[" + buyerName + "] counters with " + counterOffer);

                            Agent.publishToMessageQueue(
                                    this.currentProvider,
                                    new Message(
                                            this,
                                            this.currentProvider,
                                            new Offer(counterOffer, this.chosenTicketToNegociateWith, TypeOffer.AGAINST_PROPOSITION),
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
                        new Offer(this.lastOfferPrice, this.chosenTicketToNegociateWith, TypeOffer.END_NEGOCIATION),
                        LocalDateTime.now()
                )
        );
    }

    private Ticket findSuitableTicket() {
        // for now, iterate providers and pick the first matching ticket
        for (Provider p : Agent.providers) {
            for (Ticket t : p.getTickets()) {
                boolean withinBudget = t.getPrice() <= this.buyerConstraints.getMaxBudget();
                boolean allowedCompany = buyerConstraints.isCompanyAllowed(t.getCompany());
                boolean isDestinationCorrect = buyerConstraints.isDestinationSuitable(t.getArrival());
                if (withinBudget && allowedCompany && isDestinationCorrect) {
                    this.currentProvider = p;
                    return t;
                }
            }
        }
        return null;
    }

    private void checkIfSuperiorToMaxRound() {
        if (negotiationRounds >= MAX_ROUNDS) {
            System.out.println("[" + buyerName + "] has reached the maximum number of negotiation rounds.");
        }
    }
}