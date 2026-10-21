package edu.curtin.oose.bikeApp.model;

public class BikeFactory {
    public Bike createBike(String email, String bikeID)
    {
        return new Bike(email, bikeID);
    }
}
