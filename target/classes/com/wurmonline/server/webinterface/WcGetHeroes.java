package com.wurmonline.server.webinterface;

import com.wurmonline.server.Players;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WcGetHeroes
  extends WebCommand
{
  private static final Logger logger = Logger.getLogger(WcSetPower.class.getName());
  private long sender;
  private byte powerToCheck;
  private String response;
  
  public WcGetHeroes()
  {
    super(WurmId.getNextWCCommandId(), (short)34);
  }
  
  public WcGetHeroes(WcGetHeroes copy)
  {
    this();
    this.sender = copy.sender;
    this.powerToCheck = copy.powerToCheck;
    this.response = copy.response;
  }
  
  public WcGetHeroes(long _id, byte[] _data)
  {
    super(_id, (short)34, _data);
  }
  
  public WcGetHeroes(long sender, byte powerToCheck)
  {
    this();
    this.sender = sender;
    this.powerToCheck = powerToCheck;
    this.response = "";
  }
  
  byte[] encode()
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    DataOutputStream dos = null;
    byte[] byteArr = null;
    try
    {
      dos = new DataOutputStream(bos);
      dos.writeLong(this.sender);
      dos.writeByte(this.powerToCheck);
      dos.writeUTF(this.response);
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
    new WcGetHeroes.1(this).start();
  }
  
  private static String getPowerName(byte power)
  {
    String powerName = "heroes";
    if (power == 2) {
      powerName = "demigods";
    } else if (power == 3) {
      powerName = "high gods";
    } else if (power == 4) {
      powerName = "archangels";
    } else if (power == 5) {
      powerName = "implementors";
    }
    return powerName;
  }
  
  public static String getHeroes(byte powerToCheck)
  {
    String[] result = Players.getInstance().getHeros(powerToCheck);
    if (result.length == 0) {
      return Servers.localServer.getName() + " reports no " + getPowerName(powerToCheck);
    }
    StringBuilder sb = new StringBuilder(Servers.localServer.getName() + " reports the following " + getPowerName(powerToCheck) + ": ");
    for (int i = 0; i < result.length - 1; i++)
    {
      sb.append(result[i]);
      sb.append(", ");
    }
    sb.append(result[(result.length - 1)]);
    
    return sb.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\webinterface\WcGetHeroes.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */