package com.wurmonline.server.webinterface;

import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.Servers;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WcDeleteKingdom
  extends WebCommand
{
  private static final Logger logger = Logger.getLogger(WcDeleteKingdom.class.getName());
  private byte kingdomId;
  
  public WcDeleteKingdom(long aId, byte kingdomToDelete)
  {
    super(aId, (short)8);
    this.kingdomId = kingdomToDelete;
  }
  
  public WcDeleteKingdom(long aId, byte[] aData)
  {
    super(aId, (short)8, aData);
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
      dos.writeByte(this.kingdomId);
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
    DataInputStream dis = null;
    try
    {
      dis = new DataInputStream(new ByteArrayInputStream(getData()));
      this.kingdomId = dis.readByte();
      Servers.removeKingdomInfo(this.kingdomId);
      Kingdom k = Kingdoms.getKingdomOrNull(this.kingdomId);
      if ((k != null) && (k.isCustomKingdom()))
      {
        k.delete();
        Kingdoms.removeKingdom(this.kingdomId);
        HistoryManager.addHistory(k.getName(), "has faded and is no more.");
      }
    }
    catch (IOException ex)
    {
      logger.log(Level.WARNING, "Unpack exception " + ex.getMessage(), ex);
    }
    finally
    {
      StreamUtilities.closeInputStreamIgnoreExceptions(dis);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\webinterface\WcDeleteKingdom.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */