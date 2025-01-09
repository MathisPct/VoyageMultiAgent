package org.polytech.agent;

import org.polytech.agent.constraints.BuyerConstraints;
import org.polytech.agent.strategy.NegociationContext;

import java.time.LocalDateTime;
import java.util.*;

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
    private Stack<BuyerChoice> choices = new Stack<>();

    public Buyer(BuyerConstraints buyerConstraints, String buyerName, int interest) {
        super();
        this.interest = interest;
        this.buyerConstraints = buyerConstraints;
        this.buyerName = buyerName;

        this.active = true;
        Agent.buyers.add(this);
    }

    @Override
    public String getName() {
        return this.buyerName;
    }

    @Override
    public void run() {
        this.makeAllNegociation();
    }

    private void makeAllNegociation() {
        findSuitableTicket().forEach((ticket, provider) -> {
            // Première phase de négociation
            Offer finalOffer = processNegociation(ticket, provider);
            choices.push(new BuyerChoice(provider, finalOffer));
        });

        // Deuxième phase de négociation: choisir la meilleure offre parmi les offres reçues et on attend la confirmation de l'achat
        if (findSuitableTicket().isEmpty()) {
            System.out.println("[" + buyerName + "] found no suitable ticket based on constraints. Stopping.");
            return;
        }

        BuyerChoice choice = chooseBestOffer();
        System.out.println("[" + buyerName + "] has chosen the best offer: " + choice.offer().getPrice() + " from " + choice.provider().getName());
    }

    private BuyerChoice chooseBestOffer() {
        choices.sort(Comparator.comparingDouble(choice -> choice.offer().getPrice()));

        do {
            BuyerChoice choice = choices.pop();

            // on envoie le message pour demander la confirmation de l'achat
            Offer offer = choice.offer();
            offer.setTypeOffer(TypeOffer.DEMAND_CONFIRMATION_ACHAT);

            Agent.publishToMessageQueue(
                    choice.provider(),
                    new Message(
                            this,
                            choice.provider(),
                            offer,
                            LocalDateTime.now()
                    )
            );

            Message message = waitUntilReceiveMessage();

            if (message.getOffer().getTypeOffer() == TypeOffer.POSITIVE_RESPONSE_CONFIRMATION_ACHAT) {
                System.out.println("[" + buyerName + "] received POSITIVE_RESPONSE_CONFIRMATION_ACHAT from Provider. Ending negotiation.");
                return choice;
            }
        } while (!choices.isEmpty());

        return null;
    }

    // Première phase
    private Offer processNegociation(Ticket ticket, Provider provider) {
        this.chosenTicketToNegociateWith = ticket;
        this.currentProvider = provider;

        if (this.chosenTicketToNegociateWith == null) {
            System.out.println("[" + buyerName + "]" + " found no suitable ticket based on constraints. Stopping.");
            this.active = false;
            return null;
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

            Message response = waitUntilReceiveMessage();
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
                                        new Offer(providerOffer, this.chosenTicketToNegociateWith, TypeOffer.FIRST_ACCEPT),
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
                case FIRST_ACCEPT -> {
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

        Offer finalOffer = new Offer(this.lastOfferPrice, this.chosenTicketToNegociateWith, TypeOffer.END_NEGOCIATION);
        Agent.publishToMessageQueue(
                this.currentProvider,
                new Message(
                        this,
                        this.currentProvider,
                        finalOffer,
                        LocalDateTime.now()
                )
        );

        return finalOffer;
    }


    private Map<Ticket, Provider> findSuitableTicket() {
        Map<Ticket, Provider> suitableTickets = new HashMap<>();

        // for now, iterate providers and pick the first matching ticket
        for (Provider p : Agent.providers) {
            for (Ticket t : p.getTickets()) {
                boolean withinBudget = t.getPrice() <= this.buyerConstraints.getMaxBudget();
                boolean allowedCompany = buyerConstraints.isCompanyAllowed(t.getCompany());
                boolean isDestinationCorrect = buyerConstraints.isDestinationSuitable(t.getArrival());
                if (withinBudget && allowedCompany && isDestinationCorrect) {
                    suitableTickets.put(t, p);
                }
            }
        }
        return suitableTickets;
    }

    private void checkIfSuperiorToMaxRound() {
        if (negotiationRounds >= MAX_ROUNDS) {
            System.out.println("[" + buyerName + "] has reached the maximum number of negotiation rounds.");
        }
    }

    public boolean isInterestedIn(Ticket ticket) {
        return this.chosenTicketToNegociateWith.equals(ticket);
    }
}