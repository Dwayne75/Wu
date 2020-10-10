package com.wurmonline.server;

import com.wurmonline.server.creatures.NoSuchCreatureException;

public abstract interface MovementListener
{
  public abstract void creatureMoved(long paramLong, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5)
    throws NoSuchCreatureException, NoSuchPlayerException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\MovementListener.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */