package org.polytech.agent.strategy;

public class InterestBasedBuyerStrategy implements NegociationStrategy {
    /**
     * Calculates what fraction of the ticket's price should be used for the initial offer,
     * based on buyer's interest. Higher interest => a higher fraction.
     */
    private double calculatePercentageInitialOffer(int interest) {
        // For example, interest = 1 => ~30%% of ticket price, interest = 10 => ~80%.
        double base = 0.25;   // baseline fraction
        double step = 0.055;  // how much each interest point adds
        double fraction = base + interest * step;
        // Cap it at 1.0 in case fraction goes above 100%
        return Math.min(fraction, 1.0);
    }

    /**
     * Calculates how much the buyer is willing to increase the offer in each counter.
     * A higher interest => a bigger increase.
     */
    private double calculatePercentageCounterOffer(int interest) {
        // For instance, interest=1 => +3% each round, interest=10 => +30% each round.
        double step = 0.03;
        return interest * step;
    }

    @Override
    public double calculateInitialOffer(NegociationContext negociationContext) {
        // Buyer starts with a fraction of the ticket's base price
        double fraction = calculatePercentageInitialOffer(negociationContext.interest());
        return negociationContext.ticket().getPrice() * fraction;
    }

    @Override
    public double calculateCounterOffer(NegociationContext negociationContext) {
        // Increase last offer by a fraction based on interest
        double increaseFactor = 1.0 + calculatePercentageCounterOffer(negociationContext.interest());
        return negociationContext.lastOfferPrice() * increaseFactor;
    }

    @Override
    public boolean shouldAcceptOffer(NegociationContext negociationContext) {
        // Accept if the difference between the provider's offer and our last offer
        // is below a certain percentage of our last offer (scaled by interest).
        double difference = negociationContext.offer() - negociationContext.lastOfferPrice();
        // Example: interest=10 => up to 30%, interest=1 => up to 3%.
        double threshold = negociationContext.lastOfferPrice() * calculatePercentageCounterOffer(negociationContext.interest());
        return difference <= threshold;
    }
}