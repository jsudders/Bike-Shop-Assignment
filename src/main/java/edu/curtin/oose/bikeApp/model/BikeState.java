package edu.curtin.oose.bikeApp.model;

public interface BikeState {
    public void pickUp(Bike bike);

    public void putOnHold(Bike bike);

    public void serviceBike(Bike bike);

    public void sellBike(Bike bike);
}
