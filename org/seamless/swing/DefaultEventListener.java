package org.seamless.swing;

public abstract interface DefaultEventListener<PAYLOAD>
  extends EventListener<DefaultEvent<PAYLOAD>>
{
  public abstract void handleEvent(DefaultEvent<PAYLOAD> paramDefaultEvent);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\swing\DefaultEventListener.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */