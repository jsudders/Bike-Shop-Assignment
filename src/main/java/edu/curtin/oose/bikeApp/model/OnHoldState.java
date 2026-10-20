package edu.curtin.oose.bikeApp.model;

import java.util.logging.Level;
import java.util.logging.Logger;

// OnHoldState does not do anything really, it is more as a check for pickup
public class OnHoldState implements BikeState{
    private final static Logger LOG = Logger.getLogger(OnHoldState.class.getName());
    @Override
    public void putOnHold(Bike bike)
    {
        LOG.log(Level.SEVERE, "Bike on hold attempting to be put on hold again");
    }

    @Override
    public void serviceBike(Bike bike)
    {
        LOG.log(Level.SEVERE, "Bike on hold attempting to be put in service state");
    }

    @Override
    public void elapseDay(Bike bike)
    {
        // nothing to do
    }
}
