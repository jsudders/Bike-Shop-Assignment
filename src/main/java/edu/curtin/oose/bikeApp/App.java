package edu.curtin.oose.bikeApp;

import edu.curtin.oose.bikeApp.view.BankObserver;
import edu.curtin.oose.bikeApp.view.NotificationObserver;
import edu.curtin.oose.bikeApp.view.StorageObserver;

/**
 * Entry point into the application. To change the package, and/or the name of this class, make
 * sure to update the 'mainClass = ...' line in build.gradle.kts.
 */
public class App
{
    public static void main(String[] args)
    {
        try
        {
            Scenario scObject = new Scenario();
            BikeStore store = new BikeStore();
            BankObserver bank = new BankObserver(store);
            NotificationObserver notification = new NotificationObserver(store);
            StorageObserver storage = new StorageObserver(store);

            MessageHandler handler = new MessageHandler();
            handler.addObserver(bank);
            handler.addObserver(notification);
            handler.addObserver(storage);

            while(System.in.available() == 0)  // <-- actual Java code
            {
                String message = scObject.nextMessage();
                while(message != null)
                {
                   handler.handleMessage(store, message);
                   message = scObject.nextMessage();
                }
            // Pseudocode
            // show status;
            // check if anything needs to be done _at the present time_;
            // do them all (if any);

            // Sleep for 1 second -- actual Java code
                try
                {
                    Thread.sleep(1000);  // milliseconds
                    store.elapseDay();
                }
                catch(InterruptedException e)
                {
                    throw new AssertionError(e);
                }

            // Loop until the user presses Enter;
            // Perform one simulation “step” on each loop iteration;
            // Wait 1 second between steps.
            }
            store.printFinalStats();
        }
        catch(Exception e)
        {
            System.out.println("ERROR " + e.getMessage());
        }
       

        /*The method “System.in.available()” returns the number of bytes in the console input buffer. This is 0 until there’s any console input, and the user must press Enter to send any console input to the program. Thus, this event loop runs until the user presses Enter.

We can’t just write scanner.nextLine() (for instance), because this waits for the input while doing nothing, and we want to actually run the simulation in the meantime.

Apart from this, we don’t need any console user input. Your application will take input from another source, as explained below. */
    }
}
