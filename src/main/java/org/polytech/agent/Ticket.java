package org.polytech.agent;

public class Ticket {
    private String departure;
    private String arrival;
    private double price;
    private double minPrice;

    public Ticket(double price, double minPrice, String departure, String arrival) {
        this.price = price;
        this.departure = departure;
        this.arrival = arrival;
        this.minPrice = minPrice;
    }

    public double getPrice() {
        return price;
    }
}
