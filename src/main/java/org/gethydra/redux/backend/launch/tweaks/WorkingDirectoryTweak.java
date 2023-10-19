package org.gethydra.redux.backend.launch.tweaks;

import net.bytebuddy.asm.Advice;

public class WorkingDirectoryTweak extends LaunchTweak
{
    @Override
    public void apply()
    {

    }

    public class WorkingDirectoryAdvice
    {
        @Advice.OnMethodExit
        void intercept()
        {
            //
        }
    }
}
