package javax.servlet;

import java.util.Set;

public abstract interface ServletContainerInitializer
{
  public abstract void onStartup(Set<Class<?>> paramSet, ServletContext paramServletContext)
    throws ServletException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\servlet\ServletContainerInitializer.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */