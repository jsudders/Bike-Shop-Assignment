package edu.curtin.oose.bikeApp.model;

public class Message {
    public String message;
    public String email;
    public Bike bikeInfo;
    public boolean success;

    public Message(String inMessage, String inEmail, Bike inBikeInfo, boolean inSuccess)
    {
        this.message = inMessage;
        this.email = inEmail;
        this.bikeInfo = inBikeInfo;
        this.success = inSuccess;
    }

}
