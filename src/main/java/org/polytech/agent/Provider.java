package org.polytech.agent;

import org.polytech.agent.strategy.NegociationContext;

import java.time.LocalDateTime;
import java.util.List;

public class Provider extends Agent implements Runnable {
    private double lastProposedPrice = 0.0;
    private List<Ticket> tickets;
    private boolean active = true;

    public Provider(List<Ticket> tickets, int interest) {
        this.tickets = tickets;
        this.interest = interest;
        Agent.providers.add(this);
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    @Override
    public void run() {
        while (active) {
            Message message = waitUntilReceiveMessage(this);
            if (message == null) {
                System.out.println("[Provider] did not receive a valid message.");
                continue;
            }

            double proposalPrice = message.getOffer().getPrice();

            Buyer buyerSender = (Buyer) message.getIssuer();
            String buyerName = "[Provider]<--[" + buyerSender.getClass().getSimpleName() + "]";

            System.out.println(buyerName + " => Offer Type: "
                    + message.getOffer().getTypeOffer()
                    + ", Proposed Price: " + proposalPrice);

            switch (message.getOffer().getTypeOffer()) {
                case INITIAL -> {
                    System.out.println("[Provider] Received INITIAL offer of " + proposalPrice);
                    double counterOffer = calculateCounterOffer(
                            new NegociationContext(
                                    0,              // budget du Provider (optionnel)
                                    proposalPrice,  // prix reçu
                                    lastProposedPrice,
                                    0,
                                    interest,
                                    message.getOffer().getTicket()
                            )
                    );
                    lastProposedPrice = counterOffer;
                    System.out.println("[Provider] Counters with " + counterOffer);

                    Agent.publishToMessageQueue(
                            message.getIssuer(),
                            new Message(
                                    this,
                                    message.getIssuer(),
                                    new Offer(counterOffer, message.getOffer().getTicket(), TypeOffer.AGAINST_PROPOSITION),
                                    LocalDateTime.now()
                            )
                    );
                }
                case ACCEPT -> {
                    System.out.println("[Provider] Buyer accepts the proposed price of " + proposalPrice);
                }
                case END_NEGOCIATION -> {
                    System.out.println("[Provider] Buyer ended the negotiation.");
                }
                case AGAINST_PROPOSITION -> {
                    NegociationContext negociationContext = new NegociationContext(
                            0,
                            proposalPrice,
                            lastProposedPrice,
                            0,
                            interest,
                            message.getOffer().getTicket()
                    );
                    if (this.shouldAcceptOffer(negociationContext)) {
                        System.out.println("[Provider] Accepts the buyer's offer of " + proposalPrice);
                        Agent.publishToMessageQueue(
                                message.getIssuer(),
                                new Message(
                                        this,
                                        message.getIssuer(),
                                        new Offer(proposalPrice, message.getOffer().getTicket(), TypeOffer.ACCEPT),
                                        LocalDateTime.now()
                                )
                        );
                    } else {
                        double counterOffer = calculateCounterOffer(negociationContext);
                        lastProposedPrice = counterOffer;
                        System.out.println("[Provider] Counters with " + counterOffer);
                        Agent.publishToMessageQueue(
                                message.getIssuer(),
                                new Message(
                                        this,
                                        message.getIssuer(),
                                        new Offer(counterOffer, message.getOffer().getTicket(), TypeOffer.AGAINST_PROPOSITION),
                                        LocalDateTime.now()
                                )
                        );
                    }
                }
                default -> {
                    System.out.println("[Provider] Unknown type of offer");
                }
            }
        }
        System.out.println("[Provider] has concluded its operations");
    }
}