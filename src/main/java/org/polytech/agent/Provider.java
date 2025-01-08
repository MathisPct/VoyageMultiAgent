package org.polytech.agent;

import org.polytech.agent.strategy.NegociationContext;
import org.polytech.agent.strategy.NegociationStrategy;

import java.time.LocalDateTime;
import java.util.List;

public class Provider extends Agent implements Runnable, NegociationStrategy {
    private static final double MAX_PRICE_INCREASE_PERCENT = 0.10;
    private static final double MIN_ACCEPTABLE_DECREASE_PERCENT = 0.01;
    private double lastProposedPrice = 0.0;
    private NegociationStrategy negociationStrategy;
    /**
     * Proposals which are delivered to the buyer
     */
    private List<Ticket> tickets;
    private boolean active = true;

    public Provider(List<Ticket> tickets) {
        this.tickets = tickets;
        this.interest = 2;
        Agent.providers.add(this);
    }

    public List<Ticket> getTickets() {
        return tickets;
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

    private boolean isOfferAcceptable(double proposedPrice) {
        return tickets.stream().anyMatch(t -> t.getPrice() - t.getPrice() * MIN_ACCEPTABLE_DECREASE_PERCENT  <= proposedPrice);
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
