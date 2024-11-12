package org.polytech;

import org.polytech.agent.Agent;
import org.polytech.agent.Buyer;
import org.polytech.agent.Provider;
import org.polytech.agent.Ticket;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        /*
         For now, we have only:
           1 Provider
           1 Buyer
         */
        Provider provider = new Provider(List.of(
                new Ticket(60, "Paris", "Marseille"),
                new Ticket(70, "Paris", "Marseille"),
                new Ticket(75, "Paris", "Marseille"),
                new Ticket(85, "Paris", "Marseille"),
                new Ticket(90, "Paris", "Marseille"),
                new Ticket(100, "Paris", "Marseille")
        ));

        Buyer buyer = new Buyer(75);

        Thread providerThread = new Thread(provider);
        Thread buyerThread = new Thread(buyer);

        providerThread.start();
        buyerThread.start();
    }
}