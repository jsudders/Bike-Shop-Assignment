package edu.curtin.oose.bikeApp.model;

import edu.curtin.oose.bikeApp.model.*;

public class Bike {
    // state BikeState
    private String customerEmail;
    private BikeState bikeState = new AvailableState();
    private int holdTime = 0;
    private String bikeID;
    private Boolean canPickup = true;

    public Bike(String inEmail, String inBikeID)
    {
        this.customerEmail = inEmail;
        this.bikeID = inBikeID;
        this.bikeState = new AvailableState();

        // switch(message)
        // {
        //     case "DROP-OFF":
        //         this.bikeState = new ServiceState();
        //         break;
        //     case "PURCHASE-ONLINE":
        //         this.bikeState = new OnHoldState();
        //         break;
        // }
    }

    public String getEmail()
    {
        return customerEmail;
    }

    public BikeState getState()
    {
        return bikeState;
    }

    public String getBikeID()
    {
        return bikeID;
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
    
    public void elapseDay()
    {
        if((bikeState instanceof OnHoldState) || (bikeState instanceof ServiceState))
        {
            holdTime += 1;

            availableForPickup();
        }
        
    }

    public boolean availableForPickup()
    {
        return canPickup = (this.bikeState instanceof ServiceState) && (this.holdTime >= 2);
        // if the bike is currently being serviced and the hold time is greater than 2
    }


    public boolean checkPickup()
    {
        return canPickup;
    }
}
