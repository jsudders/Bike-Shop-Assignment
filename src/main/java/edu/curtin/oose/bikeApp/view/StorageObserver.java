package edu.curtin.oose.bikeApp.view;
import edu.curtin.oose.bikeApp.BikeStore;
import edu.curtin.oose.bikeApp.model.Bike;
import edu.curtin.oose.bikeApp.model.Message;
import edu.curtin.oose.bikeApp.model.OnHoldState;
public class StorageObserver implements MessageObserver{
    private BikeStore store;

    public StorageObserver(BikeStore inStore)
    {
        store = inStore;
    }
    
    @Override
    public void update(Message event)
    {
        
        String message = event.getMessage();

        if(event.getSuccess())
        {
            switch(message)
            {
                case "DELIVERY" -> {
                    store.getDelivery();
                }
                case "DROP-OFF" -> {
                    
                }
                case "PURCHASE-ONLINE" -> {
    
                }
                case "PURCHASE-IN-STORE" -> {
    
                }
                case "PICK-UP" -> {
                    Bike pickedUp = event.getBikeInfo();
                    if(pickedUp != null)
                    {
                        if(pickedUp.getState() instanceof OnHoldState)
                        {
                            store.removeBike(pickedUp);
                            // TODO: returns boolean, maybe do something with that
                        }
                    }
                }
            }

        }
    }
}