package edu.curtin.oose.bikeApp.model;


public interface BikeState {

    public void putOnHold(Bike bike);
    
    public void serviceBike(Bike bike);

    public void elapseDay(Bike bike);

}
