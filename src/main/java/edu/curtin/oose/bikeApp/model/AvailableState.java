package edu.curtin.oose.bikeApp.model;


public class AvailableState implements BikeState 
{

    @Override
    public void putOnHold(Bike bike)
    {
        bike.setState(new OnHoldState());
    }

    @Override
    public void serviceBike(Bike bike)
    {
        bike.setPickup(false); // resets the pickup availability
        bike.setTime(0); // resets time just in case
        bike.setState(new ServiceState());
    }

    @Override
    public void elapseDay(Bike bike)
    {
        // nothing to do
    }

}
