package com.wurmonline.server.webinterface;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Players;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WcMgmtMessage
  extends WebCommand
  implements MiscConstants
{
  private static final Logger logger = Logger.getLogger(WcMgmtMessage.class.getName());
  private String sender = "unknown";
  private String message = "";
  private boolean emote = false;
  private boolean logit = false;
  private int colourR = -1;
  private int colourG = -1;
  private int colourB = -1;
  
  WcMgmtMessage(long aId, byte[] _data)
  {
    super(aId, (short)24, _data);
  }
  
  public WcMgmtMessage(long aId, String _sender, String _message, boolean _emote, boolean logIt, int red, int green, int blue)
  {
    super(aId, (short)24);
    this.sender = _sender;
    this.message = _message;
    this.emote = _emote;
    this.logit = logIt;
    this.colourR = red;
    this.colourG = green;
    this.colourB = blue;
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
      dos.writeUTF(this.message);
      dos.writeBoolean(this.emote);
      dos.writeBoolean(this.logit);
      dos.writeInt(this.colourR);
      dos.writeInt(this.colourG);
      dos.writeInt(this.colourB);
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
      this.sender = dis.readUTF();
      this.message = dis.readUTF();
      this.emote = dis.readBoolean();
      this.logit = dis.readBoolean();
      this.colourR = dis.readInt();
      this.colourG = dis.readInt();
      this.colourB = dis.readInt();
      
      Players.getInstance().sendMgmtMessage(null, this.sender, this.message, this.emote, this.logit, this.colourR, this.colourG, this.colourB);
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\webinterface\WcMgmtMessage.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */