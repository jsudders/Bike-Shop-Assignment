package edu.curtin.oose.bikeApp;

import edu.curtin.oose.bikeApp.*;
import java.io.*;
import edu.curtin.oose.bikeApp.view.*;

import org.junit.jupiter.api.*;



import static org.mockito.Mockito.*;
import org.mockito.MockedStatic;
import static org.junit.jupiter.api.Assertions.*;

/* use factories and stuff */
public class BikeStoreTest
{
///...

    @Test
    void testBikeStore()
    {
        var bikeStore = new BikeStore(mock(FileWriter.class));
        var messageHandler = new MessageHandler();
        var bankObserver = new BankObserver(bikeStore);
    }
}

