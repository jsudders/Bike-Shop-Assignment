package edu.curtin.oose.bikeApp.view;

import java.util.logging.Level;
import java.util.logging.Logger;

import edu.curtin.oose.bikeApp.BikeStore;
import edu.curtin.oose.bikeApp.MessageHandler;
import edu.curtin.oose.bikeApp.model.Bike;
import edu.curtin.oose.bikeApp.model.BikeState;
import edu.curtin.oose.bikeApp.model.Message;
import edu.curtin.oose.bikeApp.model.ServiceState;
public class BankObserver implements MessageObserver {
    private BikeStore store;
    private final static Logger LOG = Logger.getLogger(BikeStore.class.getName());

    public BankObserver(BikeStore inStore, MessageHandler inSubject)
    {
        store = inStore;
    }
    
    @Override
    public void update(Message event)
    {
        int currentCash = store.getCash();
        int newCash = currentCash;
        String message = event.getMessage();
        Bike bike = event.getBikeInfo();
        if(event.getSuccess())
        {
            switch(message)
            {
                case "DELIVERY" ->{
                    newCash = currentCash - 5000;
                    LOG.log(Level.FINE, "Bank updated: -5000");
                }
                case "PURCHASE-ONLINE" -> {
                    newCash = currentCash + 1000;
                    LOG.log(Level.FINE, "Bank updated: +1000");
                }
                case "PURCHASE-IN-STORE" -> {
                    newCash = currentCash + 1000;
                    LOG.log(Level.FINE, "Bank updated: +1000");
                }
                case "PICK-UP" -> {
                    if(bike.availableForPickup())
                    {
                        newCash = currentCash + 100;
                        LOG.log(Level.FINE, "Bank updated: +100");
                    }
                }
            }
        }
        store.updateCash(newCash);
    }
}
