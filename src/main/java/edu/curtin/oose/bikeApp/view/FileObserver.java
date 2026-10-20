package edu.curtin.oose.bikeApp.view;

import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

public class FileObserver {
    private static final Logger LOG = Logger.getLogger(FileObserver.class.getName());
    private final FileWriter fw;

    public FileObserver(FileWriter inFW)
    {
        fw = inFW;
    }
    public void update(String output)
    {
        try
        {
            fw.write(output);
            LOG.log(Level.FINE, "File written to");
            fw.flush();
        }
        catch(IOException e)
        {
            LOG.log(Level.SEVERE, "File IO FAILED");
            System.out.println("File could not be opened and/or written to");
        }
    }
}
