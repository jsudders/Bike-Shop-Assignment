package edu.curtin.oose.bikeApp.view;

import edu.curtin.oose.bikeApp.BikeStore;
import edu.curtin.oose.bikeApp.MessageHandler;
import edu.curtin.oose.bikeApp.model.Bike;
import edu.curtin.oose.bikeApp.model.BikeState;
import edu.curtin.oose.bikeApp.model.ServiceState;
import edu.curtin.oose.bikeApp.model.Message;
public class BankObserver implements MessageObserver {
    private BikeStore store;

    public BankObserver(BikeStore inStore, MessageHandler inSubject)
    {
        store = inStore;
    }
    
    @Override
    public void update(Message event)
    {
        int currentCash = store.getCash();
        int newCash = currentCash;
        String message = event.message;
        Bike bike = event.bikeInfo;
        switch(message)
        {
            case "DELIVERY":
                newCash = currentCash - 5000;
                break;
            case "PURCHASE-ONLINE":
                newCash = currentCash + 1000;
                break;
            case "PURCHASE-IN-STORE":
                newCash = currentCash + 1000;
                break;
            case "PICK-UP":
                BikeState currentState = bike.getState();
                if(currentState instanceof ServiceState)
                {
                    newCash = currentCash + 100;
                }
                
        }
        store.updateCash(newCash);
    }
}
