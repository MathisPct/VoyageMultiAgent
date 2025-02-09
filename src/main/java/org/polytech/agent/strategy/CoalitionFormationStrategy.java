package org.polytech.agent.strategy;

import org.polytech.agent.Buyer;
import org.polytech.agent.Coalition;

import java.util.*;

public class CoalitionFormationStrategy {
    private final List<Buyer> buyers;
    private final Map<Set<Buyer>, Coalition> coalitionCache = new HashMap<>();
    private final Map<Set<Buyer>, Double> valueCache = new HashMap<>();
    private boolean isCooperative = false;
    private static final int MAX_COALITION_SIZE = 3;

    public CoalitionFormationStrategy(List<Buyer> buyers) {
        this.buyers = new ArrayList<>(buyers);
    }

    public void setCooperative(boolean cooperative) {
        isCooperative = cooperative;
    }

    public List<Coalition> formCoalitions() {
        if (!isCooperative) {
            // En mode compétitif, chaque acheteur forme sa propre coalition
            return buyers.stream().map(buyer -> {
                Coalition coalition = new Coalition();
                coalition.addMember(buyer);
                return coalition;
            }).toList();
        }

        List<Coalition> coalitions = new ArrayList<>();
        Set<Buyer> unassignedBuyers = new HashSet<>(buyers);
        
        while (!unassignedBuyers.isEmpty()) {
            Coalition bestCoalition = findBestCoalition(unassignedBuyers);
            if (bestCoalition == null || bestCoalition.getSize() == 0) {
                // Si on ne peut pas former de coalition, créer des coalitions individuelles
                for (Buyer buyer : unassignedBuyers) {
                    Coalition individual = new Coalition();
                    individual.addMember(buyer);
                    coalitions.add(individual);
                }
                break;
            }
            
            coalitions.add(bestCoalition);
            unassignedBuyers.removeAll(bestCoalition.getMembers());
        }

        return coalitions;
    }

    private Coalition findBestCoalition(Set<Buyer> availableBuyers) {
        Coalition bestCoalition = null;
        double bestValue = Double.NEGATIVE_INFINITY;

        for (Buyer buyer : availableBuyers) {
            Set<Buyer> potentialMembers = new HashSet<>();
            potentialMembers.add(buyer);

            // Essayer d'ajouter d'autres acheteurs jusqu'à MAX_COALITION_SIZE
            List<Buyer> otherBuyers = new ArrayList<>(availableBuyers);
            otherBuyers.remove(buyer);

            // ref: https://digital.csic.es/bitstream/10261/133296/1/OPTMAS2013.pdf
            // Générer toutes les combinaisons possibles jusqu'à MAX_COALITION_SIZE
            for (int size = 2; size <= MAX_COALITION_SIZE && size <= availableBuyers.size(); size++) {
                for (Set<Buyer> combination : generateCombinations(otherBuyers, size - 1)) {
                    Set<Buyer> coalitionSet = new HashSet<>(potentialMembers);
                    coalitionSet.addAll(combination);
                    
                    Coalition coalition = getOrCreateCoalition(coalitionSet);
                    if (coalition.hasCommonDestinationsAndCompanies()) {
                        double value = calculateCoalitionValue(coalitionSet);
                        if (value > bestValue) {
                            bestValue = value;
                            bestCoalition = coalition;
                        }
                    }
                }
            }
        }

        return bestCoalition;
    }

    private Set<Set<Buyer>> generateCombinations(List<Buyer> buyers, int k) {
        Set<Set<Buyer>> combinations = new HashSet<>();
        generateCombinationsHelper(buyers, k, 0, new HashSet<>(), combinations);
        return combinations;
    }

    private void generateCombinationsHelper(List<Buyer> buyers, int k, int start, Set<Buyer> current, Set<Set<Buyer>> result) {
        if (current.size() == k) {
            result.add(new HashSet<>(current));
            return;
        }
        
        for (int i = start; i < buyers.size(); i++) {
            current.add(buyers.get(i));
            generateCombinationsHelper(buyers, k, i + 1, current, result);
            current.remove(buyers.get(i));
        }
    }

    private double calculateCoalitionValue(Set<Buyer> buyers) {
        if (buyers.isEmpty()) return 0;
        
        // Check cache first
        if (valueCache.containsKey(buyers)) {
            return valueCache.get(buyers);
        }

        Coalition coalition = getOrCreateCoalition(buyers);
        
        // La valeur de la coalition est basée sur:
        // 1. Le nombre de membres (plus il y en a, plus c'est avantageux)
        // 2. Le budget commun (plus il est élevé, mieux c'est)
        // 3. L'existence de destinations communes (condition nécessaire)
        double value = 0;
        if (coalition.hasCommonDestinationsAndCompanies()) {
            value = coalition.getCoalitionBudget() * Math.log(1 + coalition.getSize());
        }

        valueCache.put(buyers, value);
        return value;
    }

    private Coalition getOrCreateCoalition(Set<Buyer> buyers) {
        return coalitionCache.computeIfAbsent(buyers, k -> {
            Coalition coalition = new Coalition();
            buyers.forEach(coalition::addMember);
            return coalition;
        });
    }
}