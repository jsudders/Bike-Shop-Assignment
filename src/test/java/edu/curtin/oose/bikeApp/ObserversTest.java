package edu.curtin.oose.bikeApp;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import edu.curtin.oose.bikeApp.model.Message;
import edu.curtin.oose.bikeApp.model.MessageFactory;
import edu.curtin.oose.bikeApp.view.ObserverFactory;

public class ObserversTest {
    @Test
    void bankDelivery()
    {
        String message = "DELIVERY";
        var mockEvent = mock(Message.class);

        var mockMessageFactory = mock(MessageFactory.class);
        
        var mockObserverFactory = mock(ObserverFactory.class);
        var mockStore = mock(BikeStore.class);

        //BankObserver bank = new BankObserver(mockStore);

        var mockHandler = mock(MessageHandler.class);

        when(mockEvent.getMessage()).thenReturn(message);
        when(mockEvent.getBikeInfo()).thenReturn(null);

        //assertEquals(mockStore.cash, 50);



        when(mockStore.getCash()).thenReturn(10000);
        when(mockStore.getTotalBikes()).thenReturn(50);



        //// failed because of cash

        when(mockStore.getCash()).thenReturn(500);
        
        //handler.handleMessage(mockStore, message);

        assertEquals(60, mockStore.getTotalBikes()); // no change
        assertEquals(5000, mockStore.getCash());

        // failed because of room

        when(mockStore.getTotalBikes()).thenReturn(96);
        assertEquals(60, mockStore.getTotalBikes()); // no change
        assertEquals(5000, mockStore.getCash());

    }
}
