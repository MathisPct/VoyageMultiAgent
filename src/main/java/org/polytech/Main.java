package org.polytech;

import org.polytech.agent.Buyer;
import org.polytech.agent.Company;
import org.polytech.agent.Provider;
import org.polytech.agent.Ticket;
import org.polytech.agent.constraints.BuyerConstraints;
import org.polytech.agent.strategy.*;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        Provider provider = new Provider(List.of(
                new Ticket(85, 85 * 0.9, "Paris", "Amsterdam", Company.TRANSAVIA),
                new Ticket(75, 75 * 0.9, "Paris", "Amsterdam", Company.KLM),
                new Ticket(70, 70 * 0.9, "Paris", "Amsterdam", Company.KLM),
                new Ticket(80, 80 * 0.9, "Paris", "Amsterdam", Company.TRANSAVIA),
                new Ticket(75, 75 * 0.9, "Paris", "Lyon", Company.TRANSAVIA),
                new Ticket(85, 85 * 0.9, "Paris", "Lille", Company.TRANSAVIA),
                new Ticket(90, 90 * 0.9, "Paris", "Bordeaux", Company.AIR_FRANCE),
                new Ticket(100, 100 * 0.9, "Amsterdam", "Suede", Company.KLM)
        ), 5);
        System.out.println("Provider interest: " + provider.getInterest());

        BuyerConstraints buyerConstraints1 = new BuyerConstraints(75);
        buyerConstraints1.addAllowedCompany(Company.KLM);
        buyerConstraints1.addDestination("Amsterdam");
        Buyer buyer1 = new Buyer(buyerConstraints1, "Buyer1", 7);

        BuyerConstraints buyerConstraints2 = new BuyerConstraints(75);
        buyerConstraints2.addAllowedCompany(Company.KLM);
        buyerConstraints2.addDestination("Amsterdam");
        Buyer buyer2 = new Buyer(buyerConstraints2,"Buyer2", 6);

        BuyerConstraints buyerConstraints3 = new BuyerConstraints(85);
        buyerConstraints3.addDestination("Suede");
        Buyer buyer3 = new Buyer(buyerConstraints3, "Buyer3", 6);

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

        try {
            buyer1Thread.join();
            buyer2Thread.join();
            buyer3Thread.join();

            System.out.println(provider.selectBestSale());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}