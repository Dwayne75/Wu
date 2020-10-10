package com.wurmonline.server.webinterface;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WcKingdomInfo
  extends WebCommand
  implements MiscConstants
{
  private static final Logger logger = Logger.getLogger(WcKingdomInfo.class.getName());
  private boolean sendSingleKingdom = false;
  private byte singleKingdomId;
  
  public WcKingdomInfo(long aId, boolean singleKingdom, byte kingdomId)
  {
    super(aId, (short)7);
    this.sendSingleKingdom = singleKingdom;
    this.singleKingdomId = kingdomId;
  }
  
  public WcKingdomInfo(long aId, byte[] aData)
  {
    super(aId, (short)7, aData);
  }
  
  public boolean autoForward()
  {
    return true;
  }
  
  public byte[] encode()
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    DataOutputStream dos = null;
    byte[] barr = null;
    try
    {
      dos = new DataOutputStream(bos);
      dos.writeBoolean(this.sendSingleKingdom);
      if (this.sendSingleKingdom)
      {
        dos.writeInt(1);
        Kingdom k = Kingdoms.getKingdom(this.singleKingdomId);
        
        dos.writeByte(k.getId());
        dos.writeUTF(k.getName());
        dos.writeUTF(k.getPassword());
        dos.writeUTF(k.getChatName());
        dos.writeUTF(k.getSuffix());
        dos.writeUTF(k.getFirstMotto());
        dos.writeUTF(k.getSecondMotto());
        dos.writeByte(k.getTemplate());
        dos.writeBoolean(k.acceptsTransfers());
      }
      else
      {
        Kingdom[] kingdoms = Kingdoms.getAllKingdoms();
        dos.writeInt(kingdoms.length);
        for (Kingdom k : kingdoms)
        {
          dos.writeByte(k.getId());
          dos.writeUTF(k.getName());
          dos.writeUTF(k.getPassword());
          dos.writeUTF(k.getChatName());
          dos.writeUTF(k.getSuffix());
          dos.writeUTF(k.getFirstMotto());
          dos.writeUTF(k.getSecondMotto());
          dos.writeByte(k.getTemplate());
          dos.writeBoolean(k.acceptsTransfers());
        }
      }
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
    new WcKingdomInfo.1(this).start();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\webinterface\WcKingdomInfo.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */