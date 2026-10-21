package edu.curtin.oose.bikeApp;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import edu.curtin.oose.bikeApp.model.AvailableState;
import edu.curtin.oose.bikeApp.model.Bike;
import edu.curtin.oose.bikeApp.model.BikeFactory;
import edu.curtin.oose.bikeApp.model.Message;
import edu.curtin.oose.bikeApp.model.OnHoldState;
import edu.curtin.oose.bikeApp.model.ServiceState;
import edu.curtin.oose.bikeApp.view.FileObserver;
import edu.curtin.oose.bikeApp.view.MessageObserver;
import edu.curtin.oose.bikeApp.view.ObserverFactory;

/* use factories and stuff */
public class BikeStoreTest
{

    @Test
    void testBikeStore()
    {
        FileObserver fileOb = mock(FileObserver.class);
        BikeStore bikeStore = new BikeStore(mock(FileObserver.class));
        var mockObserverFactory = mock(ObserverFactory.class);
        var mockMessageObserver = mock(MessageObserver.class);
        var mockBFactory = mock(BikeFactory.class);
        var mockBike = mock(Bike.class);
        when(mockObserverFactory.createBank()).thenReturn(mockMessageObserver);   



    }

    @Test
    void testServiceBike()
    {
        BikeStore bikeStore = new BikeStore(mock(FileObserver.class));

        String testEmail = "sdfs@ksme.com";
        String id = (UUID.randomUUID()).toString();

        var mockBike = mock(Bike.class);

        when(mockBike.getEmail()).thenReturn(testEmail);
        when(mockBike.getBikeID()).thenReturn(id);

        bikeStore.serviceBike(mockBike);

        assertEquals(1, bikeStore.getServicedBikesCount());
        assertEquals(51, bikeStore.getTotalBikes());
        

        
    }

    @Test
    void testHoldBike()
    {
        BikeStore bikeStore = new BikeStore(mock(FileObserver.class));

        String testEmail = "sdfs@ksme.com";
        String id = (UUID.randomUUID()).toString();
        
        var mockBike = mock(Bike.class);

        when(mockBike.getEmail()).thenReturn(testEmail);
        when(mockBike.getBikeID()).thenReturn(id);
        when(mockBike.getState()).thenReturn(new AvailableState());

        bikeStore.holdBike(mockBike);

        assertEquals(1, bikeStore.getHeldBikesCount());
        assertEquals(50, bikeStore.getTotalBikes());
        // takes one from available bikes
    }


    @Test
    void testAttemptPickupService()
    {
        BikeStore bikeStore = new BikeStore(mock(FileObserver.class));

        String testEmail = "sdfs@ksme.com";
        String id = (UUID.randomUUID()).toString();

        var mockBike = mock(Bike.class);

        when(mockBike.getEmail()).thenReturn(testEmail);
        when(mockBike.getBikeID()).thenReturn(id);
        

        bikeStore.serviceBike(mockBike);


        var mockBikeFactory = mock(BikeFactory.class);
        when(mockBike.checkPickup()).thenReturn(true);
        when(mockBike.getTime()).thenReturn(2);

        
        when(mockBikeFactory.createBike(testEmail, id)).thenReturn(mockBike);

        var mockMessage = mock(Message.class);
        when(mockMessage.getEmail()).thenReturn(testEmail);


        assertEquals(bikeStore.attemptPickup(mockMessage), mockBike);
    }

    @Test
    void testAttemptPickupHeld()
    {
        BikeStore bikeStore = new BikeStore(mock(FileObserver.class));

        String testEmail = "sdfs@ksme.com";
        String id = (UUID.randomUUID()).toString();
        var mockBike = mock(Bike.class);

        when(mockBike.getEmail()).thenReturn(testEmail);
        when(mockBike.getBikeID()).thenReturn(id);


        bikeStore.holdBike(mockBike);
        var mockBikeFactory = mock(BikeFactory.class);
    
        var mockMessage = mock(Message.class);
        when(mockMessage.getEmail()).thenReturn(testEmail);

        assertEquals(bikeStore.attemptPickup(mockMessage), mockBike);
    }

    @Test
    void testRemoveService()
    {
        BikeStore bikeStore = new BikeStore(mock(FileObserver.class));

        String testEmail = "sdfs@ksme.com";
        String id = (UUID.randomUUID()).toString();
        var mockBike = mock(Bike.class);

        when(mockBike.getEmail()).thenReturn(testEmail);
        when(mockBike.getBikeID()).thenReturn(id);

        bikeStore.serviceBike(mockBike);

        when(mockBike.checkPickup()).thenReturn(true);
        when(mockBike.getTime()).thenReturn(2);
        when(mockBike.getState()).thenReturn(new ServiceState());

        assertEquals(true, bikeStore.removeBike(mockBike));

        assertEquals(0, bikeStore.getServicedBikesCount());

        assertEquals(50, bikeStore.getTotalBikes());
        

    }

    @Test
    void testRemoveHeld()
    {
         BikeStore bikeStore = new BikeStore(mock(FileObserver.class));

        String testEmail = "sdfs@ksme.com";
        String id = (UUID.randomUUID()).toString();
        var mockBike = mock(Bike.class);

        when(mockBike.getEmail()).thenReturn(testEmail);
        when(mockBike.getBikeID()).thenReturn(id);

        when(mockBike.getState()).thenReturn(new AvailableState());

        bikeStore.holdBike(mockBike);

        when(mockBike.getState()).thenReturn(new OnHoldState());

        assertEquals(true, bikeStore.removeBike(mockBike));

        assertEquals(0, bikeStore.getHeldBikesCount());

        assertEquals(49, bikeStore.getTotalBikes());
    }

}

