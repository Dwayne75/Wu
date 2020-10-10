package com.wurmonline.communication;

import java.nio.ByteBuffer;

public abstract interface SimpleConnectionListener
{
  public abstract void reallyHandle(int paramInt, ByteBuffer paramByteBuffer);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\communication\SimpleConnectionListener.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */