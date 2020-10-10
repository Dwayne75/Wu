package com.wurmonline.server.webinterface;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WcExpelMember
  extends WebCommand
  implements MiscConstants
{
  private static final Logger logger = Logger.getLogger(WcRemoveFriendship.class.getName());
  private long playerId;
  private byte fromKingdomId;
  private byte toKingdomId;
  private int originServer;
  
  public WcExpelMember(long aPlayerId, byte aFromKingdomId, byte aToKingdomId, int aOriginServer)
  {
    super(WurmId.getNextWCCommandId(), (short)30);
    this.playerId = aPlayerId;
    this.fromKingdomId = aFromKingdomId;
    this.toKingdomId = aToKingdomId;
    this.originServer = aOriginServer;
  }
  
  public WcExpelMember(long aId, byte[] aData)
  {
    super(aId, (short)30, aData);
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
      dos.writeLong(this.playerId);
      dos.writeByte(this.fromKingdomId);
      dos.writeByte(this.toKingdomId);
      dos.writeInt(this.originServer);
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
    new Thread()
    {
      public void run()
      {
        DataInputStream dis = null;
        try
        {
          dis = new DataInputStream(new ByteArrayInputStream(WcExpelMember.this.getData()));
          WcExpelMember.this.playerId = dis.readLong();
          WcExpelMember.this.fromKingdomId = dis.readByte();
          WcExpelMember.this.toKingdomId = dis.readByte();
          WcExpelMember.this.originServer = dis.readInt();
        }
        catch (IOException ex)
        {
          WcExpelMember.logger.log(Level.WARNING, "Unpack exception " + ex.getMessage(), ex);
          return;
        }
        finally
        {
          StreamUtilities.closeInputStreamIgnoreExceptions(dis);
        }
        if (Servers.isThisLoginServer()) {
          WcExpelMember.this.sendFromLoginServer();
        }
        PlayerInfoFactory.expelMember(WcExpelMember.this.playerId, WcExpelMember.this.fromKingdomId, WcExpelMember.this.toKingdomId, WcExpelMember.this.originServer);
      }
    }.start();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\webinterface\WcExpelMember.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */