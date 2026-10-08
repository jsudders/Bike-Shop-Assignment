package edu.curtin.oose.bikeApp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.curtin.oose.bikeApp.model.Message;
import edu.curtin.oose.bikeApp.view.MessageObserver;

public class MessageHandler {
    private List<MessageObserver> observers;

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

        Message event = new Message(messageParts[0], messageParts[1], null, false);

        switch(messageParts[0])
        {
            case "DELIVERY":
                if((store.getTotalBikes() <= 90) && (store.getCash() >= 10000))
                {
                    event.success = true;
                    event.email = "";
                }
                break;
            case "DROP-OFF":
                if((store.getTotalBikes() <= 99))
                {
                    event.success = true;
                }
                break;
            case "PURCHASE-ONLINE": case "PURCHASE-IN-STORE": // may need to updte for time until pickup
                if(store.getAvailableBikes() >= 1)
                {
                    event.success = true;
                }
                break;
            case "PICK-UP":
                // if email address matches one previously supplied
                // if 2 days of simulated time
                break;
            default:
                event = null;
                break;
        }  
        
        updateObservers(event);
    }
    public static boolean msgIsValid(String message)
    {
        boolean valid = false;
        // preset valid to false so that if the message does not match any valid starts, false is passed


        List<String> validStarts = new ArrayList<>(Arrays.asList("DELIVERY", "PURCHASE-IN-STORE"));
        List<String> emailStarts = new ArrayList<>(Arrays.asList("DROP-OFF", "PURCHASE-ONLINE", "PICK-UP"));
        // define the valid starts that either need no extra info or an email

        String[] messageParts = message.strip().split(" ");
        // split the message into parts in case it has an email

        switch(messageParts.length)
        {
            case 1: // if the message only has 1 string and it starts with one of the valid starts, return true
                if(validStarts.contains(message))
                {
                    valid = true;
                }
                break;
            case 2: // if the message has 2 strings and starts with any of the starts that need emails, return true
                if(emailStarts.contains(messageParts[0]))
                {
                    valid = true;
                }
                break;
            default: // else if the message has more than 2 parts, return false
                valid = false;
                
        }
        
        return valid;
    }


}
