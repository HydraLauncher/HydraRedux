package org.gethydra.redux.backend;

import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.OutputStream;

public class GameOutputStream extends OutputStream
{
    private final TextArea outputField;
    private final StringBuilder stringBuilder;

    public GameOutputStream(TextArea outputField)
    {
        this.outputField = outputField;
        stringBuilder = new StringBuilder();
    }

    public final void write(int i) throws IOException
    {
        char c = (char) i;
        if (c == '\r' || c == '\n')
        {
            outputField.appendText(stringBuilder.toString());
        } else
            stringBuilder.append(c);
    }
}
