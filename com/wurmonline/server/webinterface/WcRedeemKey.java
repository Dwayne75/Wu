package com.wurmonline.server.webinterface;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.players.Player;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WcRedeemKey
  extends WebCommand
{
  private static final Logger logger = Logger.getLogger(WcRedeemKey.class.getName());
  
  public WcRedeemKey(long playerId, String coupon, byte reply)
  {
    super(WurmId.getNextWCCommandId(), (short)26);
  }
  
  public WcRedeemKey(long aId, byte[] aData)
  {
    super(aId, (short)26, aData);
  }
  
  public boolean autoForward()
  {
    return false;
  }
  
  byte[] encode()
  {
    byte[] barr = null;
    
    return barr;
  }
  
  public void execute() {}
  
  private static final Player getRedeemingPlayer(long wurmId)
  {
    try
    {
      return Players.getInstance().getPlayer(wurmId);
    }
    catch (NoSuchPlayerException e)
    {
      logger.log(Level.WARNING, e.getMessage(), e);
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\webinterface\WcRedeemKey.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */