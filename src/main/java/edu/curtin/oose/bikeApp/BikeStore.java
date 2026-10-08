package edu.curtin.oose.bikeApp;

public class BikeStore {
    private int cash = 15000;
    private int availableBikes = 50;
    public final static int BIKESPACE = 100;
    private int totalBikes = 50;


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

    public int getTotalBikes()
    {
        return totalBikes;
    }
}
