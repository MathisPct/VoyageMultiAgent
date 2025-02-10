package org.polytech.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

import org.polytech.agent.Ticket;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TicketsViewController {
    
    @FXML
    private TableView<Ticket> ticketsTable;
    
    @FXML
    private TableColumn<Ticket, String> providerColumn;
    
    @FXML
    private TableColumn<Ticket, String> departureColumn;

    @FXML
    private TableColumn<Ticket, String> arrivalColumn;

    @FXML
    private TableColumn<Ticket, Double> priceColumn;
    
    @FXML
    private TableColumn<Ticket, String> companyColumn;
    
    @FXML
    private TableColumn<Ticket, String> quantityColumn;

    private final ObservableList<Ticket> tickets = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        providerColumn.setCellValueFactory(new PropertyValueFactory<>("provider"));
        departureColumn.setCellValueFactory(new PropertyValueFactory<>("departure"));
        arrivalColumn.setCellValueFactory(new PropertyValueFactory<>("arrival"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        companyColumn.setCellValueFactory(new PropertyValueFactory<>("company"));

        ticketsTable.setItems(tickets);
    }

    public void updateTickets(List<Ticket> updatedTickets) {
        tickets.clear();
        tickets.addAll(updatedTickets);
    }
}