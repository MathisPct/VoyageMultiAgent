package org.polytech.ui.controller;

import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import org.polytech.agent.*;
import org.polytech.agent.constraints.BuyerConstraints;
import org.polytech.agent.strategy.InterestBasedBuyerStrategy;
import org.polytech.agent.strategy.InterestBasedProviderStrategy;
import org.polytech.messaging.MessageManagerSimpleImpl;

import java.net.URL;
import java.util.*;

public class MainController implements Initializable {
    @FXML
    private VBox providerBox;
    @FXML
    private ListView<String> providerList;
    @FXML
    private VBox buyerBox;
    @FXML
    private ListView<String> buyerList;
    @FXML
    private ListView<String> buyersContactsOfProvider;
    @FXML
    private ListView<String> providersContactsOfBuyers;

    private Provider provider;
    private final List<Buyer> buyers = new ArrayList<>();
    private MessageManagerSimpleImpl messageManager = new MessageManagerSimpleImpl();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Setup du Provider
        provider = new Provider(messageManager, List.of(
                new Ticket(85, 85 * 0.9, "Paris", "Amsterdam", Company.TRANSAVIA),
                new Ticket(75, 75 * 0.9, "Paris", "Amsterdam", Company.KLM),
                new Ticket(70, 70 * 0.9, "Paris", "Amsterdam", Company.KLM),
                new Ticket(80, 80 * 0.9, "Paris", "Amsterdam", Company.TRANSAVIA),
                new Ticket(75, 75 * 0.9, "Paris", "Lyon", Company.TRANSAVIA),
                new Ticket(85, 85 * 0.9, "Paris", "Lille", Company.TRANSAVIA),
                new Ticket(90, 90 * 0.9, "Paris", "Bordeaux", Company.AIR_FRANCE),
                new Ticket(100, 100 * 0.9, "Amsterdam", "Suede", Company.KLM)
        ), 5);
        provider.setNegociationStrategy(new InterestBasedProviderStrategy());

        // Fabrication de quelques Buyers
        createBuyer("Buyer1", 7, 75, List.of(Company.KLM), List.of("Amsterdam"));
        createBuyer("Buyer2", 6, 75, List.of(Company.KLM), List.of("Amsterdam"));
        createBuyer("Buyer3", 6, 85, List.of(), List.of("Suede"));


        // Affichage initial
        providerList.getItems().add(
                provider.getName() + " (interest=" + provider.getInterest() + ")"
        );
        for (Buyer b : buyers) {
            buyerList.getItems().add(
                    b.getName() + " (interest=" + b.getInterest() + ")"
            );
        }

        buyerList.getSelectionModel().getSelectedItems().addListener((ListChangeListener<String>) c -> {
            providersContactsOfBuyers.getItems().clear();
            for (String buyerNameString : c.getList()) {
                for (Buyer buyer : buyers) {
                    if (buyerNameString.contains(buyer.getName())) {
                        // TODO: afficher liste agents contacts
                    }
                }
            }
        });

        providerList.getSelectionModel().getSelectedItems().addListener((ListChangeListener<String>) c -> {
            buyersContactsOfProvider.getItems().clear();
            for (String providerNameString : c.getList()) {
                if (providerNameString.contains(provider.getName())) {
                    // TODO: afficher liste agents contacts
                }
            }
        });
    }

    /**
     * Pour simplifier la création d'un Buyer, factorisée en méthode.
     */
    private void createBuyer(String name, int interest, int budget,
                             List<Company> allowedCompanies, List<String> destinations) {
        BuyerConstraints constraints = new BuyerConstraints(budget);
        allowedCompanies.forEach(constraints::addAllowedCompany);
        destinations.forEach(constraints::addDestination);

        Buyer buyer = new Buyer(messageManager ,constraints, name, interest);
        buyer.setNegociationStrategy(new InterestBasedBuyerStrategy());
        buyers.add(buyer);
    }

    /**
     * Lance la négociation en démarrant les threads des agents
     */
    @FXML
    public void launchNegociation(ActionEvent actionEvent) {
        Thread providerThread = new Thread(provider);
        providerThread.start();

        for (Buyer b : buyers) {
            new Thread(b).start();
        }
    }
}