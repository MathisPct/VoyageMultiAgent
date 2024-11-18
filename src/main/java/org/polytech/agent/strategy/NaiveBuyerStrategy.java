package org.polytech.agent.strategy;

public class NaiveBuyerStrategy implements NegociationStrategy {
    /**
     * Max increase price percent per negotiation round
     */
    private static final double MAX_PRICE_INCREASE_PERCENT = 0.10;
    private static final double INITIAL_OFFER_PERCENT = 0.60;

    @Override
    public double calculateInitialOffer(NegociationContext negociationContext) {
        return negociationContext.budget() * INITIAL_OFFER_PERCENT;
    }

    @Override
    public double calculateCounterOffer(NegociationContext negociationContext) {
        double maxIncrease = negociationContext.offer() * MAX_PRICE_INCREASE_PERCENT;
        return Math.min(negociationContext.budget(), negociationContext.lastOfferPrice() + maxIncrease);
    }

    @Override
    public boolean shouldAcceptOffer(NegociationContext negociationContext) {
        return negociationContext.offer() - negociationContext.lastOfferPrice() <= negociationContext.lastOfferPrice() * MAX_PRICE_INCREASE_PERCENT;
    }
}
