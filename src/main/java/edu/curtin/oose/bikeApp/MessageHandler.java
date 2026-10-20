package edu.curtin.oose.bikeApp;

import java.util.ArrayList;
import java.util.List;

import edu.curtin.oose.bikeApp.model.Bike;
import edu.curtin.oose.bikeApp.model.Message;
import edu.curtin.oose.bikeApp.view.MessageObserver;

public class MessageHandler {
    private final List<MessageObserver> observers;

    public MessageHandler()
    {
        observers = new ArrayList<>();
    }
    
    public void addObserver(MessageObserver ob)
    {
        observers.add(ob);
    }

    public void removeObserver(MessageObserver ob)
    {
        observers.remove(ob);
    }

    public void updateObservers(Message event)
    {
        for(MessageObserver ob : observers)
        {
            ob.update(event);
        }
    }



    public void handleMessage(BikeStore store, String message)
    {
        String[] messageParts = message.strip().split(" ");
        String email = "";
        if(messageParts.length == 2)
        {
            email = messageParts[1];
        }

        
        Message event = new Message(messageParts[0], email, false);
        
        // storage observer manages the bike states and store availability, setting bikes to OnHold, Service, Sold, etc.
        // bank observer manages the store's cash
        // notification observer will print the message


        switch(messageParts[0])
        {
            case "DELIVERY" -> {
                if((store.getTotalBikes() <= 90) && (store.getCash() >= 10000))
                {
                    event.setSuccess(true);
                    event.setEmail(""); // no email needed
                }
            }
            case "DROP-OFF" -> {
                if((store.getTotalBikes() <= store.BIKESPACE - 1))
                {
                    event.setSuccess(true);
                    // if the bike store has room for the bike, set the event as a success
                    // storageObserver manages putting the bike on hold
                }
            }
            case "PURCHASE-ONLINE", "PURCHASE-IN-STORE" -> {
                    if(store.getAvailableBikes() >= 1)
                    {
                        event.setSuccess(true);
                        // if the bike store has an available bike, set the event success to true,
                    }
                }
            case "PICK-UP" -> {
                Bike pickedUp = store.attemptPickup(event);
                if(pickedUp != null)
                {
                    event.setBikeInfo(pickedUp);
                    event.setSuccess(true);
                }

            }
            default -> {
                event.setSuccess(false);
                event.setFailure(1);
            }
        }

        updateObservers(event);

    }
}