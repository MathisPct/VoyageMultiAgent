package org.polytech.agent;

public class Ticket {
    private String departure;
    private String arrival;
    private double price;
    private double minPrice;
    private Company company;

    public Ticket(double price, double minPrice, String departure, String arrival, Company company) {
        this.price = price;
        this.departure = departure;
        this.arrival = arrival;
        this.minPrice = minPrice;
        this.company = company;
    }

    public double getPrice() {
        return price;
    }

    public double getMinPrice() {
        return minPrice;
    }

    public Company getCompany() {
        return company;
    }

    public String getArrival() {
        return arrival;
    }
}
