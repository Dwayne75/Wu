package com.wurmonline.server.webinterface;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WcGlobalPM
  extends WebCommand
  implements MiscConstants
{
  private static final Logger logger = Logger.getLogger(WcGlobalPM.class.getName());
  public static final byte GETID = 0;
  public static final byte THE_ID = 2;
  public static final byte SEND = 3;
  public static final byte IGNORED = 5;
  public static final byte NA = 6;
  public static final byte AFK = 7;
  private byte action = 3;
  private byte power = 0;
  private long senderId = -10L;
  private String senderName = "unknown";
  private byte kingdom = 0;
  private int targetServerId = 0;
  private long targetId = -10L;
  private String targetName = "unknown";
  private boolean friend = false;
  private String message = "";
  private boolean emote = false;
  private boolean override = false;
  
  public WcGlobalPM(long aId, byte _action, byte _power, long _senderId, String _senderName, byte _kingdom, int _targetServerId, long _targetId, String _targetName, boolean _friend, String _message, boolean _emote, boolean aOverride)
  {
    super(aId, (short)17);
    this.action = _action;
    this.power = _power;
    this.senderId = _senderId;
    this.senderName = _senderName;
    this.kingdom = _kingdom;
    this.targetServerId = _targetServerId;
    this.targetId = _targetId;
    this.targetName = _targetName;
    this.friend = _friend;
    this.message = _message;
    this.emote = _emote;
    this.override = aOverride;
  }
  
  public WcGlobalPM(long _id, byte[] _data)
  {
    super(_id, (short)17, _data);
  }
  
  public boolean autoForward()
  {
    return false;
  }
  
  public byte[] encode()
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    DataOutputStream dos = null;
    byte[] barr = null;
    try
    {
      dos = new DataOutputStream(bos);
      dos.writeByte(this.action);
      dos.writeByte(this.power);
      dos.writeLong(this.senderId);
      dos.writeUTF(this.senderName);
      dos.writeByte(this.kingdom);
      dos.writeInt(this.targetServerId);
      dos.writeLong(this.targetId);
      dos.writeUTF(this.targetName);
      dos.writeBoolean(this.friend);
      dos.writeUTF(this.message);
      dos.writeBoolean(this.emote);
      dos.writeBoolean(this.override);
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
    new WcGlobalPM.1(this).start();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\webinterface\WcGlobalPM.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */