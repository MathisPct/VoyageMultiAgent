package org.polytech.agent.strategy;

import org.polytech.agent.Ticket;

/**
 * A simple interest-based strategy for a provider.
 * Higher "interest" means the provider is more motivated to sell quickly and
 * might be willing to lower prices more rapidly as negotiations progress.
 */
public class InterestBasedProviderStrategy implements NegociationStrategy {

    /**
     * For the initial offer, we raise the base ticket price by a small fraction.
     * If interest is high, the provider won't raise the price much (they want to sell),
     * if interest is low, the provider tries to charge more initially.
     */
    @Override
    public double calculateInitialOffer(NegociationContext negociationContext) {
        double basePrice = negociationContext.ticket().getPrice();
        // Example: If interest = 10 (very motivated), increase is small (5%).
        // If interest = 1 (less motivated to sell), increase is larger (20%).
        double increasePercent = 0.20 - (0.015 * (negociationContext.interest() - 1));
        return basePrice * (1.0 + increasePercent);
    }

    /**
     * For counter offers, the provider tries to reduce the price gradually.
     * If there's no previous offer, start from the initial offer logic.
     * Otherwise, discount a small fraction based on interest:
     * More interest = bigger discount (faster to reach an agreement).
     */
    @Override
    public double calculateCounterOffer(NegociationContext negociationContext) {
        double currentOffer = negociationContext.lastOfferPrice();
        double basePrice = negociationContext.ticket().getPrice();

        if (currentOffer == 0) {
            // Use initial offer logic if no prior offer
            return calculateInitialOffer(negociationContext);
        } else {
            // Reduce the current offer by a small fraction (e.g., 1% per interest point)
            double reductionPercent = 0.01 * negociationContext.interest();
            double newPrice = currentOffer * (1.0 - reductionPercent);
            // Ensure we don't go below the min price of the ticket
            return Math.max(newPrice, negociationContext.ticket().getMinPrice());
        }
    }

    /**
     * Decide whether to accept the buyer’s offer:
     * If the buyer’s offer is above a certain threshold of the base price
     * (factor in interest; higher interest => accept lower offers more eagerly),
     * then accept.
     */
    @Override
    public boolean shouldAcceptOffer(NegociationContext negociationContext) {
        double basePrice = negociationContext.ticket().getPrice();
        double threshold = basePrice * (0.75 + (0.02 * (10 - negociationContext.interest())));
        return negociationContext.offer() >= threshold;
    }
}