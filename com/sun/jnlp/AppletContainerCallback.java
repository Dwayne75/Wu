package com.sun.jnlp;

import java.awt.Dimension;
import java.net.URL;

public abstract interface AppletContainerCallback
{
  public abstract void showDocument(URL paramURL);
  
  public abstract void relativeResize(Dimension paramDimension);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\jnlp\AppletContainerCallback.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */