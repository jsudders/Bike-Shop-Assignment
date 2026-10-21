package edu.curtin.oose.bikeApp.view;

import edu.curtin.oose.bikeApp.BikeStore;

public class ObserverFactory {
    private BikeStore store;
    private FileObserver fileOb;

    public ObserverFactory(BikeStore inStore, FileObserver fileOb)
    {
        this.store = inStore;
        this.fileOb = fileOb;
    }

    public MessageObserver createBank()
    {
        return new BankObserver(store);
    }

    public MessageObserver createStorage()
    {
        return new StorageObserver(store);
    }
    public MessageObserver createNotification()
    {
        return new NotificationObserver(store, fileOb);
    }
}
