package com.wurmonline.server.webinterface;

import com.wurmonline.server.Message;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WcGVHelpMessage
  extends WebCommand
  implements MiscConstants
{
  private static final Logger logger = Logger.getLogger(WcGVHelpMessage.class.getName());
  private String name = "";
  private String msg = "";
  private boolean emote = false;
  private int colourR = -1;
  private int colourG = -1;
  private int colourB = -1;
  
  public WcGVHelpMessage(String playerName, String message, boolean aEmote, int red, int green, int blue)
  {
    super(WurmId.getNextWCCommandId(), (short)29);
    this.name = playerName;
    this.msg = message;
    this.emote = aEmote;
    this.colourR = red;
    this.colourG = green;
    this.colourB = blue;
  }
  
  WcGVHelpMessage(long aId, byte[] _data)
  {
    super(aId, (short)29, _data);
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
      dos.writeUTF(this.name);
      dos.writeUTF(this.msg);
      dos.writeBoolean(this.emote);
      dos.write((byte)this.colourR);
      dos.write((byte)this.colourG);
      dos.write((byte)this.colourB);
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
      this.name = dis.readUTF();
      this.msg = dis.readUTF();
      this.emote = dis.readBoolean();
      this.colourR = dis.read();
      this.colourG = dis.read();
      this.colourB = dis.read();
      if ((Servers.isThisLoginServer()) && (!Server.getInstance().isPS())) {
        for (ServerEntry se : Servers.getAllServers()) {
          if ((se.getId() != Servers.getLocalServerId()) && 
            (se.getId() != WurmId.getOrigin(getWurmId())))
          {
            WcGVHelpMessage wchgm = new WcGVHelpMessage(this.name, this.msg, this.emote, this.colourR, this.colourG, this.colourB);
            
            wchgm.sendToServer(se.getId());
          }
        }
      }
      if (Servers.isThisLoginServer())
      {
        Message mess;
        Message mess;
        if (this.emote) {
          mess = new Message(null, (byte)6, "CA HELP", this.msg, this.colourR, this.colourG, this.colourB);
        } else {
          mess = new Message(null, (byte)12, "CA HELP", "<" + this.name + "> " + this.msg, this.colourR, this.colourG, this.colourB);
        }
        Players.getInstance().sendPaMessage(mess);
      }
      else
      {
        Message mess;
        Message mess;
        if (this.emote) {
          mess = new Message(null, (byte)6, "GV HELP", this.msg, this.colourR, this.colourG, this.colourB);
        } else {
          mess = new Message(null, (byte)12, "GV HELP", "<" + this.name + "> " + this.msg, this.colourR, this.colourG, this.colourB);
        }
        Players.getInstance().sendGVMessage(mess);
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\webinterface\WcGVHelpMessage.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */