package org.polytech.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.polytech.agent.Buyer;
import org.polytech.agent.Ticket;
import org.polytech.messaging.Message;
import org.polytech.agent.TypeOffer;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class TicketTabController implements Initializable {
    @FXML private TextFlow ticketInfoText;
    @FXML private Text ticketBasicInfo;
    @FXML private Text ticketDetailsInfo;
    @FXML private TextFlow buyerInfoText;
    @FXML private Text buyerBasicInfo;
    @FXML private Text buyerDetailsInfo;
    @FXML private ListView<Message> firstPhaseMessages;
    @FXML private ListView<Message> secondPhaseMessages;
    
    private Ticket ticket;
    private Buyer buyer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        MessageCellFactory cellFactory = new MessageCellFactory();
        
        // Initialize empty texts to avoid NPE
        ticketBasicInfo.setText("");
        ticketDetailsInfo.setText("");
        buyerBasicInfo.setText("");
        buyerDetailsInfo.setText("");
        
        // Initialize list views
        if (firstPhaseMessages != null) {
            firstPhaseMessages.setCellFactory(cellFactory);
        }
        if (secondPhaseMessages != null) {
            secondPhaseMessages.setCellFactory(cellFactory);
        }
    }

    public void updateTicketInfo(Ticket ticket, Buyer buyer) {
        if (ticket == null) return;
        
        this.ticket = ticket;
        this.buyer = buyer;

        if (ticketBasicInfo != null) {
            ticketBasicInfo.setText(String.format("Ticket: %s → %s%n",
                ticket.getDeparture(), ticket.getArrival()));
        }
        
        if (ticketDetailsInfo != null) {
            ticketDetailsInfo.setText(String.format(
                "Compagnie: %s%nPrix de base: %.2f€%nQuantité disponible: %d%n",
                ticket.getCompany(),
                ticket.getPrice(),
                ticket.getQuantity()
            ));
        }

        if (buyer != null) {
            if (buyerBasicInfo != null) {
                buyerBasicInfo.setText(String.format("Acheteur: %s%n", buyer.getName()));
            }
            
            if (buyerDetailsInfo != null) {
                int coalitionSize = buyer.getBuyerConstraints().getCoalitionSize();
                String coalitionInfo = coalitionSize > 1 ?
                    String.format("Négociation pour une coalition de %d acheteurs%n", coalitionSize) :
                    String.format("Négociation individuelle%n");
                
                buyerDetailsInfo.setText(String.format(
                    "%sBudget maximum: %.2f€%nNiveau d'intérêt: %d/10",
                    coalitionInfo,
                    buyer.getBuyerConstraints().getMaxBudget(),
                    buyer.getInterest()
                ));
            }
        }
    }

    public void addMessage(Message message) {
        if (message == null) return;

        TypeOffer offerType = message.getOffer().getTypeOffer();
        if (isSecondPhaseMessage(offerType)) {
            if (secondPhaseMessages != null) {
                secondPhaseMessages.getItems().add(message);
            }
        } else {
            if (firstPhaseMessages != null) {
                firstPhaseMessages.getItems().add(message);
            }
        }
    }

    private boolean isSecondPhaseMessage(TypeOffer offerType) {
        return offerType == TypeOffer.DEMAND_CONFIRMATION_ACHAT ||
               offerType == TypeOffer.POSITIVE_RESPONSE_CONFIRMATION_ACHAT ||
               offerType == TypeOffer.NEGATIVE_RESPONSE_CONFIRMATION_ACHAT;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public Buyer getBuyer() {
        return buyer;
    }

    public void clearMessages() {
        if (firstPhaseMessages != null) {
            firstPhaseMessages.getItems().clear();
        }
        if (secondPhaseMessages != null) {
            secondPhaseMessages.getItems().clear();
        }
    }

    public List<Message> getAllMessages() {
        List<Message> allMessages = new ArrayList<>();
        if (firstPhaseMessages != null) {
            allMessages.addAll(firstPhaseMessages.getItems());
        }
        if (secondPhaseMessages != null) {
            allMessages.addAll(secondPhaseMessages.getItems());
        }
        return allMessages;
    }
}