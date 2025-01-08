package org.polytech.agent.constraints;

import org.polytech.agent.Company;

import java.util.ArrayList;
import java.util.List;

public class BuyerConstraints {
    private double maxBudget;
    private List<Company> allowedCompanies;
    private List<Company> disallowedCompanies;
    private List<String> destinations;

    public BuyerConstraints(double maxBudget) {
        this.maxBudget = maxBudget;
        this.allowedCompanies = new ArrayList<>();
        this.disallowedCompanies = new ArrayList<>();
        this.destinations = new ArrayList<>();
    }

    public double getMaxBudget() {
        return maxBudget;
    }

    public void addAllowedCompany(Company company) {
        this.allowedCompanies.add(company);
    }

    public void addDisallowedCompany(Company company) {
        this.disallowedCompanies.add(company);
    }

    public boolean isCompanyAllowed(Company company) {
        // If not in disallowed list AND (allowed is empty or includes it)
        return !disallowedCompanies.contains(company) &&
                (allowedCompanies.isEmpty() || allowedCompanies.contains(company));
    }

    public void addDestination(String destination) {
        this.destinations.add(destination);
    }

    public boolean isDestinationSuitable(String destination) {
        return this.destinations.contains(destination);
    }
}
