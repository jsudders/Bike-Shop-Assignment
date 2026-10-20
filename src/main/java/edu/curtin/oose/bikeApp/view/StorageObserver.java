package edu.curtin.oose.bikeApp.view;
import edu.curtin.oose.bikeApp.BikeStore;
import edu.curtin.oose.bikeApp.model.Bike;
import edu.curtin.oose.bikeApp.model.Message;
public class StorageObserver implements MessageObserver{
    private final BikeStore store;

    public StorageObserver(BikeStore inStore)
    {
        store = inStore;
    }
    
    @Override
    public void update(Message event)
    {
        
        String message = event.getMessage();
        Bike bike = event.getBikeInfo();

        if(event.getSuccess())
        {
            switch(message)
            {
                case "DELIVERY" -> {
                    store.getDelivery();
                }
                case "DROP-OFF" -> {
                    store.serviceBike(bike);
                }
                case "PURCHASE-ONLINE" -> {
                    store.holdBike(bike);
                }
                case "PURCHASE-IN-STORE" -> {
                    store.sellBike();
                }
                case "PICK-UP" -> {
                    // already checked whether there is a valid bike available for service in the subject and store
                    store.removeBike(bike);
                    // removes the bike from either the held or serviced list
                    }
                }
            }

        }
}
