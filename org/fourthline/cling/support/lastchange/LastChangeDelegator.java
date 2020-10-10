package org.fourthline.cling.support.lastchange;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;

public abstract interface LastChangeDelegator
{
  public abstract LastChange getLastChange();
  
  public abstract void appendCurrentState(LastChange paramLastChange, UnsignedIntegerFourBytes paramUnsignedIntegerFourBytes)
    throws Exception;
  
  public abstract UnsignedIntegerFourBytes[] getCurrentInstanceIds();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\lastchange\LastChangeDelegator.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */