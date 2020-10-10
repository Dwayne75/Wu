package com.wurmonline.server.webinterface;

import com.wurmonline.server.WurmId;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WcSetPower
  extends WebCommand
{
  private static final Logger logger = Logger.getLogger(WcSetPower.class.getName());
  private String playerName;
  private int newPower;
  private String senderName;
  private int senderPower;
  private String response;
  
  public WcSetPower(String playerName, int newPower, String senderName, int senderPower, String response)
  {
    this();
    this.playerName = playerName;
    this.newPower = newPower;
    this.senderName = senderName;
    this.senderPower = senderPower;
    this.response = response;
  }
  
  WcSetPower(WcSetPower copy)
  {
    this();
    this.playerName = copy.playerName;
    this.newPower = copy.newPower;
    this.senderName = copy.senderName;
    this.senderPower = copy.senderPower;
    this.response = copy.response;
  }
  
  WcSetPower()
  {
    super(WurmId.getNextWCCommandId(), (short)33);
  }
  
  public WcSetPower(long aId, byte[] _data)
  {
    super(aId, (short)33, _data);
  }
  
  byte[] encode()
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    DataOutputStream dos = null;
    byte[] byteArr = null;
    try
    {
      dos = new DataOutputStream(bos);
      dos.writeUTF(this.playerName);
      dos.writeInt(this.newPower);
      dos.writeUTF(this.senderName);
      dos.writeInt(this.senderPower);
      dos.writeUTF(this.response);
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
      byteArr = bos.toByteArray();
      StreamUtilities.closeOutputStreamIgnoreExceptions(bos);
      setData(byteArr);
    }
    return byteArr;
  }
  
  public boolean autoForward()
  {
    return false;
  }
  
  public void execute()
  {
    new WcSetPower.1(this).start();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\webinterface\WcSetPower.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */