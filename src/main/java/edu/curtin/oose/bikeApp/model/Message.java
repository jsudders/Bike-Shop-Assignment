package edu.curtin.oose.bikeApp.model;

import java.util.Map;
import java.util.UUID;

public class Message {
    private String message;
    private String email;
    private Bike bikeInfo;
    private int failure;
    private boolean success;
    private Map<Integer, String> failureMap = Map.of(
        0, "",
        1, "Invalid message",
        2, "Not enough space",
        3, "No bikes left",
        4, "No bike matching customer email",
        5, "Bike not ready"
    );

    public Message(String inMessage, String inEmail, boolean inSuccess)
    {
        this.message = inMessage;
        this.email = inEmail;
        this.success = inSuccess;
        this.bikeInfo = null;

        if(!(message.equals("DELIVERY")))
        {
            UUID bikeUUID = UUID.randomUUID();
            Bike newBike = new Bike(email, bikeUUID.toString());
    
            this.bikeInfo = newBike;
        }
    }

    public String getFailure(int failureInt)
    {
        return failureMap.get(failureInt);
    }

    public String getMessage()
    {
        return message;
    }

    public String getEmail()
    {
        return email;
    }

    public Bike getBikeInfo()
    {
        return bikeInfo;
    }

    public int getFailure()
    {
        return failure;
    }

    public boolean getSuccess()
    {
        return success;
    }

    public void setEmail(String inEmail)
    {
        this.email = inEmail;
    }

    public void setBikeInfo(Bike inBike)
    {
        this.bikeInfo = inBike;
    }

    public void setSuccess(boolean inSuccess)
    {
        this.success = inSuccess;
    }

    public void setFailure(int inFailure)
    {
        this.failure = inFailure;
    }
}
