package edu.curtin.oose.bikeApp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.*;

import edu.curtin.oose.bikeApp.model.AvailableState;
import edu.curtin.oose.bikeApp.model.Bike;
import edu.curtin.oose.bikeApp.model.Message;
import edu.curtin.oose.bikeApp.model.OnHoldState;
import edu.curtin.oose.bikeApp.model.ServiceState;

public class BikeStore {
    private int cash = 15000;
    private int availableBikes = 50;
    public final static int BIKESPACE = 100;
    private Map<String, List<Bike>> servicedBikes = new HashMap<>();
    private Map<String, List<Bike>> heldBikes = new HashMap<>();
    // using HashMap because it has better time complexity and bikes do not need to be in order
    // using List<Bike> because customers can have more than one bike
    private final static Logger LOG = Logger.getLogger(BikeStore.class.getName());
    private int time = 0;


    public void updateCash(int inCash)
    {
        cash = inCash;
    }

    public int getCash()
    {
        return cash;
    }

    public int getAvailableBikes()
    {
        return availableBikes;
    }

    public void getDelivery()
    {
        availableBikes += 10;
    }

    public int getTotalBikes()
    {
        int totalBikes = getAvailableBikes() + getHeldBikesCount() + getServicedBikesCount();

        return totalBikes;
    }

    public void elapseDay()
    {
        time += 1;
    }

    public int getTime()
    {
        return time;
    }


    public int getServicedBikesCount()
    {
        int totalServicedBikes = 0;
        for(List<Bike> custServiceBikes : servicedBikes.values())
        {
            totalServicedBikes += custServiceBikes.size();
        }
        LOG.log(Level.FINER, "Total Bikes being Serviced = {0}", totalServicedBikes);
        return totalServicedBikes;
    }

    public int getHeldBikesCount()
    {
        int totalHeldBikes = 0;
        for(List<Bike> custHoldBikes : heldBikes.values())
        {
            totalHeldBikes += custHoldBikes.size();
        }
        LOG.log(Level.FINER, "Total Held Bikes = {0}", totalHeldBikes);
        return totalHeldBikes;
    }

    public void serviceBike(Bike bike)
    {
        String custEmail = bike.getEmail();
        
        
        if(servicedBikes.containsKey(custEmail))
        {
            List<Bike> preExistingBikes = servicedBikes.get(custEmail);
            for(Bike existingBike : preExistingBikes)
            {
                if(existingBike.getBikeID().equals(bike.getBikeID()))
                {
                    // bike is already being serviced
                    LOG.severe("Bike is already being serviced");
                    return;
                }
            }
            
        }
        //TODO: this above may not be necessary at all

        bike.serviceBike(); // bike is not in the servicedBikes list, but customer may already be in list
        List<Bike> custsBikes = servicedBikes.get(custEmail);
        custsBikes.add(bike);
        servicedBikes.put(custEmail, custsBikes);
        LOG.log(Level.INFO, "Bike with ID {0} added to Service List", bike.getBikeID());
    }

    public void holdBike(Bike bike)
    {
        String custEmail = bike.getEmail();

        if(heldBikes.containsKey(custEmail))
        {
            List<Bike> preExistingBikes = heldBikes.get(custEmail);
            for(Bike existingBike : preExistingBikes)
            {
                if(existingBike.getBikeID().equals(bike.getBikeID()))
                {
                    // bike is already being serviced
                    LOG.severe("Bike is already being held");
                    return;
                }
            }
            
        }

        if(bike.getState() instanceof AvailableState)
        {
            availableBikes -= 1;
        }

        List<Bike> custBikes = heldBikes.get(custEmail);
        custBikes.add(bike);
        heldBikes.put(custEmail, custBikes);

        LOG.log(Level.INFO, "Bike with ID {0} added to Held List", bike.getBikeID());

    }

    public Bike attemptPickup(Message message)
    {
        String email = message.getEmail();

        if(!heldBikes.containsKey(email) && !servicedBikes.containsKey(email))
        {
            message.setFailure(4); // no bike matching customer email
            LOG.log(Level.INFO, "Failure on pickup, no bike matching customer email");

            return null; // return null bike as there are no bikes available
        }
        if(heldBikes.containsKey(email))
        {
            return heldBikes.get(email).getFirst();
        }

        if(servicedBikes.containsKey(email))
        {
            List<Bike> bikeList = servicedBikes.get(email);
            // also check if bike has been serviced
            for(Bike bike : bikeList)
            {
                if(bike.availableForPickup())
                {
                    return bike;
                }
            }
        }

        message.setFailure(5); // bike not ready
        // if the bike hasn't been returned yet, that means its not ready 
        LOG.log(Level.INFO, "Failure on pickup, bike not ready");
        return null;
    }

    public boolean removeBike(Bike bike)
    {
        String custEmail = bike.getEmail();

        if(bike.getState() instanceof OnHoldState)
        {
            List<Bike> custHelds = heldBikes.get(custEmail);
            for(Bike heldBike : custHelds)
            {
                if(heldBike.getBikeID().equals(bike.getBikeID()))
                {
                    custHelds.remove(heldBike);
                    LOG.log(Level.INFO, "Successfully removed bike of customer [{0}] from Held Bikes list", custEmail);
                    return true;
                }
            }
        }
        else if(bike.getState() instanceof ServiceState)
        {
            List<Bike> custServiced = servicedBikes.get(custEmail);
            for(Bike servicedBike : custServiced)
            {
                if(servicedBike.getBikeID().equals(bike.getBikeID()))
                {
                    custServiced.remove(servicedBike);
                    LOG.log(Level.INFO, "Successfully removed bike of customer [{0}] from Service List", custEmail);
                    return true;
                }
            }
        }

        LOG.log(Level.SEVERE, "Could not remove bike under customer [{0}] from Held List or Serviced List", custEmail);
        return false;
    }
    
}
