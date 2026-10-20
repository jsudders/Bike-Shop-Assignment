package edu.curtin.oose.bikeApp.view;

import edu.curtin.oose.bikeApp.BikeStore;
import edu.curtin.oose.bikeApp.model.Message;

import java.util.*;
import java.io.*;
import java.util.logging.Logger;
import java.util.logging.Level;

public class NotificationObserver implements MessageObserver{
    private final BikeStore store;
    private String output;
    private final FileObserver fileOb;
    private static final Logger LOG = Logger.getLogger(NotificationObserver.class.getName());

    public NotificationObserver(BikeStore inStore, FileObserver inOb)
    {
        store = inStore;
        fileOb = inOb;
    }

    public void updateObservers()
    {
        fileOb.update(output);
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
        updateObservers();
    }

    public void displayMessage() {
        System.out.println(output);
    }
}
