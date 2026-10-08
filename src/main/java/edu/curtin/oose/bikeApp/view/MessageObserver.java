package edu.curtin.oose.bikeApp.view;

import edu.curtin.oose.bikeApp.model.Message;
public interface MessageObserver {
    void update(Message event);
}
