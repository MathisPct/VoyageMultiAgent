package org.polytech.agent;

public class Offer {
    private double price;
    private TypeOffer typeOffer;

    public Offer(double price, TypeOffer typeOffer) {
        this.price = price;
        this.typeOffer = typeOffer;
    }

    public double getPrice() {
        return this.price;
    }

    public TypeOffer getTypeOffer() {
        return typeOffer;
    }
}
