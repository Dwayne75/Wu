package com.wurmonline.server.webinterface;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WcGlobalModeration
  extends WebCommand
  implements MiscConstants, TimeConstants
{
  private static final Logger logger = Logger.getLogger(WcGlobalModeration.class.getName());
  private boolean warning;
  private boolean ban;
  private boolean mute;
  private boolean unmute;
  private boolean muteWarn;
  private int hours;
  private int days;
  private String sender = "";
  private String reason = "";
  private String playerName = "";
  private byte senderPower = 0;
  
  public WcGlobalModeration(long id, String _sender, byte _senderPower, boolean _mute, boolean _unmute, boolean _mutewarn, boolean _ban, boolean _warning, int _hours, int _days, String _playerName, String _reason)
  {
    super(id, (short)14);
    this.sender = _sender;
    this.warning = _warning;
    this.ban = _ban;
    this.mute = _mute;
    this.unmute = _unmute;
    this.muteWarn = _mutewarn;
    this.hours = _hours;
    this.days = _days;
    this.reason = _reason;
    this.playerName = _playerName;
    this.senderPower = _senderPower;
  }
  
  public WcGlobalModeration(long id, byte[] data)
  {
    super(id, (short)14, data);
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
      dos.writeUTF(this.sender);
      dos.writeBoolean(this.ban);
      dos.writeBoolean(this.mute);
      dos.writeBoolean(this.unmute);
      dos.writeBoolean(this.muteWarn);
      dos.writeBoolean(this.warning);
      
      dos.writeUTF(this.playerName);
      dos.writeUTF(this.reason);
      dos.writeInt(this.days);
      dos.writeInt(this.hours);
      dos.writeByte(this.senderPower);
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
    new WcGlobalModeration.1(this).start();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\webinterface\WcGlobalModeration.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */