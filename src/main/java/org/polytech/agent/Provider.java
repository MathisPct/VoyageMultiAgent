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
import java.util.HashMap;

public class Provider extends Agent implements Runnable {
    private double lastProposedPrice = 0.0;
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
            ConcurrentHashMap<Buyer, Double> ticketOffers = this.offersMap.get(ticket);
            if (ticketOffers != null && !ticketOffers.isEmpty()) {
                Buyer bestBuyer = ticketOffers.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse(null);
                if (bestBuyer != null) {
                    bestSales.put(ticket, new ProviderChoice(bestBuyer, ticketOffers.get(bestBuyer)));
                }
            }
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
                System.out.println("[" + this.getName() + "]"  + " did not receive a valid message.");
                continue;
            }

            double proposalPrice = message.getOffer().getPrice();
            Ticket ticket = message.getOffer().getTicket();

            Buyer buyerSender = (Buyer) message.getIssuer();

            switch (message.getOffer().getTypeOffer()) {
                case INITIAL -> {
                    System.out.println("[" + this.getName() + "]"  + " Received INITIAL offer of " + proposalPrice);
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
                    System.out.println("[" + this.getName() + "]"  + " Counters with " + counterOffer);

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
                    System.out.println("[" + this.getName() + "] " + buyerSender.getName() + " first accepts the proposed price of " + proposalPrice);
                }
                case DEMAND_CONFIRMATION_ACHAT -> {
                    System.out.println("[" + this.getName() + "] "  + buyerSender.getName() + " demands confirmation of purchase.");

                    // on filtre le ticket demandé, et on récupère toutes les offres pour ce ticket
                    offersMap.putIfAbsent(ticket, new ConcurrentHashMap<>());
                    ConcurrentHashMap<Buyer, Double> offers = offersMap.get(ticket);

                    if (offers != null && !offers.isEmpty()) {
                        Buyer bestBuyer = offers.entrySet().stream()
                                .max(Map.Entry.comparingByValue())
                                .map(Map.Entry::getKey)
                                .orElse(null);

                        if (bestBuyer != null) {
                            TypeOffer responseType;
                            double offerPrice = offers.get(bestBuyer);
                            
                            int requestedTickets = bestBuyer.getBuyerConstraints().getCoalitionSize(); // TODO: le faire dans le message
                            if (bestBuyer == message.getIssuer() && !ticket.hasBeenSelled() && ticket.getQuantity() >= requestedTickets) {
                                responseType = TypeOffer.POSITIVE_RESPONSE_CONFIRMATION_ACHAT;
                                offersMap.get(ticket).entrySet().removeIf(entry -> entry.getKey() == bestBuyer);
                                // On retire les autres offres placées par l'acheteur sur les autres ticket
                                offersMap.forEach((t, offersMap) -> offersMap.entrySet().removeIf(entry -> entry.getKey() == bestBuyer));
                                System.out.println("[" + this.getName() + "]" + " has accepted the offer of " + bestBuyer.getName() + " for " + requestedTickets + " tickets of " + ticket);
                                
                                ticket.decrementQuantity(requestedTickets);
                            } else {
                                responseType = TypeOffer.NEGATIVE_RESPONSE_CONFIRMATION_ACHAT;
                                offerPrice = message.getOffer().getPrice(); // On renvoie le prix proposé par l'acheteur
                            }

                            this.sendMessage(
                                message.getIssuer(),
                                new Message(
                                    this,
                                    message.getIssuer(),
                                    new Offer(offerPrice, ticket, responseType),
                                    LocalDateTime.now()
                                )
                            );
                            if(responseType == TypeOffer.POSITIVE_RESPONSE_CONFIRMATION_ACHAT) {
                                ticket.hasBeenSelled(true);
                            }
                        }
                    } else {
                        // Si pas d'offres, envoyer une réponse négative
                        this.sendMessage(
                            message.getIssuer(),
                            new Message(
                                this,
                                message.getIssuer(),
                                new Offer(message.getOffer().getPrice(), ticket, TypeOffer.NEGATIVE_RESPONSE_CONFIRMATION_ACHAT),
                                LocalDateTime.now()
                            )
                        );
                    }
                }
                case END_FIRST_PHASE_NEGOCIATION -> {
                    this.receiveFinalOffer(buyerSender, message.getOffer().getTicket(), message.getOffer().getPrice());
                    System.out.println("[" + this.getName() + "] "  + buyerSender.getName() + " ended the first phase of negotiation, received its final offer.");
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
                        System.out.println("[" + this.getName() + "]"  + " Accepts " + buyerSender.getName() + " offer of " + proposalPrice + " for " + ticket);
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
                        System.out.println("[" + this.getName() + "]"  + " Counters with " + counterOffer);
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
                    System.out.println("[" + this.getName() + "]"  + " Unknown type of offer");
                }
            }
        }

        System.out.println("[" + this.getName() + "]"  + " has concluded its operations");
    }

    @Override
    protected void resetSpecific() {
        this.active = false;
        this.offersMap.clear();
        for (Ticket ticket : this.tickets) {
            ticket.hasBeenSelled(false);
            ticket.setQuantity(5);
        }
        this.active = true;
    }
}