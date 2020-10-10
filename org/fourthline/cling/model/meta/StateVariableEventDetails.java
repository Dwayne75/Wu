package org.fourthline.cling.model.meta;

public class StateVariableEventDetails
{
  private final boolean sendEvents;
  private final int eventMaximumRateMilliseconds;
  private final int eventMinimumDelta;
  
  public StateVariableEventDetails()
  {
    this(true, 0, 0);
  }
  
  public StateVariableEventDetails(boolean sendEvents)
  {
    this(sendEvents, 0, 0);
  }
  
  public StateVariableEventDetails(boolean sendEvents, int eventMaximumRateMilliseconds, int eventMinimumDelta)
  {
    this.sendEvents = sendEvents;
    this.eventMaximumRateMilliseconds = eventMaximumRateMilliseconds;
    this.eventMinimumDelta = eventMinimumDelta;
  }
  
  public boolean isSendEvents()
  {
    return this.sendEvents;
  }
  
  public int getEventMaximumRateMilliseconds()
  {
    return this.eventMaximumRateMilliseconds;
  }
  
  public int getEventMinimumDelta()
  {
    return this.eventMinimumDelta;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\meta\StateVariableEventDetails.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */