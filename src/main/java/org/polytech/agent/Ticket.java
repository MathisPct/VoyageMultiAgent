package org.polytech.agent;

import java.util.Objects;

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

    public String getDeparture() {
        return departure;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ticket ticket = (Ticket) o;
        return Double.compare(price, ticket.price) == 0 && Double.compare(minPrice, ticket.minPrice) == 0 && Objects.equals(departure, ticket.departure) && Objects.equals(arrival, ticket.arrival) && company == ticket.company;
    }

    @Override
    public int hashCode() {
        return Objects.hash(departure, arrival, price, minPrice, company);
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "departure='" + departure + '\'' +
                ", arrival='" + arrival + '\'' +
                ", price=" + price +
                ", minPrice=" + minPrice +
                ", company=" + company +
                '}';
    }
}
