package org.gethydra.redux.event;

import java.util.ArrayList;

public class EventBus<T>
{
    private ArrayList<T> subscribers = new ArrayList<>();

    public void subscribe(T handler)
    {
        subscribers.add(handler);
    }

    public ArrayList<T> getSubscribers()
    {
        return subscribers;
    }
}
