package org.polytech.agent.strategy;

public class NaiveProviderStrategy implements NegociationStrategy {
    private static final double MAX_PRICE_INCREASE_PERCENT = 0.10;
    private static final double MIN_ACCEPTABLE_DECREASE_PERCENT = 0.01;

    @Override
    public double calculateInitialOffer(NegociationContext negociationContext) {
        return 0;
    }

    @Override
    public double calculateCounterOffer(NegociationContext negociationContext) {
        if(negociationContext.lastOfferPrice() == 0) {
            return negociationContext.ticket().getPrice() * (1 - MAX_PRICE_INCREASE_PERCENT);
        }
        return negociationContext.lastOfferPrice() * (1 - MAX_PRICE_INCREASE_PERCENT);
    }

    @Override
    public boolean shouldAcceptOffer(NegociationContext negociationContext) {
        return negociationContext.offer() >= negociationContext.ticket().getPrice() - negociationContext.ticket().getPrice() * MIN_ACCEPTABLE_DECREASE_PERCENT;
    }
}
