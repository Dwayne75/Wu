package com.sun.xml.bind;

import javax.xml.bind.Marshaller;

public abstract interface CycleRecoverable
{
  public abstract Object onCycleDetected(Context paramContext);
  
  public static abstract interface Context
  {
    public abstract Marshaller getMarshaller();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\CycleRecoverable.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */