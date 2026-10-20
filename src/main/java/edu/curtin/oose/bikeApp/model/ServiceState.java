package edu.curtin.oose.bikeApp.model;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ServiceState implements BikeState{
    private final static Logger LOG = Logger.getLogger(ServiceState.class.getName());

    @Override
    public void putOnHold(Bike bike)
    {
        LOG.log(Level.SEVERE, "Bike in service attempted to be put on hold");
    }

    @Override
    public void serviceBike(Bike bike)
    {
        LOG.log(Level.WARNING, "Bike in service attempting to be serviced again");
    }

    @Override
    public void elapseDay(Bike bike)
    {
        LOG.log(Level.FINE, "Day elapsed for bike with ID: {0}", bike.getBikeID());
        bike.setTime(bike.getTime() + 1);
        if(bike.getTime() >= 2)
        {
            bike.setPickup(true);
            LOG.log(Level.INFO, "Bike from email: {0} ready for pickup from being serviced", bike.getEmail());
        }
    }

}
