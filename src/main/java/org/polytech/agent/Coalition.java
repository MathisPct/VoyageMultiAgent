package org.polytech.agent;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class Coalition {
    private Set<Buyer> members;
    private double coalitionBudget;
    private Buyer representative;

    public Coalition() {
        this.members = new HashSet<>();
    }

    public void addMember(Buyer buyer) {
        members.add(buyer);
        updateCoalitionProperties();
    }

    private void updateCoalitionProperties() {
        // Le budget est indexé sur le plus petit budget du groupe
        this.coalitionBudget = members.stream()
                .mapToDouble(b -> b.getBuyerConstraints().getMaxBudget())
                .min()
                .orElse(0.0);

        // Le représentant est celui qui a le plus grand budget initial
        this.representative = members.stream()
                .max(Comparator.comparingDouble(b -> b.getBuyerConstraints().getMaxBudget()))
                .orElse(null);
    }

    public Set<Buyer> getMembers() {
        return new HashSet<>(members);
    }

    public double getCoalitionBudget() {
        return coalitionBudget;
    }

    public Buyer getRepresentative() {
        return representative;
    }

    public int getSize() {
        return members.size();
    }

    public boolean hasCommonDestinationsAndCompanies() {
        if (members.isEmpty()) return false;
        
        // Get destinations of first member
        Set<String> commonDestinations = new HashSet<>(members.iterator().next().getBuyerConstraints().getDestinations());
        Set<Company> commonCompanies = new HashSet<>(members.iterator().next().getBuyerConstraints().getAllowedCompanies());
        
        for (Buyer buyer : members) {
            commonDestinations.retainAll(buyer.getBuyerConstraints().getDestinations());
            commonCompanies.retainAll(buyer.getBuyerConstraints().getAllowedCompanies());
            if (commonDestinations.isEmpty() || commonCompanies.isEmpty()) {
                return false;
            }
        }
        
        return !commonDestinations.isEmpty() && !commonCompanies.isEmpty();
    }

    @Override
    public String toString() {
        return "Coalition{" +
                "members=" + members.stream().map(Agent::getName).toList() +
                ", budget=" + coalitionBudget +
                ", representative=" + (representative != null ? representative.getName() : "none") +
                '}';
    }
}