package org.polytech.agent;

import org.polytech.agent.strategy.NegociationContext;

import java.time.LocalDateTime;
import java.util.List;

public class Provider extends Agent implements Runnable {
    private double lastProposedPrice = 0.0;
    /**
     * Proposals which are delivered to the buyer
     */
    private List<Ticket> tickets;
    private boolean active = true;

    public Provider(List<Ticket> tickets) {
        this.tickets = tickets;
        this.interest = 9;
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
                System.out.println("Provider did not receive a valid message.");
                continue;
            }

            double proposalPrice = message.getOffer().getPrice();

            switch (message.getOffer().getTypeOffer()) {
                case INITIAL -> {
                    System.out.println("Provider received INITIAL offer of " + proposalPrice);
                    double counterOffer = calculateCounterOffer(new NegociationContext(0, proposalPrice, lastProposedPrice, 0, interest, message.getOffer().getTicket()));
                    lastProposedPrice = counterOffer;
                    System.out.println("Provider counter the offer by sending " + counterOffer);
                    Agent.publishToMessageQueue(message.getIssuer(),
                            new Message(this,
                                    message.getIssuer(),
                                    new Offer(counterOffer, message.getOffer().getTicket(), TypeOffer.AGAINST_PROPOSITION),
                                    LocalDateTime.now()));
                }
                case ACCEPT -> {
                    System.out.println("Provider accepted the offer of " + proposalPrice);
                    active = false;
                }
                case END_NEGOCIATION -> {
                    System.out.println("Provider receive end negotiation message");
                    active = false;
                }
                case AGAINST_PROPOSITION -> {
                    NegociationContext negociationContext = new NegociationContext(0, proposalPrice, lastProposedPrice, 0, interest, message.getOffer().getTicket());
                    if (this.shouldAcceptOffer(negociationContext)) {
                        System.out.println("Provider accepts the offer of " + proposalPrice);
                        Agent.publishToMessageQueue(message.getIssuer(),
                                new Message(this,
                                        message.getIssuer(),
                                        new Offer(proposalPrice, message.getOffer().getTicket(), TypeOffer.ACCEPT),
                                        LocalDateTime.now()));
                        active = false;
                    } else {
                        double counterOffer = calculateCounterOffer(negociationContext);
                        lastProposedPrice = counterOffer;
                        System.out.println("Provider counters with " + counterOffer);
                        Agent.publishToMessageQueue(message.getIssuer(),
                                new Message(this,
                                        message.getIssuer(),
                                        new Offer(counterOffer, message.getOffer().getTicket(), TypeOffer.AGAINST_PROPOSITION),
                                        LocalDateTime.now()));
                    }
                }
                default -> {
                    System.out.println("Provider received an unknown type of offer");
                }
            }
        }
        System.out.println("Provider has concluded its operations");
    }
}
