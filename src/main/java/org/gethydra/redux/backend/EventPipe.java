package org.gethydra.redux.backend;

import org.gethydra.redux.event.Events;

public interface EventPipe
{
    void flush(Events event);
}
