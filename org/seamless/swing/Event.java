package org.seamless.swing;

public abstract interface Event<PAYLOAD>
{
  public abstract PAYLOAD getPayload();
  
  public abstract void addFiredInController(Controller paramController);
  
  public abstract boolean alreadyFired(Controller paramController);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\swing\Event.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */