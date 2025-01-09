package org.polytech.agent;

import org.polytech.agent.strategy.NegociationContext;
import org.polytech.agent.strategy.NegociationStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class Agent implements NegociationStrategy {
    protected static ConcurrentHashMap<Agent, BlockingQueue<Message>> messagesQueue;
    protected static List<Provider> providers;
    protected static List<Buyer> buyers;
    protected int interest;
    private NegociationStrategy negociationStrategy;

    static {
        messagesQueue = new ConcurrentHashMap<>();
        providers = new ArrayList<>();
        buyers = new ArrayList<>();
    }

    public Agent() {
        this.interest = new Random().nextInt(1, 10 + 1); // between 1 and 10
        messagesQueue.putIfAbsent(this, new LinkedBlockingQueue<>());
    }

    public abstract String getName();

    public void setNegociationStrategy(NegociationStrategy negociationStrategy) {
        this.negociationStrategy = negociationStrategy;
    }

    @Override
    public double calculateInitialOffer(NegociationContext negociationContext) {
        return this.negociationStrategy.calculateInitialOffer(negociationContext);
    }

    @Override
    public double calculateCounterOffer(NegociationContext negociationContext) {
        return this.negociationStrategy.calculateCounterOffer(negociationContext);
    }

    @Override
    public boolean shouldAcceptOffer(NegociationContext negociationContext) {
        return this.negociationStrategy.shouldAcceptOffer(negociationContext);
    }

    public int getInterest() {
        return interest;
    }

    /**
     * To distribute a message to a recipient
     *
     * @param recipient (destinataire) which will receive the message
     * @param message   the message
     */
    public static void publishToMessageQueue(Agent recipient, Message message) {
        BlockingQueue<Message> queue = messagesQueue.get(recipient);
        if (queue != null) {
            queue.offer(message);
        }
    }

    protected Message waitUntilReceiveMessage() {
        try {
            return messagesQueue.get(this).take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    @Override
    public String toString() {
        return "Agent: " + getName() + " " + " with interest=" + interest;
    }
}
