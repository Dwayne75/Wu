package com.wurmonline.server.webinterface;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WcKingdomChat
  extends WebCommand
  implements MiscConstants
{
  private static final Logger logger = Logger.getLogger(WcKingdomChat.class.getName());
  private String sender = "unknown";
  private long senderId = -10L;
  private String message = "";
  private byte kingdom = 0;
  private boolean emote = false;
  private int colorR = 0;
  private int colorG = 0;
  private int colorB = 0;
  
  public WcKingdomChat(long aId, long _senderId, String _sender, String _message, boolean _emote, byte _kingdom, int r, int g, int b)
  {
    super(aId, (short)13);
    this.sender = _sender;
    this.senderId = _senderId;
    this.message = _message;
    this.emote = _emote;
    this.kingdom = _kingdom;
    this.colorR = r;
    this.colorG = g;
    this.colorB = b;
  }
  
  public WcKingdomChat(long _id, byte[] _data)
  {
    super(_id, (short)13, _data);
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
      dos.writeUTF(this.sender);
      dos.writeLong(this.senderId);
      dos.writeUTF(this.message);
      dos.writeBoolean(this.emote);
      dos.writeByte(this.kingdom);
      dos.writeInt(this.colorR);
      dos.writeInt(this.colorG);
      dos.writeInt(this.colorB);
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
    new WcKingdomChat.1(this).start();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\webinterface\WcKingdomChat.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */