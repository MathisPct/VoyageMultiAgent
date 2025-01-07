package org.polytech.agent.strategy;

public class InterestBasedStrategy implements NegociationStrategy {


    private double calculatePercentageInitialOffer(int envie) {
        return (-5.56* envie + 85.56) / 100;
    }

    private double calculatePercentageCounterOffer(int envie) {
        return (double) envie / 100;
    }

    @Override
    public double calculateInitialOffer(NegociationContext negociationContext) {
        return (int)(this.calculatePercentageInitialOffer(negociationContext.interest()) * negociationContext.ticket().getPrice());
    }

    @Override
    public double calculateCounterOffer(NegociationContext negociationContext) {
         return negociationContext.initialOffer() * (1 + calculatePercentageCounterOffer(negociationContext.interest()));
    }

    @Override
    public boolean shouldAcceptOffer(NegociationContext negociationContext) {
        return false;
    }
}
