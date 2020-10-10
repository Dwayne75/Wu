package com.wurmonline.server.players;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.shared.constants.PlayerOnlineStatus;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerState
{
  private static final Logger logger = Logger.getLogger(PlayerState.class.getName());
  private int serverId;
  private long playerId;
  private String playerName;
  private long lastLogin;
  private long lastLogout;
  private PlayerOnlineStatus state;
  
  public PlayerState(long aWurmId)
  {
    PlayerInfo pInfo = PlayerInfoFactory.getPlayerInfoWithWurmId(aWurmId);
    this.playerId = aWurmId;
    if (pInfo != null) {
      try
      {
        pInfo.load();
      }
      catch (IOException localIOException) {}
    }
    if ((pInfo != null) && (pInfo.loaded))
    {
      this.serverId = pInfo.currentServer;
      this.lastLogin = pInfo.getLastLogin();
      this.lastLogout = pInfo.lastLogout;
      this.playerName = pInfo.getName();
      if (pInfo.currentServer != Servers.getLocalServerId())
      {
        this.state = PlayerOnlineStatus.OTHER_SERVER;
      }
      else
      {
        PlayerOnlineStatus onoff;
        try
        {
          Player p = Players.getInstance().getPlayer(pInfo.wurmId);
          onoff = PlayerOnlineStatus.ONLINE;
        }
        catch (NoSuchPlayerException e)
        {
          PlayerOnlineStatus onoff;
          onoff = PlayerOnlineStatus.OFFLINE;
        }
        this.state = onoff;
      }
    }
    else if (pInfo != null)
    {
      this.serverId = -1;
      this.lastLogin = 0L;
      this.lastLogout = 0L;
      this.playerName = "Error Loading";
      this.state = PlayerOnlineStatus.UNKNOWN;
    }
    else
    {
      this.serverId = -1;
      this.lastLogin = 0L;
      this.lastLogout = 0L;
      this.playerName = "Deleted";
      this.state = PlayerOnlineStatus.DELETE_ME;
    }
  }
  
  public PlayerState(Player player, long aWhenStateChanged, PlayerOnlineStatus aState)
  {
    this(Servers.getLocalServerId(), player.getWurmId(), player.getName(), aWhenStateChanged, aState);
  }
  
  public PlayerState(long aPlayerId, String aPlayerName, long aWhenStateChanged, PlayerOnlineStatus aState)
  {
    this(Servers.getLocalServerId(), aPlayerId, aPlayerName, aWhenStateChanged, aState);
  }
  
  public PlayerState(int aServerId, long aPlayerId, String aPlayerName, long aWhenStateChanged, PlayerOnlineStatus aState)
  {
    this.serverId = aServerId;
    this.playerId = aPlayerId;
    this.playerName = aPlayerName;
    this.state = aState;
    if (aState == PlayerOnlineStatus.ONLINE)
    {
      this.lastLogin = aWhenStateChanged;
      this.lastLogout = 0L;
    }
    else
    {
      this.lastLogin = 0L;
      this.lastLogout = aWhenStateChanged;
    }
  }
  
  public PlayerState(long aPlayerId, String aPlayerName, long aLastLogin, long aLastLogout, PlayerOnlineStatus aState)
  {
    this(Servers.getLocalServerId(), aPlayerId, aPlayerName, aLastLogin, aLastLogout, aState);
  }
  
  public PlayerState(int aServerId, long aPlayerId, String aPlayerName, long aLastLogin, long aLastLogout, PlayerOnlineStatus aState)
  {
    this.serverId = aServerId;
    this.playerId = aPlayerId;
    this.playerName = aPlayerName;
    this.lastLogin = aLastLogin;
    this.lastLogout = aLastLogout;
    this.state = aState;
  }
  
  public PlayerState(byte[] aData)
  {
    decode(aData);
  }
  
  public int getServerId()
  {
    return this.serverId;
  }
  
  public String getServerName()
  {
    ServerEntry server = Servers.getServerWithId(this.serverId);
    if (server == null) {
      return "Unknown";
    }
    return server.getName();
  }
  
  public long getPlayerId()
  {
    return this.playerId;
  }
  
  public String getPlayerName()
  {
    return this.playerName;
  }
  
  public long getLastLogin()
  {
    return this.lastLogin;
  }
  
  public long getLastLogout()
  {
    return this.lastLogout;
  }
  
  public long getWhenStateChanged()
  {
    return Math.max(this.lastLogin, this.lastLogout);
  }
  
  public PlayerOnlineStatus getState()
  {
    return this.state;
  }
  
  public void setState(PlayerOnlineStatus aState)
  {
    this.state = aState;
  }
  
  public void setStatus(int aServerId, PlayerOnlineStatus aState, long aWhenStateChanged)
  {
    this.serverId = aServerId;
    this.state = aState;
    if (aState == PlayerOnlineStatus.ONLINE) {
      this.lastLogin = aWhenStateChanged;
    } else {
      this.lastLogout = aWhenStateChanged;
    }
  }
  
  final void decode(byte[] aData)
  {
    DataInputStream dis = null;
    try
    {
      dis = new DataInputStream(new ByteArrayInputStream(aData));
      this.serverId = dis.readInt();
      this.playerId = dis.readLong();
      this.playerName = dis.readUTF();
      this.lastLogin = dis.readLong();
      this.lastLogout = dis.readLong();
      this.state = PlayerOnlineStatus.playerOnlineStatusFromId(dis.readByte());
    }
    catch (IOException ex)
    {
      logger.log(Level.WARNING, "Unpack exception " + ex.getMessage(), ex);
      return;
    }
    finally
    {
      StreamUtilities.closeInputStreamIgnoreExceptions(dis);
    }
  }
  
  public final byte[] encode()
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    DataOutputStream dos = null;
    byte[] barr = null;
    try
    {
      dos = new DataOutputStream(bos);
      dos.writeInt(this.serverId);
      dos.writeLong(this.playerId);
      dos.writeUTF(this.playerName);
      dos.writeLong(this.lastLogin);
      dos.writeLong(this.lastLogout);
      dos.writeByte(this.state.getId());
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
    }
    return barr;
  }
  
  public String toString()
  {
    return 
      "PlayerState [ServerId=" + this.serverId + ", playerId=" + this.playerId + ", playerName=" + this.playerName + ", whenStateChanged=" + getWhenStateChanged() + ", state=" + this.state + "]";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\players\PlayerState.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */