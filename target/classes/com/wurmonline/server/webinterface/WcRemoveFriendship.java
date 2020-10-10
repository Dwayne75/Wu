package com.wurmonline.server.webinterface;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.WurmId;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WcRemoveFriendship
  extends WebCommand
  implements MiscConstants
{
  private static final Logger logger = Logger.getLogger(WcRemoveFriendship.class.getName());
  private String playerName;
  private long playerWurmId;
  private String friendName;
  private long friendWurmId;
  
  public WcRemoveFriendship(String aPlayerName, long aPlayerWurmId, String aFriendName, long aFriendWurmId)
  {
    super(WurmId.getNextWCCommandId(), (short)4);
    this.playerName = aPlayerName;
    this.playerWurmId = aPlayerWurmId;
    this.friendName = aFriendName;
    this.friendWurmId = aFriendWurmId;
  }
  
  public WcRemoveFriendship(long aId, byte[] aData)
  {
    super(aId, (short)4, aData);
  }
  
  public boolean autoForward()
  {
    return true;
  }
  
  byte[] encode()
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    DataOutputStream dos = null;
    byte[] barr = null;
    try
    {
      dos = new DataOutputStream(bos);
      dos.writeUTF(this.playerName);
      dos.writeLong(this.playerWurmId);
      dos.writeUTF(this.friendName);
      dos.writeLong(this.friendWurmId);
      dos.flush();
      dos.close();
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, ex.getMessage(), ex);
    }
    finally
    {
      StreamUtilities.closeOutputStreamIgnoreExceptions(dos);
      barr = bos.toByteArray();
      StreamUtilities.closeOutputStreamIgnoreExceptions(bos);
      setData(barr);
    }
    return barr;
  }
  
  public void execute()
  {
    new WcRemoveFriendship.1(this).start();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\webinterface\WcRemoveFriendship.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */