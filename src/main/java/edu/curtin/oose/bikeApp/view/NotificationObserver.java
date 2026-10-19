package edu.curtin.oose.bikeApp.view;

import edu.curtin.oose.bikeApp.model.Message;
public class NotificationObserver implements MessageObserver{


    @Override
    public void update(Message event)
    {
        String message = event.getMessage();

        switch(message)
        {
            case "DELIVERY" -> {

            }
            case "DROP-OFF" -> {

            }
            case "PURCHASE-ONLINE" -> {

            }
            case "PURCHASE-IN-STORE" -> {

            }
            case "PICK-UP" -> {
                
            }
        }
    }
}
