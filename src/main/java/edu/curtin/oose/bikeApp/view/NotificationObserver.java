package edu.curtin.oose.bikeApp.view;

import edu.curtin.oose.bikeApp.BikeStore;
import edu.curtin.oose.bikeApp.model.Message;
public class NotificationObserver implements MessageObserver{
    private final BikeStore store;
    private String output;

    public NotificationObserver(BikeStore inStore)
    {
        store = inStore;
    }

    @Override
    public void update(Message event)
    {
        this.output = "\n" + event.getMessage();
        store.incrementMessages();

        if(!(event.getEmail()).isEmpty())
        {
            output += " WITH EMAIL: " + event.getEmail(); 
        }


        if(event.getSuccess())
        {
            output += "\nSUCCESS";
        }
        else
        {
            output += "\nFAILURE: " + event.getFailure();
            store.incrementFailures();
        }

        displayMessage();
    }

    public void displayMessage() {
        System.out.println(output);
    }
}
