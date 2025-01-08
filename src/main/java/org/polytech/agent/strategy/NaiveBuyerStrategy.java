package org.polytech.agent.strategy;

/**
 * A naive buyer strategy:
 * - Makes a relatively low initial offer (e.g., 60% of budget).
 * - In each counter, it increases the price by up to 10% of the provider’s last proposal.
 * - Accepts if the new proposal is within a small margin of the last offer.
 */
public class NaiveBuyerStrategy implements NegociationStrategy {
    // Maximum increase in offered price per negotiation round
    private static final double MAX_PRICE_INCREASE_PERCENT = 0.10;
    // Portion of buyer's budget to use as the initial offer
    private static final double INITIAL_OFFER_PERCENT = 0.60;

    @Override
    public double calculateInitialOffer(NegociationContext negociationContext) {
        // Start by offering 60% of the buyer’s budget
        return negociationContext.budget() * INITIAL_OFFER_PERCENT;
    }

    @Override
    public double calculateCounterOffer(NegociationContext negociationContext) {
        // Increase the last offer by up to 10% of the provider's current offer price but never exceed the buyer’s total budget
        double allowableIncrease = negociationContext.offer() * MAX_PRICE_INCREASE_PERCENT;
        double candidateOffer = negociationContext.lastOfferPrice() + allowableIncrease;

        // The buyer won't exceed their total budget
        return Math.min(candidateOffer, negociationContext.budget());
    }

    @Override
    public boolean shouldAcceptOffer(NegociationContext negociationContext) {
        // If the difference between the provider’s offer and our last offer
        // is small (<= 10% of our last offer), accept it.
        double difference = negociationContext.offer() - negociationContext.lastOfferPrice();
        return difference <= negociationContext.lastOfferPrice() * MAX_PRICE_INCREASE_PERCENT;
    }
}