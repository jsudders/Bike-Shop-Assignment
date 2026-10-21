package edu.curtin.oose.bikeApp.model;

public class MessageFactory {
    private BikeFactory bFactory;
    public MessageFactory(BikeFactory inFactory)
    {
        bFactory = inFactory;
    }
    public Message createMessage(String message, String email, boolean success)
    {
        return new Message(message, email, success, bFactory);
    }
}
