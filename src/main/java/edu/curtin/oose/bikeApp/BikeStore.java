package edu.curtin.oose.bikeApp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.curtin.oose.bikeApp.model.AvailableState;
import edu.curtin.oose.bikeApp.model.Bike;
import edu.curtin.oose.bikeApp.model.Message;
import edu.curtin.oose.bikeApp.model.OnHoldState;
import edu.curtin.oose.bikeApp.model.ServiceState;

public class BikeStore {
    private int cash = 15000;
    private int availableBikes = 50;
    public final int BIKESPACE = 100;
    private final Map<String, List<Bike>> servicedBikes = new HashMap<>();
    private final Map<String, List<Bike>> heldBikes = new HashMap<>();
    // using HashMap because it has better time complexity and bikes do not need to be in order
    // using List<Bike> because customers can have more than one bike
    private final static Logger LOG = Logger.getLogger(BikeStore.class.getName());
    private int time = 0;
    private int totalMessages = 0;
    private int totalFailures = 0;


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
        for(List<Bike> serviceBikeList : servicedBikes.values())
        {
            for(Bike serviceBike : serviceBikeList )
            {
                serviceBike.elapseDay();
            }
        }

        if(time % 7 == 0)
        {
            updateCash(cash - 1000); // pay employee every 7 days
        }

        printStats();
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
            bike.serviceBike();
            servicedBikes.get(custEmail).add(bike); // adds bike to list

        }
        else
        {
            List<Bike> custBikes = new ArrayList<>();
            bike.serviceBike();
            custBikes.add(bike);
            servicedBikes.put(custEmail, custBikes);
        }
        
        LOG.log(Level.INFO, "Bike with ID {0} added to Service List", bike.getBikeID());
    }

    public void holdBike(Bike bike)
    {
        String custEmail = bike.getEmail().trim();
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
            heldBikes.get(custEmail).add(bike);
            
        }
        else
        {
            List<Bike> custBikes = new ArrayList<>();
            custBikes.add(bike);
            heldBikes.put(custEmail, custBikes);
        }

        if(bike.getState() instanceof AvailableState)
        {
            availableBikes -= 1;
        }

        
        bike.putOnHold();

        LOG.log(Level.INFO, "Bike with ID {0} added to Held List", bike.getBikeID());

    }

    public Bike attemptPickup(Message message)
    {
        String email = message.getEmail().trim();
        if(heldBikes.isEmpty())
        {
            System.out.println("heldBikes is empty");
        }
        if(servicedBikes.isEmpty())
        {
            System.out.println("Services bikes is empty");
        }
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
                System.out.println(bike.getTime());
                System.out.println(bike.checkPickup());
                if(bike.checkPickup())
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
        /* PICK-UP WITH EMAIL: lnrsi@htf.gax
SUCCESS
Oct 20, 2026 12:01:51 PM edu.curtin.oose.bikeApp.BikeStore removeBike
INFO: Successfully removed bike of customer [lnrsi@htf.gax] from Held Bikes list */
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

// TODO: remove from list after pickup on service
// method overload
    public boolean sellBike()
    {
        if(availableBikes != 0)
        {
            availableBikes -= 1;
            return true;
        }
        return false;
    }
    
    public void incrementMessages()
    {
        totalMessages += 1;
    }

    public void incrementFailures()
    {
        totalFailures += 1;
    }


    public void printStats()
    {
        System.out.println("\nDays elapsed: " + time);
        System.out.println("Total cash: " + cash);
        System.out.println("Bikes available for purchase " + availableBikes);
        System.out.println("Bikes being serviced: " + servicedBikes.size());
        System.out.println("Bikes awaiting pickup: " + heldBikes.size() + "\n");
    }

    public void printFinalStats()
    {
        System.out.println("Total messages: " + totalMessages);
        System.out.println("Total failures: " + totalFailures);
    }
}
