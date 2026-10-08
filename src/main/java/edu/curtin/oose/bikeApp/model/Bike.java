package edu.curtin.oose.bikeApp.model;

public class Bike {
    // state BikeState
    private String customerEmail;
    private BikeState bikeState = new AvailableState();
    private int holdTime = 0;

    public Bike(String inEmail, String message)
    {
        this.customerEmail = inEmail;

        switch(message)
        {
            case "DROP-OFF":
                this.bikeState = new ServiceState();
                break;
            case "PURCHASE-ONLINE":
                this.bikeState = new OnHoldState();
                break;
        }
    }

    public String getEmail()
    {
        return customerEmail;
    }

    public BikeState getState()
    {
        return bikeState;
    }

    public int getTime()
    {
        return holdTime;
    }

    public void pickUp()
    {
        bikeState.pickUp(this);
    }

    public void putOnHold()
    {
        bikeState.putOnHold(this);
    }
    
}
