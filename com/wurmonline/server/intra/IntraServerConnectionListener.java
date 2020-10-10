package com.wurmonline.server.intra;

import java.nio.ByteBuffer;

public abstract interface IntraServerConnectionListener
{
  public abstract void reschedule(IntraClient paramIntraClient);
  
  public abstract void remove(IntraClient paramIntraClient);
  
  public abstract void commandExecuted(IntraClient paramIntraClient);
  
  public abstract void commandFailed(IntraClient paramIntraClient);
  
  public abstract void dataReceived(IntraClient paramIntraClient);
  
  public abstract void receivingData(ByteBuffer paramByteBuffer);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\intra\IntraServerConnectionListener.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */