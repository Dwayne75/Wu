package com.wurmonline.server.players;

import com.wurmonline.communication.SocketConnection;
import com.wurmonline.server.creatures.Communicator;

public class PlayerCommunicator
  extends Communicator
{
  public PlayerCommunicator(Player aPlayer, SocketConnection aConn)
  {
    super(aPlayer, aConn);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\players\PlayerCommunicator.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */