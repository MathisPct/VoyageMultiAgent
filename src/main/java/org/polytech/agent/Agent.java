package org.polytech.agent;

import org.polytech.agent.strategy.NegociationContext;
import org.polytech.agent.strategy.NegociationStrategy;
import org.polytech.messaging.Message;
import org.polytech.messaging.MessageManager;
import org.polytech.messaging.MessageManagerSimpleImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class Agent implements NegociationStrategy {
    protected BlockingQueue<Message> messagesQueue;
    protected static List<Provider> providers;
    protected static List<Buyer> buyers;
    protected int interest;
    private NegociationStrategy negociationStrategy;

    private final MessageManager messageManager;

    static {
        providers = new ArrayList<>();
        buyers = new ArrayList<>();
    }

    public Agent(MessageManager messageManager) {
        this.interest = new Random().nextInt(1, 10 + 1); // between 1 and 10
        messagesQueue = new LinkedBlockingQueue<>();
        this.messageManager = messageManager;
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
    public void sendMessage(Agent recipient, Message message) {
        this.messageManager.sendMessage(recipient, message);
    }

    public void receiveMessage(Message message) {
        if (this.messagesQueue != null) {
            this.messagesQueue.offer(message);
        }
    }

    protected Message waitUntilReceiveMessage() {
        try {
            return this.messagesQueue.take();
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
