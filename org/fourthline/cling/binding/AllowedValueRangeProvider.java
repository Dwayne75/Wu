package org.fourthline.cling.binding;

public abstract interface AllowedValueRangeProvider
{
  public abstract long getMinimum();
  
  public abstract long getMaximum();
  
  public abstract long getStep();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\binding\AllowedValueRangeProvider.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */