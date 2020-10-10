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

public class WcTradeChannel
  extends WebCommand
  implements MiscConstants
{
  private static final Logger logger = Logger.getLogger(WcTradeChannel.class.getName());
  private String sender = "unknown";
  private long senderId = -10L;
  private String message = "";
  private byte kingdom = 0;
  private int colorR = 0;
  private int colorG = 0;
  private int colorB = 0;
  
  public WcTradeChannel(long aId, long _senderId, String _sender, String _message, byte _kingdom, int r, int g, int b)
  {
    super(aId, (short)28);
    this.sender = _sender;
    this.senderId = _senderId;
    this.message = _message;
    this.kingdom = _kingdom;
    this.colorR = r;
    this.colorG = g;
    this.colorB = b;
  }
  
  public WcTradeChannel(long _id, byte[] _data)
  {
    super(_id, (short)28, _data);
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
    new Thread()
    {
      public void run()
      {
        DataInputStream dis = null;
        try
        {
          dis = new DataInputStream(new ByteArrayInputStream(WcTradeChannel.this.getData()));
          WcTradeChannel.this.sender = dis.readUTF();
          WcTradeChannel.this.senderId = dis.readLong();
          WcTradeChannel.this.message = dis.readUTF();
          WcTradeChannel.this.kingdom = dis.readByte();
          WcTradeChannel.this.colorR = dis.readInt();
          WcTradeChannel.this.colorG = dis.readInt();
          WcTradeChannel.this.colorB = dis.readInt();
          Players.getInstance().sendGlobalTradeMessage(null, WcTradeChannel.this.senderId, WcTradeChannel.this.sender, WcTradeChannel.this.message, 
            WcTradeChannel.this.kingdom, WcTradeChannel.this.colorR, WcTradeChannel.this.colorG, WcTradeChannel.this.colorB);
        }
        catch (IOException ex)
        {
          WcTradeChannel.logger.log(Level.WARNING, "Unpack exception " + ex.getMessage(), ex);
        }
        finally
        {
          StreamUtilities.closeInputStreamIgnoreExceptions(dis);
        }
      }
    }.start();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\webinterface\WcTradeChannel.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */