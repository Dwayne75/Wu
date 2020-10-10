package com.wurmonline.server.webinterface;

import com.wurmonline.server.Message;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.support.Trello;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
    new Thread()
    {
      public void run()
      {
        DataInputStream dis = null;
        try
        {
          dis = new DataInputStream(new ByteArrayInputStream(WcGlobalModeration.this.getData()));
          WcGlobalModeration.this.sender = dis.readUTF();
          WcGlobalModeration.this.ban = dis.readBoolean();
          WcGlobalModeration.this.mute = dis.readBoolean();
          WcGlobalModeration.this.unmute = dis.readBoolean();
          WcGlobalModeration.this.muteWarn = dis.readBoolean();
          WcGlobalModeration.this.warning = dis.readBoolean();
          WcGlobalModeration.this.playerName = dis.readUTF();
          WcGlobalModeration.this.reason = dis.readUTF();
          WcGlobalModeration.this.days = dis.readInt();
          WcGlobalModeration.this.hours = dis.readInt();
          WcGlobalModeration.this.senderPower = dis.readByte();
          try
          {
            Player p = Players.getInstance().getPlayer(WcGlobalModeration.this.playerName);
            if (WcGlobalModeration.this.ban) {
              if (p.getPower() < WcGlobalModeration.this.senderPower) {
                try
                {
                  Message mess = new Message(null, (byte)3, ":Event", "You have been banned for " + WcGlobalModeration.this.days + " days and thrown out from the game.");
                  
                  mess.setReceiver(p.getWurmId());
                  Server.getInstance().addMessage(mess);
                  p.ban(WcGlobalModeration.this.reason, System.currentTimeMillis() + WcGlobalModeration.this.days * 86400000L + WcGlobalModeration.this.hours * 3600000L);
                }
                catch (Exception ex)
                {
                  WcGlobalModeration.logger.log(Level.WARNING, ex.getMessage());
                }
              }
            }
            if (WcGlobalModeration.this.mute) {
              if (p.getPower() <= WcGlobalModeration.this.senderPower)
              {
                p.mute(true, WcGlobalModeration.this.reason, System.currentTimeMillis() + WcGlobalModeration.this.days * 86400000L + WcGlobalModeration.this.hours * 3600000L);
                
                Message mess = new Message(null, (byte)3, ":Event", "You have been muted by " + WcGlobalModeration.this.sender + " for " + WcGlobalModeration.this.hours + " hours and cannot shout anymore. Reason: " + WcGlobalModeration.this.reason);
                mess.setReceiver(p.getWurmId());
                Server.getInstance().addMessage(mess);
              }
            }
            if (WcGlobalModeration.this.unmute)
            {
              p.mute(false, "", 0L);
              
              Message mess = new Message(null, (byte)3, ":Event", "You have been given your voice back and can shout again.");
              
              mess.setReceiver(p.getWurmId());
              Server.getInstance().addMessage(mess);
            }
            if (WcGlobalModeration.this.muteWarn) {
              if (p.getPower() <= WcGlobalModeration.this.senderPower)
              {
                Message mess = new Message(null, (byte)3, ":Event", WcGlobalModeration.this.sender + " issues a warning that you may be muted. Be silent for a while and try to understand why or change the subject of your conversation please.");
                
                mess.setReceiver(p.getWurmId());
                Server.getInstance().addMessage(mess);
                if (WcGlobalModeration.this.reason.length() > 0)
                {
                  Message mess2 = new Message(null, (byte)3, ":Event", "The reason for this is '" + WcGlobalModeration.this.reason + "'");
                  
                  mess2.setReceiver(p.getWurmId());
                  Server.getInstance().addMessage(mess2);
                }
              }
            }
            if (WcGlobalModeration.this.warning) {
              if (p.getPower() < WcGlobalModeration.this.senderPower)
              {
                p.getSaveFile().warn();
                Message mess = new Message(null, (byte)3, ":Event", "You have just received an official warning. Too many of these will get you banned from the game.");
                
                mess.setReceiver(p.getWurmId());
                Server.getInstance().addMessage(mess);
              }
            }
          }
          catch (NoSuchPlayerException nsp)
          {
            if (WcGlobalModeration.this.unmute) {
              try
              {
                PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(WcGlobalModeration.this.playerName);
                pinf.load();
                if (pinf.wurmId > 0L) {
                  pinf.setMuted(false, "", 0L);
                }
              }
              catch (IOException ex)
              {
                if (Servers.isThisATestServer()) {
                  WcGlobalModeration.logger.log(Level.WARNING, "Unable to find player:" + WcGlobalModeration.this.playerName + "." + ex.getMessage(), ex);
                }
              }
            }
          }
          if (WcGlobalModeration.this.mute)
          {
            Players.addMgmtMessage(WcGlobalModeration.this.sender, "mutes " + WcGlobalModeration.this.playerName + " for " + WcGlobalModeration.this.hours + " hours. Reason: " + 
              WcGlobalModeration.this.reason);
            
            Message mess = new Message(null, (byte)9, "MGMT", "<" + WcGlobalModeration.this.sender + "> mutes " + WcGlobalModeration.this.playerName + " for " + WcGlobalModeration.this.hours + " hours. Reason: " + WcGlobalModeration.this.reason);
            
            Server.getInstance().addMessage(mess);
          }
          if (WcGlobalModeration.this.unmute)
          {
            Players.addMgmtMessage(WcGlobalModeration.this.sender, "unmutes " + WcGlobalModeration.this.playerName);
            
            Message mess = new Message(null, (byte)9, "MGMT", "<" + WcGlobalModeration.this.sender + "> unmutes " + WcGlobalModeration.this.playerName);
            
            Server.getInstance().addMessage(mess);
          }
          if (WcGlobalModeration.this.muteWarn)
          {
            Players.addMgmtMessage(WcGlobalModeration.this.sender, "mutewarns " + WcGlobalModeration.this.playerName + " (" + WcGlobalModeration.this.reason + ")");
            
            Message mess = new Message(null, (byte)9, "MGMT", "<" + WcGlobalModeration.this.sender + "> mutewarns " + WcGlobalModeration.this.playerName + " (" + WcGlobalModeration.this.reason + ")");
            
            Server.getInstance().addMessage(mess);
          }
          if (Servers.isThisLoginServer()) {
            if ((WcGlobalModeration.this.mute) || (WcGlobalModeration.this.muteWarn) || (WcGlobalModeration.this.unmute)) {
              Trello.addMessage(WcGlobalModeration.this.sender, WcGlobalModeration.this.playerName, WcGlobalModeration.this.reason, WcGlobalModeration.this.hours);
            }
          }
        }
        catch (IOException ex)
        {
          WcGlobalModeration.logger.log(Level.WARNING, "Unpack exception " + ex.getMessage(), ex);
        }
        finally
        {
          StreamUtilities.closeInputStreamIgnoreExceptions(dis);
        }
      }
    }.start();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\webinterface\WcGlobalModeration.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */