package org.polytech.agent;

import java.time.LocalDateTime;
import java.util.List;

public class Provider extends Agent implements Runnable {
    private static final double MAX_PRICE_INCREASE_PERCENT = 0.10;
    private static final double MIN_ACCEPTABLE_DECREASE_PERCENT = 0.01;
    private double lastProposedPrice = 0.0;
    /**
     * Proposals which are delivered to the buyer
     */
    private List<Ticket> tickets;
    private boolean active = true;

    public Provider(List<Ticket> tickets) {
        this.tickets = tickets;
        Agent.providers.add(this);
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    private double calculateCounterOffer(double clientOffer) {
        double maxIncrease = clientOffer * MAX_PRICE_INCREASE_PERCENT;
        double desiredPrice = clientOffer + maxIncrease;
        
        return tickets.stream()
                     .mapToDouble(Ticket::getPrice)
                     .filter(price -> price > clientOffer)
                     .min()
                     .orElse(desiredPrice);
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
                    double counterOffer = calculateCounterOffer(proposalPrice);
                    lastProposedPrice = counterOffer;
                    System.out.println("Provider counter the offer by sending " + counterOffer);
                    Agent.publishToMessageQueue(message.getIssuer(),
                            new Message(this,
                                    message.getIssuer(),
                                    new Offer(counterOffer, TypeOffer.AGAINST_PROPOSITION),
                                    LocalDateTime.now()));
                }
                case ACCEPT -> {
                    System.out.println("Provider accepted the offer of " + proposalPrice);
                    active = false;
                }
                case REFUSE -> {
                    System.out.println("Provider refused the offer of " + proposalPrice);
                    active = false;
                }
                case AGAINST_PROPOSITION -> {
                    if (isOfferAcceptable(proposalPrice)) {
                        System.out.println("Provider accepts the offer of " + proposalPrice);
                        Agent.publishToMessageQueue(message.getIssuer(),
                                new Message(this,
                                        message.getIssuer(),
                                        new Offer(proposalPrice, TypeOffer.ACCEPT),
                                        LocalDateTime.now()));
                        active = false;
                    } else {
                        double counterOffer = calculateCounterOffer(proposalPrice);
                        if (counterOffer - lastProposedPrice <= lastProposedPrice * MAX_PRICE_INCREASE_PERCENT) {
                            lastProposedPrice = counterOffer;
                            System.out.println("Provider counters with " + counterOffer);
                            Agent.publishToMessageQueue(message.getIssuer(),
                                    new Message(this,
                                            message.getIssuer(),
                                            new Offer(counterOffer, TypeOffer.AGAINST_PROPOSITION),
                                            LocalDateTime.now()));
                        } else {
                            System.out.println("Provider refuses - increase too high");
                            Agent.publishToMessageQueue(message.getIssuer(),
                                    new Message(this,
                                            message.getIssuer(),
                                            new Offer(proposalPrice, TypeOffer.REFUSE),
                                            LocalDateTime.now()));
                        }
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
