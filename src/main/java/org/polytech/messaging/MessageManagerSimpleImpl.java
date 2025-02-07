package org.polytech.messaging;

import org.polytech.agent.Agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessageManagerSimpleImpl implements MessageManager {
    private HashMap<AgentCouple, List<Message>> agentMessageHashMap = new HashMap<>();

    public HashMap<AgentCouple, List<Message>> getAgentMessageHashMap() {
        return agentMessageHashMap;
    }

    public void sendMessage(Agent recipient, Message message) {
        AgentCouple agentCouple = new AgentCouple(message.getIssuer(), recipient);
        if (!agentMessageHashMap.containsKey(agentCouple)) {
            agentMessageHashMap.put(agentCouple, new ArrayList<>());
        }
        agentMessageHashMap.get(agentCouple).add(message);
        recipient.receiveMessage(message);
    }
}
