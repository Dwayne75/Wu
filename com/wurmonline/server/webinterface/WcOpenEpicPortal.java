package com.wurmonline.server.webinterface;

import com.wurmonline.server.Players;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.questions.PortalQuestion;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WcOpenEpicPortal
  extends WebCommand
{
  private static final Logger logger = Logger.getLogger(WcOpenEpicPortal.class.getName());
  private boolean open = true;
  
  public WcOpenEpicPortal(long _id, boolean toggleOpen)
  {
    super(_id, (short)12);
    this.open = toggleOpen;
  }
  
  public WcOpenEpicPortal(long _id, byte[] _data)
  {
    super(_id, (short)12, _data);
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
      dos.writeBoolean(this.open);
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
      this.open = dis.readBoolean();
      PortalQuestion.epicPortalsEnabled = this.open;
      Player[] players = Players.getInstance().getPlayers();
      for (Player p : players) {
        SoundPlayer.playSound("sound.music.song.mountaintop", p, 2.0F);
      }
      if (Servers.localServer.LOGINSERVER)
      {
        WcOpenEpicPortal wccom = new WcOpenEpicPortal(WurmId.getNextWCCommandId(), PortalQuestion.epicPortalsEnabled);
        
        wccom.sendFromLoginServer();
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\webinterface\WcOpenEpicPortal.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */