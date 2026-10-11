package edu.curtin.oose.bikeApp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.curtin.oose.bikeApp.model.Bike;
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
        String email = "";
        if(messageParts.length == 2)
        {
            email = messageParts[1];
        }

        
        Message event = new Message(messageParts[0], email, false);
        


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
                if((store.getTotalBikes() <= 99))
                {
                    event.setSuccess(true);
                }
            }
            case "PURCHASE-ONLINE", "PURCHASE-IN-STORE" -> {
                // may need to update for time until pickup
                if(store.getAvailableBikes() >= 1)
                {
                    event.setSuccess(true);
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


        if(msgIsValid(message) == 1)
        {
            event.setSuccess(false);
        }

        updateObservers(event);
    }
    public static int msgIsValid(String message)
    {
        int invalid = 1;
        // preset invalid to 1 which is the case of a message string that needs an email not having one

        List<String> validStarts = new ArrayList<>(Arrays.asList("DELIVERY", "PURCHASE-IN-STORE"));
        List<String> emailStarts = new ArrayList<>(Arrays.asList("DROP-OFF", "PURCHASE-ONLINE", "PICK-UP"));
        // define the valid starts that either need no extra info or an email

        String[] messageParts = message.strip().split(" ");
        // split the message into parts in case it has an email

        switch(messageParts.length)
        {
            case 1 -> {
                // if the message only has 1 string and it starts with one of the valid starts, return true
                if(validStarts.contains(message))
                {
                    invalid = 0;
                }
            }
            case 2 -> {
                // if the message has 2 strings and starts with any of the starts that need emails, return true
                if(emailStarts.contains(messageParts[0]))
                {
                    invalid = 0;
                }
            }
            default -> // else if the message has more than 2 parts, return false
                invalid = 1;
                
        }
        
        return invalid;
    }


}
