package org.polytech.agent;

import org.polytech.agent.strategy.NegociationContext;
import org.polytech.messaging.Message;
import org.polytech.messaging.MessageManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Provider extends Agent implements Runnable {
    private double lastProposedPrice = 0.0;
    private List<Message> pendingDemandMessages = new ArrayList<>();
    private List<Ticket> tickets;
    private boolean active = true;
    private ConcurrentHashMap<Ticket, ConcurrentHashMap<Buyer, Double>> offersMap = new ConcurrentHashMap<>();

    public Provider(MessageManager messageManager, List<Ticket> tickets, int interest, String name) {
        super(messageManager, name);
        this.tickets = tickets;
        this.interest = interest;
        Agent.addProvider(this);
    }

    public synchronized void receiveFinalOffer(Buyer buyer, Ticket ticket, double offerPrice) {
        offersMap.putIfAbsent(ticket, new ConcurrentHashMap<>());
        offersMap.get(ticket).put(buyer, offerPrice);

    }

    public Map<Ticket, ProviderChoice> selectBestSale() {
        Map<Ticket, ProviderChoice> bestSales = new ConcurrentHashMap<>();
        for (Ticket ticket : this.offersMap.keySet()) {
            Buyer bestBuyer = this.offersMap.get(ticket).entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);
            bestSales.put(ticket, new ProviderChoice(bestBuyer, this.offersMap.get(ticket).get(bestBuyer)));
        }

        return bestSales;
    }

    private void checkAllOfferReceived(Ticket ticket) {
        List<Buyer> interestedBuyers = this.getInterestedBuyers(ticket);
    }

    private List<Buyer> getInterestedBuyers(Ticket ticket) {
        return buyers.stream().filter(buyer -> buyer.isInterestedIn(ticket)).collect(Collectors.toList());
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    @Override
    public void run() {
        while (active) {

            Message message = waitUntilReceiveMessage();
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

                    this.sendMessage(
                            message.getIssuer(),
                            new Message(
                                    this,
                                    message.getIssuer(),
                                    new Offer(counterOffer, message.getOffer().getTicket(), TypeOffer.AGAINST_PROPOSITION),
                                    LocalDateTime.now()
                            )
                    );
                }
                case FIRST_ACCEPT -> {
                    System.out.println("[Provider] Buyer accepts the proposed price of " + proposalPrice);
                }
                case DEMAND_CONFIRMATION_ACHAT -> {
                    pendingDemandMessages.add(message);

                    System.out.println("[Provider] One Buyer demands confirmation of purchase.");

                    // on filtre le ticket demandé, et on récupère toutes les offres pour ce ticket
                    Ticket ticket = message.getOffer().getTicket();
                    Map<Buyer, Double> offers = this.offersMap.get(ticket);
                    Buyer bestBuyer = offers.entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse(null);

                    if (bestBuyer == message.getIssuer()) {
                        this.sendMessage(
                                message.getIssuer(),
                                new Message(
                                        this,
                                        message.getIssuer(),
                                        new Offer(offers.get(bestBuyer), ticket, TypeOffer.POSITIVE_RESPONSE_CONFIRMATION_ACHAT),
                                        LocalDateTime.now()
                                )
                        );
                        this.active = false;
                    }
                }
                case END_NEGOCIATION -> {
                    message.getOffer();
                    this.receiveFinalOffer(buyerSender, message.getOffer().getTicket(), message.getOffer().getPrice());
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
                        this.sendMessage(
                                message.getIssuer(),
                                new Message(
                                        this,
                                        message.getIssuer(),
                                        new Offer(proposalPrice, message.getOffer().getTicket(), TypeOffer.FIRST_ACCEPT),
                                        LocalDateTime.now()
                                )
                        );
                    } else {
                        double counterOffer = calculateCounterOffer(negociationContext);
                        lastProposedPrice = counterOffer;
                        System.out.println("[Provider] Counters with " + counterOffer);
                        this.sendMessage(
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

    public void setActive(boolean active) {
        this.active = active;
    }
}