package org.seamless.swing;

import java.util.HashSet;
import java.util.Set;

public class DefaultEvent<PAYLOAD>
  implements Event
{
  PAYLOAD payload;
  Set<Controller> firedInControllers = new HashSet();
  
  public DefaultEvent() {}
  
  public DefaultEvent(PAYLOAD payload)
  {
    this.payload = payload;
  }
  
  public PAYLOAD getPayload()
  {
    return (PAYLOAD)this.payload;
  }
  
  public void setPayload(PAYLOAD payload)
  {
    this.payload = payload;
  }
  
  public void addFiredInController(Controller seenController)
  {
    this.firedInControllers.add(seenController);
  }
  
  public boolean alreadyFired(Controller controller)
  {
    return this.firedInControllers.contains(controller);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\swing\DefaultEvent.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */