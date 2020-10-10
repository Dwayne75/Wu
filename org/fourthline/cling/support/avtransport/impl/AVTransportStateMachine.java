package org.fourthline.cling.support.avtransport.impl;

import java.net.URI;
import org.fourthline.cling.support.avtransport.impl.state.AbstractState;
import org.fourthline.cling.support.model.SeekMode;
import org.seamless.statemachine.StateMachine;

public abstract interface AVTransportStateMachine
  extends StateMachine<AbstractState>
{
  public abstract void setTransportURI(URI paramURI, String paramString);
  
  public abstract void setNextTransportURI(URI paramURI, String paramString);
  
  public abstract void stop();
  
  public abstract void play(String paramString);
  
  public abstract void pause();
  
  public abstract void record();
  
  public abstract void seek(SeekMode paramSeekMode, String paramString);
  
  public abstract void next();
  
  public abstract void previous();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\avtransport\impl\AVTransportStateMachine.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */