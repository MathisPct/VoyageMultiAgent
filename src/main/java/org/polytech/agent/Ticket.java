package org.polytech.agent;

public class Ticket {
    private String departure;
    private String arrival;
    private double price;

    public Ticket(double price, String departure, String arrival) {
        this.price = price;
        this.departure = departure;
        this.arrival = arrival;
    }

    public double getPrice() {
        return price;
    }
}
