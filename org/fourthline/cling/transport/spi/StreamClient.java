package org.fourthline.cling.transport.spi;

import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.StreamResponseMessage;

public abstract interface StreamClient<C extends StreamClientConfiguration>
{
  public abstract StreamResponseMessage sendRequest(StreamRequestMessage paramStreamRequestMessage)
    throws InterruptedException;
  
  public abstract void stop();
  
  public abstract C getConfiguration();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\spi\StreamClient.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */