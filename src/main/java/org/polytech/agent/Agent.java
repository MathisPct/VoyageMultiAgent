package org.polytech.agent;

import org.polytech.agent.strategy.NegociationContext;
import org.polytech.agent.strategy.NegociationStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Agent implements NegociationStrategy {
    protected static HashMap<Agent, List<Message>> messagesQueue;
    protected static List<Provider> providers;
    protected static List<Buyer> buyers;
    protected int interest;
    private NegociationStrategy negociationStrategy;

    static {
        messagesQueue = new HashMap<>();
        providers = new ArrayList<>();
        buyers = new ArrayList<>();
    }

    public Agent() {
        this.interest = new Random().nextInt(1, 10 + 1); // between 1 and 10
    }

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
        List<Message> messages = Agent.messagesQueue.getOrDefault(recipient, new ArrayList<>());
        messages.add(message);
        Agent.messagesQueue.put(recipient, messages);
    }

    protected Message waitUntilReceiveMessage(Agent receiver) {
        while (true) {
            List<Message> receiverMessages = Agent.messagesQueue.get(receiver);
            if (receiverMessages != null && !receiverMessages.isEmpty()) {
                synchronized (receiverMessages) {
                    for (Message message : receiverMessages) {
                        if (!message.isRead()) {
                            message.setRead(true);
                            receiverMessages.remove(message);
                            return message;
                        }
                    }
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
    }
}
