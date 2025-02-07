package org.polytech.agent;

public class Offer {
    private double price;
    private TypeOffer typeOffer;
    private Ticket ticket;

    public Offer(double price, Ticket ticket, TypeOffer typeOffer) {
        this.price = price;
        this.typeOffer = typeOffer;
        this.ticket = ticket;
    }

    public void setTypeOffer(TypeOffer typeOffer) {
        this.typeOffer = typeOffer;
    }

    public double getPrice() {
        return this.price;
    }

    public TypeOffer getTypeOffer() {
        return typeOffer;
    }

    public Ticket getTicket() {
        return ticket;
    }

    @Override
    public String toString() {
        return "Offer{" +
                "price=" + price +
                ", typeOffer=" + typeOffer +
                ", ticket=" + ticket.toString() +
                '}';
    }
}
