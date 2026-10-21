package edu.curtin.oose.bikeApp;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.curtin.oose.bikeApp.model.Bike;
import edu.curtin.oose.bikeApp.model.Message;
import edu.curtin.oose.bikeApp.model.MessageFactory;
import edu.curtin.oose.bikeApp.view.MessageObserver;

public class MessageHandler {
    private final List<MessageObserver> observers;
    private final static Logger LOG = Logger.getLogger(BikeStore.class.getName());
    private final MessageFactory mFactory;

    public MessageHandler(MessageFactory inMessageFactory)
    {
        observers = new ArrayList<>();
        mFactory = inMessageFactory;
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
        LOG.log(Level.FINEST, "Message being handled: {0}", message);
        String[] messageParts = message.strip().split(" ");
        String email = "";
        if(messageParts.length == 2)
        {
            email = messageParts[1];
        }

        
        Message event = mFactory.createMessage(messageParts[0], email, false);
        
        // storage observer manages the bike states and store availability, setting bikes to OnHold, Service, Sold, etc.
        // bank observer manages the store's cash
        // notification observer will print the message


        switch(messageParts[0])
        {
            case "DELIVERY" -> {
                if((store.getTotalBikes() <= store.BIKESPACE - 10))
                {
                    if(store.getCash() >= 10000)
                    {
                        event.setSuccess(true);
                        event.setEmail(""); // no email needed
                    }
                    else
                    {
                        event.setFailure(6); // not enough cash
                    }
                }
                else
                {
                    event.setFailure(2); // not enough space
                }

            }
            case "DROP-OFF" -> {
                if((store.getTotalBikes() <= store.BIKESPACE - 1))
                {
                    event.setSuccess(true);
                    // if the bike store has room for the bike, set the event as a success
                    // storageObserver manages putting the bike on hold
                }
                else
                {
                    event.setFailure(2); // not enough space
                }
            }
            case "PURCHASE-ONLINE", "PURCHASE-IN-STORE" -> {
                    if(store.getAvailableBikes() >= 1)
                    {
                        event.setSuccess(true);
                        // if the bike store has an available bike, set the event success to true,
                    }
                    else
                    {
                        event.setFailure(3); // no bikes left
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
                event.setFailure(1); // invalid message
            }
        }

        updateObservers(event);

    }
}