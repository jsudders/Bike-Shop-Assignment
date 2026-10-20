package edu.curtin.oose.bikeApp.model;

public class Bike {
    // state BikeState
    private final String customerEmail;
    private BikeState bikeState = new AvailableState();
    private int serviceTime;
    private final String bikeID;
    private Boolean canPickup = true;

    public Bike(String inEmail, String inBikeID)
    {
        this.customerEmail = inEmail;
        this.bikeID = inBikeID;
        this.bikeState = new AvailableState();
        this.serviceTime = 0;
    }

    public String getEmail()
    {
        return customerEmail;
    }

    public BikeState getState()
    {
        return bikeState;
    }

    public void setState(BikeState state)
    {
        bikeState = state;
    }

    public String getBikeID()
    {
        return bikeID;
    }

    public int getTime()
    {
        return serviceTime;
    }

    public void putOnHold()
    {
        bikeState.putOnHold(this);
    }

    public void serviceBike()
    {
        bikeState.serviceBike(this);
    }
    
    public void elapseDay()
    {
        bikeState.elapseDay(this);
    }
    
    public void setTime(int newTime)
    {
        serviceTime = newTime;
    }

    public void setPickup(boolean isAvailable)
    {
        canPickup = isAvailable;
    }

    public boolean checkPickup()
    {
        return canPickup;
    }

}
