package org.polytech;

import org.polytech.agent.Buyer;
import org.polytech.agent.Provider;
import org.polytech.agent.Ticket;
import org.polytech.agent.strategy.*;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        Provider provider = new Provider(List.of(
                new Ticket(70, 70 * 0.9, "Paris", "Marseille"),
                new Ticket(75, 75 * 0.9, "Paris", "Lyon"),
                new Ticket(85, 85 * 0.9, "Paris", "Lille"),
                new Ticket(90, 90 * 0.9, "Paris", "Bordeaux"),
                new Ticket(100, 100 * 0.9, "Paris", "Nantes")
        ), 5);
        System.out.println("Provider interest: " + provider.getInterest());

        Buyer buyer1 = new Buyer(70, provider.getTickets().get(0), "Buyer1", 9);
        Buyer buyer2 = new Buyer(95, provider.getTickets().get(1), "Buyer2", 9);
        Buyer buyer3 = new Buyer(70, provider.getTickets().get(2), "Buyer3", 9);

        buyer1.setNegociationStrategy(new InterestBasedBuyerStrategy());
        buyer2.setNegociationStrategy(new InterestBasedBuyerStrategy());
        buyer3.setNegociationStrategy(new InterestBasedBuyerStrategy());

        provider.setNegociationStrategy(new InterestBasedProviderStrategy());

        Thread providerThread = new Thread(provider);
        Thread buyer1Thread = new Thread(buyer1);
        Thread buyer2Thread = new Thread(buyer2);
        Thread buyer3Thread = new Thread(buyer3);

        providerThread.start();
        buyer1Thread.start();
        buyer2Thread.start();
        buyer3Thread.start();
    }
}