package org.polytech;

import org.polytech.agent.Buyer;
import org.polytech.agent.Provider;
import org.polytech.agent.Ticket;
import org.polytech.agent.strategy.NaiveBuyerStrategy;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        /*
         For now, we have only:
           1 Provider
           1 Buyer
         */
        Provider provider = new Provider(List.of(
                new Ticket(70, 70 * 0.9, "Paris", "Marseille"),
                new Ticket(75, 75 * 0.9,"Paris", "Lyon"),
                new Ticket(85, 85 * 0.9,"Paris", "Lille"),
                new Ticket(90, 90 * 0.9,"Paris", "Bordeaux"),
                new Ticket(100, 100 * 0.9,"Paris", "Nantes")
        ));

        Buyer buyer = new Buyer(90, provider.getTickets().get(0));
        buyer.setNegociationStrategy(new NaiveBuyerStrategy());

        Thread providerThread = new Thread(provider);
        Thread buyerThread = new Thread(buyer);

        providerThread.start();
        buyerThread.start();
    }
}