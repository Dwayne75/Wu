package com.wurmonline.server.webinterface;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WcDemotion
  extends WebCommand
  implements MiscConstants
{
  private static final Logger logger = Logger.getLogger(WcDemotion.class.getName());
  private long senderWurmId;
  private long targetWurmId;
  private String responseMsg;
  private short demoteType;
  public static final short CA = 1;
  public static final short CM = 2;
  public static final short GM = 3;
  
  public WcDemotion(long _id, long senderId, long targetId, short demotionType)
  {
    super(_id, (short)3);
    this.senderWurmId = senderId;
    this.targetWurmId = targetId;
    this.demoteType = demotionType;
    this.responseMsg = "";
  }
  
  public WcDemotion(long _id, long senderId, long targetId, short demotionType, String response)
  {
    super(_id, (short)3);
    this.senderWurmId = senderId;
    this.targetWurmId = targetId;
    this.demoteType = demotionType;
    this.responseMsg = response;
  }
  
  public WcDemotion(long _id, byte[] _data)
  {
    super(_id, (short)3, _data);
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
      dos.writeShort(this.demoteType);
      dos.writeLong(this.senderWurmId);
      dos.writeLong(this.targetWurmId);
      dos.writeUTF(this.responseMsg);
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
    new WcDemotion.1(this).start();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\webinterface\WcDemotion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */