package org.gethydra.redux.backend.auth;

import org.gethydra.redux.backend.auth.methods.HydraMethod;
import org.gethydra.redux.backend.auth.methods.MojangMethod;

import java.util.ArrayList;

public class MethodManager
{
    private ArrayList<AuthenticationMethod> methods;

    public MethodManager()
    {
        this.methods = new ArrayList<>();

        registerMethod(new HydraMethod());
        registerMethod(new MojangMethod());
        //TODO: add microsoft method
    }

    public void registerMethod(AuthenticationMethod method)
    {
        this.methods.add(method);
    }

    public void unregisterMethod(String name)
    {
        methods.removeIf(method -> method.getFriendlyName().equals(name));
    }

    public ArrayList<AuthenticationMethod> getMethods()
    {
        return methods;
    }

    public AuthenticationMethod getMethod(String name)
    {
        for (AuthenticationMethod method : methods)
            if (method.getFriendlyName().equals(name))
                return method;
        return null;
    }
}
