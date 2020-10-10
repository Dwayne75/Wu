package com.wurmonline.server;

import com.wurmonline.communication.SimpleConnectionListener;
import com.wurmonline.communication.SocketConnection;
import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.behaviours.Seat;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.behaviours.VehicleBehaviour;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.bodys.BodyTemplate;
import com.wurmonline.server.bodys.Wounds;
import com.wurmonline.server.creatures.CombatHandler;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreaturePos;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.MountAction;
import com.wurmonline.server.creatures.MovementScheme;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.PlayerMove;
import com.wurmonline.server.creatures.SpellEffects;
import com.wurmonline.server.creatures.SpellEffectsEnum;
import com.wurmonline.server.creatures.VisionArea;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.epic.ValreiMapData;
import com.wurmonline.server.intra.MountTransfer;
import com.wurmonline.server.intra.PlayerTransfer;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.WurmColor;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Abilities;
import com.wurmonline.server.players.Achievements;
import com.wurmonline.server.players.Ban;
import com.wurmonline.server.players.Cultist;
import com.wurmonline.server.players.HackerIp;
import com.wurmonline.server.players.ItemBonus;
import com.wurmonline.server.players.KingdomIp;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerCommunicator;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.players.Spawnpoint;
import com.wurmonline.server.players.Titles.Title;
import com.wurmonline.server.questions.SelectSpawnQuestion;
import com.wurmonline.server.skills.AffinitiesTimed;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.steam.SteamHandler;
import com.wurmonline.server.steam.SteamId;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.Door;
import com.wurmonline.server.structures.FenceGate;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.tutorial.MissionPerformed;
import com.wurmonline.server.tutorial.MissionPerformer;
import com.wurmonline.server.tutorial.PlayerTutorial;
import com.wurmonline.server.utils.ProtocolUtilities;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.webinterface.WCGmMessage;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.constants.PlayerOnlineStatus;
import com.wurmonline.shared.constants.ProtoConstants;
import com.wurmonline.shared.exceptions.WurmServerException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import sun.misc.BASE64Encoder;

public final class LoginHandler
  implements SimpleConnectionListener, TimeConstants, MiscConstants, CreatureTemplateIds, ProtoConstants, CounterTypes
{
  private final SocketConnection conn;
  private static Logger logger = Logger.getLogger(LoginHandler.class.getName());
  public static final String legalChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final int DISCONNECT_TICKS = 400;
  private int loadedItems = 0;
  private boolean redirected = false;
  private static final byte clientDevLevel = 2;
  static int redirects = 0;
  static int logins = 0;
  private static final String BROKEN_PLAYER_MODEL = "model.player.broken";
  private static final String MESSAGE_FORMAT_UTF_8 = "UTF-8";
  private static final String PROBLEM_SENDING_LOGIN_DENIED_MESSAGE = ", problem sending login denied message: ";
  public static final Map<String, HackerIp> failedIps = new HashMap();
  private static final int MAX_REAL_DEATHS = 4;
  private static final int MAX_NAME_LENGTH = 40;
  private static final int MIN_NAME_LENGTH = 3;
  private static final int ITERATIONS = 1000;
  private static final int KEY_LENGTH = 192;
  
  public LoginHandler(SocketConnection aConn)
  {
    this.conn = aConn;
    if (logger.isLoggable(Level.FINER)) {
      logger.finer("Creating LoginHandler for SocketConnection " + aConn);
    }
  }
  
  public static final boolean containsIllegalCharacters(String name)
  {
    char[] chars = name.toCharArray();
    for (char lC : chars) {
      if ("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(lC) < 0) {
        return true;
      }
    }
    return false;
  }
  
  public static final String raiseFirstLetter(String oldString)
  {
    if (oldString.length() == 0) {
      return oldString;
    }
    String lOldString = oldString.toLowerCase();
    String firstLetter = lOldString.substring(0, 1).toUpperCase();
    String newString = firstLetter + lOldString.substring(1, lOldString.length());
    return newString;
  }
  
  public void reallyHandle(int num, ByteBuffer byteBuffer)
  {
    short cmd = (short)byteBuffer.get();
    if (logger.isLoggable(Level.FINEST)) {
      logger.finest("Handling block with Command: " + cmd + ", " + ProtocolUtilities.getDescriptionForCommand((byte)cmd));
    }
    if ((cmd == -15) || (cmd == 23))
    {
      int protocolVersion = byteBuffer.getInt();
      if (protocolVersion != 250990584)
      {
        String message = "Incompatible communication protocol.\nPlease update the client at http://www.wurmonline.com or wait for the server to be updated.";
        logger.log(Level.INFO, "Rejected protocol " + protocolVersion + ". Mine=" + 250990584 + ", (" + "0xEF5CFF8s" + ") " + this.conn);
        try
        {
          sendLoginAnswer(false, "Incompatible communication protocol.\nPlease update the client at http://www.wurmonline.com or wait for the server to be updated.", 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, 0);
        }
        catch (IOException ioe)
        {
          if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, this.conn.getIp() + ", problem sending login denied message: " + "Incompatible communication protocol.\nPlease update the client at http://www.wurmonline.com or wait for the server to be updated.", ioe);
          }
        }
        return;
      }
      String name = getNextString(byteBuffer, "name", true);
      name = raiseFirstLetter(name);
      
      String password = getNextString(byteBuffer, "password for " + name, false);
      
      String serverPassword = getNextString(byteBuffer, "server password for " + name, false);
      
      String steamIDAsString = getNextString(byteBuffer, "steamid for " + name, false);
      
      boolean sendExtraBytes = false;
      if (byteBuffer.hasRemaining()) {
        sendExtraBytes = byteBuffer.get() != 0;
      }
      if (cmd == 23) {
        reconnect(name, password, false, serverPassword, steamIDAsString);
      } else {
        login(name, password, sendExtraBytes, false, serverPassword, steamIDAsString);
      }
    }
    else if (cmd == -52)
    {
      try
      {
        String steamIDAsString = getNextString(byteBuffer, "steamid", false);
        
        long authTicket = byteBuffer.getLong();
        int arrayLenght = byteBuffer.getInt();
        
        byte[] ticketArray = new byte[arrayLenght];
        for (int i = 0; i < ticketArray.length; i++) {
          ticketArray[i] = byteBuffer.get();
        }
        long tokenLen = byteBuffer.getLong();
        if (Server.getInstance().steamHandler.getIsOfflineServer())
        {
          Server.getInstance().steamHandler.setIsPlayerAuthenticated(steamIDAsString);
          sendAuthenticationAnswer(true, "");
          return;
        }
        boolean wasAddedBeforeWithSameIp = Server.getInstance().steamHandler.addLoginHandler(steamIDAsString, this);
        
        int authenticationResult = Server.getInstance().steamHandler.BeginAuthSession(steamIDAsString, ticketArray, tokenLen);
        if (authenticationResult != 0) {
          if (authenticationResult == 2)
          {
            if (!wasAddedBeforeWithSameIp)
            {
              logger.log(Level.INFO, "Duplicate authentication");
              sendAuthenticationAnswer(false, "Duplicate authentication");
            }
            else
            {
              Server.getInstance().steamHandler.setIsPlayerAuthenticated(steamIDAsString);
              sendAuthenticationAnswer(true, "");
            }
          }
          else
          {
            logger.log(Level.INFO, "Steam could not authenticate the user");
            sendAuthenticationAnswer(false, "Steam could not authenticate the user");
          }
        }
      }
      catch (Throwable t)
      {
        logger.log(Level.SEVERE, "Error while authenticating the user with steam.");
        sendAuthenticationAnswer(false, "Error while authenticating the user with steam.");
      }
    }
  }
  
  private String getNextString(ByteBuffer byteBuffer, String name, boolean logValue)
  {
    byte[] bytes = new byte[byteBuffer.get() & 0xFF];
    byteBuffer.get(bytes);
    String decoded;
    try
    {
      decoded = new String(bytes, "UTF-8");
    }
    catch (UnsupportedEncodingException nse)
    {
      String decoded;
      decoded = new String(bytes);
      String logMessage = "Unsupported encoding for " + (logValue ? name + ": " + decoded : name);
      logger.log(Level.WARNING, logMessage, nse);
    }
    return decoded;
  }
  
  private void login(String name, String password, boolean sendExtraBytes, boolean isUndead, String serverPassword, String steamIDAsString)
  {
    try
    {
      password = hashPassword(password, encrypt(raiseFirstLetter(name)));
    }
    catch (Exception ex)
    {
      logger.log(Level.SEVERE, name + " Failed to encrypt password", ex);
      String message = "We failed to encrypt your password. Please try another.";
      try
      {
        Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
        sendLoginAnswer(false, "We failed to encrypt your password. Please try another.", 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, 0);
      }
      catch (IOException ioe)
      {
        if (logger.isLoggable(Level.FINE)) {
          logger.log(Level.FINE, this.conn.getIp() + ", problem sending login denied message: " + "We failed to encrypt your password. Please try another.", ioe);
        }
      }
      return;
    }
    try
    {
      if ((Servers.localServer.LOGINSERVER) || (Constants.maintaining) || (isUndead) || (Servers.localServer.testServer))
      {
        handleLogin(name, password, sendExtraBytes, false, false, isUndead, serverPassword, steamIDAsString);
      }
      else
      {
        logger.log(Level.WARNING, name + " logging in directly! Rejected.");
        String message = "You need to connect to the login server.";
        try
        {
          Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
          sendLoginAnswer(false, "You need to connect to the login server.", 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, 0);
        }
        catch (IOException ioe)
        {
          if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, this.conn.getIp() + ", problem sending login denied message: " + "You need to connect to the login server.", ioe);
          }
        }
        return;
      }
    }
    catch (Exception ex)
    {
      logger.log(Level.SEVERE, "Failed to log " + name + " due to an Exception: " + ex.getMessage(), ex);
      String message = "We failed to log you in. " + ex.getMessage();
      try
      {
        Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
        sendLoginAnswer(false, message, 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, 0);
      }
      catch (IOException ioe)
      {
        if (logger.isLoggable(Level.FINE)) {
          logger.log(Level.FINE, this.conn.getIp() + ", problem sending login denied message: " + message, ioe);
        }
      }
      return;
    }
  }
  
  private void reconnect(String name, String sessionkey, boolean isUndead, String serverPassword, String steamIDAsString)
  {
    this.redirected = true;
    try
    {
      handleLogin(name, sessionkey, false, false, true, isUndead, serverPassword, steamIDAsString);
    }
    catch (Exception ex)
    {
      logger.log(Level.SEVERE, name + " " + ex.getMessage(), ex);
      String message = "We failed to log you in. " + ex.getMessage();
      try
      {
        Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
        sendLoginAnswer(false, message, 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, 0);
      }
      catch (IOException ioe)
      {
        if (logger.isLoggable(Level.FINE)) {
          logger.log(Level.FINE, this.conn.getIp() + ", " + name + ", problem sending login denied message: " + message, ioe);
        }
      }
      return;
    }
  }
  
  private boolean preValidateLogin(String name, String steamIDAsString)
  {
    if (Server.getInstance().isLagging())
    {
      logger.log(Level.INFO, "Refusing connection due to lagging server for " + name);
      String message = "The server is lagging. Retrying in 20 seconds.";
      try
      {
        Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
        sendLoginAnswer(false, "The server is lagging. Retrying in 20 seconds.", 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, 20);
      }
      catch (IOException ioe)
      {
        if (logger.isLoggable(Level.FINE)) {
          logger.log(Level.FINE, this.conn.getIp() + ", problem sending login denied message: " + "The server is lagging. Retrying in 20 seconds.", ioe);
        }
      }
      return false;
    }
    return true;
  }
  
  private void handleLogin(String name, String password, boolean sendExtraBytes, boolean usingWeb, boolean reconnecting, boolean isUndead, String serverPassword, String steamIDAsString)
  {
    if (!preValidateLogin(name, steamIDAsString)) {
      return;
    }
    String hashedSteamId = steamIDAsString;
    try
    {
      hashedSteamId = hashPassword(hashedSteamId, encrypt(raiseFirstLetter(name)));
    }
    catch (Exception ex)
    {
      logger.log(Level.SEVERE, name + " Failed to encrypt password", ex);
      String message = "We failed to encrypt your password. Please try another.";
      try
      {
        Server.getInstance().steamHandler.removeIsPlayerAuthenticated(steamIDAsString);
        Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
        sendLoginAnswer(false, "We failed to encrypt your password. Please try another.", 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, 0);
      }
      catch (IOException ioe)
      {
        if (logger.isLoggable(Level.FINE)) {
          logger.log(Level.FINE, this.conn.getIp() + ", problem sending login denied message: " + "We failed to encrypt your password. Please try another.", ioe);
        }
      }
      return;
    }
    if (!Server.getInstance().steamHandler.isPlayerAuthenticated(steamIDAsString))
    {
      Server.getInstance().steamHandler.removeIsPlayerAuthenticated(steamIDAsString);
      try
      {
        String message = "You need to be authenticated";
        Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
        sendLoginAnswer(false, message, 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, -2);
      }
      catch (IOException localIOException1) {}
      if (!password.equals(hashedSteamId)) {
        logger.log(Level.INFO, "Unauthenticated user trying to login with incorrect credentials, with ip: " + this.conn.getIp());
      } else {
        logger.log(Level.INFO, "Unauthenticated user trying to login, with ip: " + this.conn.getIp());
      }
      return;
    }
    Server.getInstance().steamHandler.removeIsPlayerAuthenticated(steamIDAsString);
    if (!password.equals(hashedSteamId))
    {
      try
      {
        String message = "You need to be authenticated";
        Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
        sendLoginAnswer(false, message, 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, -2);
      }
      catch (IOException localIOException2) {}
      logger.log(Level.INFO, "Authenticated user trying to login with incorrect credentials, with ip: " + this.conn.getIp());
      return;
    }
    String steamServerPassword = Servers.localServer.getSteamServerPassword();
    if (!steamServerPassword.equals(serverPassword))
    {
      try
      {
        String message = "Incorrect server password!";
        Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
        sendLoginAnswer(false, message, 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, -2);
      }
      catch (IOException localIOException3) {}
      logger.log(Level.INFO, "Incorrect server password: " + this.conn.getIp());
      return;
    }
    if (checkName(name))
    {
      if ((isUndead) && (!Servers.localServer.LOGINSERVER)) {
        name = "Undead " + name;
      }
      boolean wasinvuln = false;
      try
      {
        Player p = Players.getInstance().getPlayer(name);
        p.setSteamID(SteamId.fromSteamID64(Long.valueOf(steamIDAsString).longValue()));
        
        String dbpassw = "";
        dbpassw = p.getSaveFile().getPassword();
        if (!reconnecting) {
          p.setSendExtraBytes(sendExtraBytes);
        }
        byte commandType;
        if ((dbpassw.equals(password)) || (sendExtraBytes))
        {
          Ban ban = Players.getInstance().getAnyBan(this.conn.getIp(), p, steamIDAsString);
          if (ban != null)
          {
            String time = Server.getTimeFor(ban.getExpiry() - System.currentTimeMillis());
            
            String message = ban.getIdentifier() + " is banned for " + time + " more. Reason: " + ban.getReason();
            if (ban.getExpiry() - System.currentTimeMillis() > 29030400000L) {
              message = ban.getIdentifier() + " is permanently banned. Reason: " + ban.getReason();
            }
            try
            {
              Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
              sendLoginAnswer(false, message, 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, 0);
            }
            catch (IOException localIOException4) {}
            logger.log(Level.INFO, name + " is banned, trying to log on from " + this.conn.getIp());
            
            return;
          }
          if ((!p.isFullyLoaded()) || (p.isLoggedOut()))
          {
            try
            {
              Zone zone = Zones.getZone(p.getTileX(), p.getTileY(), p.isOnSurface());
              zone.deleteCreature(p, true);
            }
            catch (NoSuchZoneException|NoSuchCreatureException|NoSuchPlayerException localNoSuchZoneException1) {}
            Players.getInstance().logoutPlayer(p);
            logger.log(Level.INFO, this.conn.getIp() + "," + name + " logged on too early after reconnecting.");
            
            return;
          }
          if ((p.getCommunicator().getCurrentmove() != null) && (p.getCommunicator().getCurrentmove().getNext() != null))
          {
            logger.log(Level.INFO, this.conn.getIp() + "," + name + " was still moving at reconnect - " + p.getCommunicator().getMoves());
            String message = "You are still moving on the server. Retry in 10 seconds.";
            try
            {
              Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
              sendLoginAnswer(false, "You are still moving on the server. Retry in 10 seconds.", 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, 10);
            }
            catch (IOException ioe)
            {
              if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, this.conn.getIp() + ", " + name + ", problem sending login denied message: " + "You are still moving on the server. Retry in 10 seconds.", ioe);
              }
            }
            return;
          }
          if (p.getSaveFile().realdeath > 4)
          {
            String message = "Your account has suffered real death. You can not log on.";
            try
            {
              Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
              sendLoginAnswer(false, "Your account has suffered real death. You can not log on.", 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, 0);
            }
            catch (IOException ioe)
            {
              if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, this.conn.getIp() + ", " + name + ", problem sending login denied message: " + "Your account has suffered real death. You can not log on.", ioe);
              }
            }
            return;
          }
          logger.log(Level.INFO, this.conn.getIp() + "," + name + " successfully reconnected.");
          wasinvuln = p.getCommunicator().isInvulnerable();
          p.getCommunicator().sendShutDown("Reconnected", true);
          
          p.stopTeleporting();
          p.getMovementScheme().clearIntraports();
          p.getCommunicator().player = null;
          p.setCommunicator(new PlayerCommunicator(p, this.conn));
          this.conn.setLogin(true);
          if (p.getSaveFile().currentServer != Servers.localServer.id)
          {
            p.getSaveFile().setLogin();
            p.getSaveFile().logout();
            
            String message = "Failed to redirect to another server.";
            logger.log(Level.INFO, this.conn.getIp() + "," + name + " redirected from " + Servers.localServer.id + " to " + 
              p.getSaveFile().currentServer);
            try
            {
              ServerEntry entry = Servers.getServerWithId(p.getSaveFile().currentServer);
              if (entry != null)
              {
                if (entry.isAvailable(p.getPower(), p.isReallyPaying()))
                {
                  p.getCommunicator().sendReconnect(entry.EXTERNALIP, Integer.parseInt(entry.EXTERNALPORT), password);
                }
                else
                {
                  Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                  message = "The server is currently not available. Please try later.";
                  sendLoginAnswer(false, message, 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, 300);
                }
              }
              else
              {
                Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                sendLoginAnswer(false, message, 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, 0);
              }
            }
            catch (IOException localIOException5) {}
            Players.getInstance().logoutPlayer(p);
            redirects += 1;
            this.conn.ticksToDisconnect = 400;
            return;
          }
          logins += 1;
          p.setLoginHandler(this);
          this.conn.setConnectionListener(p.getCommunicator());
          Server.getInstance().addIp(this.conn.getIp());
          p.setIpaddress(this.conn.getIp());
          if (p.isTransferring())
          {
            String message = "You are being transferred to another server.";
            try
            {
              Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
              sendLoginAnswer(false, "You are being transferred to another server.", 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, 60);
            }
            catch (IOException ioe)
            {
              if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, this.conn.getIp() + ", " + name + ", problem sending login denied message: " + "You are being transferred to another server.", ioe);
              }
            }
            return;
          }
          if ((p.getPower() < 1) && (!p.isPaying()) && 
            (Players.getInstance().numberOfPlayers() > Servers.localServer.pLimit))
          {
            String message = "The server is full. If you pay for a premium account you will be able to enter anyway. Retrying in 60 seconds.";
            try
            {
              Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
              sendLoginAnswer(false, "The server is full. If you pay for a premium account you will be able to enter anyway. Retrying in 60 seconds.", 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, 60);
            }
            catch (IOException ioe)
            {
              if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, this.conn.getIp() + ", " + name + ", problem sending login denied message: " + "The server is full. If you pay for a premium account you will be able to enter anyway. Retrying in 60 seconds.", ioe);
              }
            }
            return;
          }
          if (Constants.maintaining) {
            if (p.getPower() <= 1)
            {
              String message = "The server is in maintenance mode. Retrying in 60 seconds.";
              try
              {
                Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                sendLoginAnswer(false, "The server is in maintenance mode. Retrying in 60 seconds.", 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, 60);
              }
              catch (IOException ioe)
              {
                if (logger.isLoggable(Level.FINE)) {
                  logger.log(Level.FINE, this.conn.getIp() + ", " + name + ", problem sending login denied message: " + "The server is in maintenance mode. Retrying in 60 seconds.", ioe);
                }
              }
              return;
            }
          }
          p.setLink(true);
          if (!p.isDead()) {
            p.setNewTile(null, 0, true);
          }
          if (p.isTeleporting()) {
            p.cancelTeleport();
          }
          putOutsideWall(p);
          if (p.isOnSurface())
          {
            putOutsideFence(p);
            if ((!p.isDead()) && (creatureIsInsideWrongHouse(p, false))) {
              putOutsideHouse(p, false);
            }
          }
          p.sendAddChampionPoints();
          p.getCommunicator().sendWeather();
          p.getCommunicator().checkSendWeather();
          p.getCommunicator().sendSleepInfo();
          if (!p.isDead())
          {
            putInBoatAndAssignSeat(p, true);
            try
            {
              Zone zone = Zones.getZone(p.getTileX(), p.getTileY(), p.isOnSurface());
              zone.addCreature(p.getWurmId());
            }
            catch (NoSuchZoneException nsz)
            {
              logger.log(Level.WARNING, nsz.getMessage(), nsz);
              p.logoutIn(2, "You were out of bounds.");
              return;
            }
            catch (NoSuchCreatureException|NoSuchPlayerException e)
            {
              logger.log(Level.WARNING, e.getMessage(), e);
              p.logoutIn(2, "A server error occurred.");
              return;
            }
          }
          try
          {
            String message = "Reconnecting " + name + "! " + (Servers.localServer.hasMotd() ? Servers.localServer.getMotd() : Constants.motd);
            
            float posx = p.getStatus().getPositionX();
            posy = p.getStatus().getPositionY();
            commandType = 0;
            if (p.isVehicleCommander())
            {
              Vehicle vehic = Vehicles.getVehicleForId(p.getVehicle());
              if (vehic != null)
              {
                Seat s = vehic.getPilotSeat();
                if ((s != null) && (s.occupant == p.getWurmId()))
                {
                  float posz = p.getStatus().getPositionZ();
                  try
                  {
                    VolaTile tile = Zones.getOrCreateTile((int)(p.getPosX() / 4.0F), 
                      (int)(p.getPosY() / 4.0F), p.getLayer() >= 0);
                    boolean skipSetZ = false;
                    if (tile != null)
                    {
                      Structure structure = tile.getStructure();
                      if (structure != null) {
                        skipSetZ = (structure.isTypeHouse()) || (structure.getWurmId() == p.getBridgeId());
                      }
                    }
                    if (!skipSetZ) {
                      posz = Zones.calculateHeight(p.getStatus().getPositionX(), p
                        .getStatus().getPositionY(), p
                        .isOnSurface());
                    }
                    if (posz < 0.0F) {
                      posz = Math.max(-1.45F, posz);
                    }
                    p.getStatus().setPositionZ(posz);
                  }
                  catch (NoSuchZoneException localNoSuchZoneException2) {}
                  commandType = vehic.commandType;
                  
                  posz = Math.max(posz + s.offz, s.offz);
                  posy += s.offy;
                  posx += s.offx;
                  p.getStatus().setPositionZ(posz);
                }
              }
            }
            p.getMovementScheme().setPosition(posx, posy, p.getStatus().getPositionZ(), p
              .getStatus().getRotation(), p.isOnSurface() ? 0 : -1);
            VolaTile targetTile = Zones.getTileOrNull((int)(posx / 4.0F), (int)(posy / 4.0F), p.isOnSurface());
            if (targetTile != null)
            {
              float height = p.getFloorLevel() > 0 ? p.getFloorLevel() * 3 : 0.0F;
              if (p.getBridgeId() > 0L) {
                height = 0.0F;
              }
              p.getMovementScheme().setGroundOffset((int)(height * 10.0F), true);
              p.calculateFloorLevel(targetTile, true);
            }
            p.getMovementScheme().haltSpeedModifier();
            p.setTeleporting(true);
            p.setTeleportCounter(p.getTeleportCounter() + 1);
            
            byte power = Players.isArtist(p.getWurmId(), false, false) ? 2 : (byte)p.getPower();
            sendLoginAnswer(true, message, p.getStatus().getPositionX(), p.getStatus().getPositionY(), p
              .getStatus().getPositionZ(), p.getStatus().getRotation(), p.isOnSurface() ? 0 : -1, p
              .getModelName(), power, 0, commandType, p
              .getKingdomTemplateId(), p.getFace(), p.getTeleportCounter(), p.getBlood(), p
              .getBridgeId(), p.getMovementScheme().getGroundOffset());
            if (logger.isLoggable(Level.FINE)) {
              logger.log(Level.FINE, "Sent " + p.getStatus().getPositionX() + "," + p.getStatus().getPositionY() + "," + p
                .getStatus().getPositionZ() + "," + p.getStatus().getRotation());
            }
          }
          catch (IOException ioe)
          {
            logger.log(Level.INFO, "Player " + name + " dropped during login.", ioe);
            p.logoutIn(2, "You seemed to have lost your connection to Wurm.");
            return;
          }
          Server.getInstance().addToPlayersAtLogin(p);
          try
          {
            p.loadSkills();
            p.sendSkills();
            sendAllItemModelNames(p);
            sendAllEquippedArmor(p);
            if (p.getStatus().getBody().getBodyItem() != null) {
              p.getStatus().getBody().getBodyItem().addWatcher(-1L, p);
            }
            p.getInventory().addWatcher(-1L, p);
            p.getBody().sendWounds();
            Players.loadAllPrivatePOIForPlayer(p);
            p.sendAllMapAnnotations();
            p.resetLastSentToolbelt();
            ValreiMapData.sendAllMapData(p);
          }
          catch (Exception ex)
          {
            logger.log(Level.SEVERE, "Failed to load status for player " + name + ".", ex);
            p.getCommunicator().sendAlertServerMessage("Failed to load your status! Please contact server administrators.");
            
            p.logoutIn(2, "The game failed to load your status. Please contact server administrators.");
            
            return;
          }
          p.sendReligion();
          p.sendKarma();
          p.sendScenarioKarma();
          if (p.getTeam() != null) {
            p.getTeam().creatureReconnectedTeam(p);
          }
          p.lastSentHasCompass = false;
          Players.getInstance().sendReconnect(p);
          try
          {
            p.sendActionControl(p.getCurrentAction().getActionString(), true, p
              .getCurrentAction().getTimeLeft());
          }
          catch (NoSuchActionException localNoSuchActionException) {}
          if (p.getSpellEffects() != null) {
            p.getSpellEffects().sendAllSpellEffects();
          }
          ItemBonus.sendAllItemBonusToPlayer(p);
          p.sendSpellResistances();
          p.getCommunicator().sendClimb(p.isClimbing());
          p.getCommunicator().sendToggle(0, p.isClimbing());
          p.getCommunicator().sendToggle(2, p.isLegal());
          p.getCommunicator().sendToggle(1, p.faithful);
          p.getCommunicator().sendToggle(3, p.isStealth());
          
          p.getCommunicator().sendToggle(4, p.isAutofight());
          p.getCommunicator().sendToggle(100, p.isArcheryMode());
          if (p.getShield() != null) {
            p.getCommunicator().sendToggleShield(true);
          }
          Item dragged = p.getMovementScheme().getDraggedItem();
          if (dragged != null)
          {
            Items.stopDragging(dragged);
            Items.startDragging(p, dragged);
          }
          Players.getInstance().addToGroups(p);
        }
        else
        {
          String message = "Password incorrect. Please try again or create a new player with a different name than " + name + ".";
          
          HackerIp ip = (HackerIp)failedIps.get(this.conn.getIp());
          if (ip != null)
          {
            ip.name = name;
            ip.timesFailed += 1;
            long atime = 0L;
            if (ip.timesFailed == 10) {
              atime = 180000L;
            }
            if (ip.timesFailed == 20) {
              atime = 600000L;
            } else if (ip.timesFailed % 20 == 0) {
              atime = 10800000L;
            }
            if (ip.timesFailed == 100) {
              Players.addGmMessage("System", "The ip " + this.conn.getIp() + " has failed the password for " + name + " 100 times. It is now banned one hour every failed attempt.");
            }
            if (ip.timesFailed > 100) {
              atime = 3600000L;
            }
            ip.mayTryAgain = (System.currentTimeMillis() + atime);
            if (atime > 0L) {
              message = message + " Because of the repeated failures you may try again in " + Server.getTimeFor(atime) + ".";
            }
          }
          else
          {
            failedIps.put(this.conn.getIp(), new HackerIp(1, System.currentTimeMillis(), name));
          }
          try
          {
            Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
            sendLoginAnswer(false, message, 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, -2);
          }
          catch (IOException ioe)
          {
            if (logger.isLoggable(Level.FINE)) {
              logger.log(Level.FINE, this.conn.getIp() + ", " + name + ", problem sending login denied message: " + message, ioe);
            }
          }
          return;
        }
        p.destroyVisionArea();
        try
        {
          p.createVisionArea();
        }
        catch (Exception ex)
        {
          logger.log(Level.WARNING, "Failed to create visionarea for player " + p.getName(), ex);
          p.logoutIn(2, "The game failed to create your vision area. Please contact the administrators.");
          return;
        }
        if (!p.hasLink())
        {
          p.destroyVisionArea();
          return;
        }
        if (!p.isDead())
        {
          VolaTile tile = p.getCurrentTile();
          if (tile == null)
          {
            logger.log(Level.WARNING, p.getName() + " isn't in the world. Adding and retrying.");
            p.sendToWorld();
          }
          p.getCommunicator().sendSelfToLocal();
          Achievements.sendAchievementList(p);
          p.getCommunicator().sendAllKingdoms();
          tile = p.getCurrentTile();
          doors = tile.getDoors();
          if (doors != null)
          {
            ioe = doors;posy = ioe.length;
            for (commandType = 0; commandType < posy; commandType++)
            {
              Door lDoor = ioe[commandType];
              if (lDoor.canBeOpenedBy(p, false)) {
                if ((lDoor instanceof FenceGate)) {
                  p.getCommunicator().sendOpenFence(((FenceGate)lDoor).getFence(), true, true);
                } else {
                  p.getCommunicator().sendOpenDoor(lDoor);
                }
              }
            }
          }
          if (!wasinvuln)
          {
            p.getCommunicator().sendAlertServerMessage("You are not invulnerable now.");
            p.getCommunicator().setInvulnerable(false);
          }
        }
        else
        {
          p.getCommunicator().sendDead();
          p.sendSpawnQuestion();
        }
        p.setFullyLoaded();
        p.getCommunicator().setReady(true);
        sendLoggedInPeople(p);
        sendStatus(p);
        
        p.getCombatHandler().sendRodEffect();
        p.sendHasFingerEffect();
        p.getStatus().sendStateString();
        p.getCommunicator().sendMapInfo();
        Server.getInstance().addToPlayersAtLogin(p);
        if ((p.getVisionArea() != null) && (p.getVisionArea().getSurface() != null)) {
          p.getVisionArea().getSurface().sendCreatureItems(p);
        }
        p.setBestLightsource(null, true);
        
        boolean isEducated = false;
        Door[] doors = p.getTitles();ioe = doors.length;
        for (posy = 0; posy < ioe; posy++)
        {
          Titles.Title t = doors[posy];
          if (t == Titles.Title.Educated) {
            isEducated = true;
          }
        }
        if (!isEducated) {
          PlayerTutorial.getTutorialForPlayer(p.getWurmId(), true).sendCurrentStageBML();
        }
        p.isLit = false;
        p.recalcLimitingFactor(null);
        if (p.getCultist() != null) {
          p.getCultist().sendBuffs();
        }
        AffinitiesTimed.sendTimedAffinitiesFor(p);
        if (p.isDeathProtected()) {
          p.getCommunicator().sendAddStatusEffect(SpellEffectsEnum.DEATH_PROTECTION, Integer.MAX_VALUE);
        }
        MissionPerformer.sendEpicMissionsPerformed(p, p.getCommunicator());
        mp = MissionPerformed.getMissionPerformer(p.getWurmId());
        if (mp != null) {
          mp.sendAllMissionPerformed(p.getCommunicator());
        }
        checkPutOnBoat(p);
      }
      catch (NoSuchPlayerException nsp)
      {
        float posy;
        MissionPerformer mp;
        PlayerInfo file = PlayerInfoFactory.createPlayerInfo(name);
        Player player = null;
        try
        {
          if (isUndead)
          {
            file.undeadType = ((byte)(1 + Server.rand.nextInt(3)));
            if (Servers.localServer.LOGINSERVER) {
              try
              {
                if (file.currentServer <= 1)
                {
                  mp = Servers.getAllServers();ioe = mp.length;
                  for (posy = 0; posy < ioe; posy++)
                  {
                    ServerEntry entry = mp[posy];
                    if ((entry.EPIC) && (entry.isAvailable(file.getPower(), true))) {
                      file.currentServer = entry.getId();
                    }
                  }
                }
                player = new Player(file, this.conn);
                player.setSteamID(SteamId.fromSteamID64(Long.valueOf(steamIDAsString).longValue()));
                Ban ban = Players.getInstance().getAnyBan(this.conn.getIp(), player, steamIDAsString);
                if (ban != null)
                {
                  try
                  {
                    String time = Server.getTimeFor(ban.getExpiry() - System.currentTimeMillis());
                    
                    String message = ban.getIdentifier() + " is banned for " + time + " more. Reason: " + ban.getReason();
                    
                    Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                    if (ban.getExpiry() - System.currentTimeMillis() > 29030400000L) {
                      message = ban.getIdentifier() + " is permanently banned. Reason: " + ban.getReason();
                    }
                    sendLoginAnswer(false, message, 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, 0);
                  }
                  catch (IOException ioe)
                  {
                    if (logger.isLoggable(Level.FINE)) {
                      logger.log(Level.FINE, this.conn.getIp() + ", " + name + " problem sending banned IP login denied message", ioe);
                    }
                  }
                  logger.log(Level.INFO, name + " is banned, trying to log on from " + this.conn.getIp());
                  return;
                }
                String message = "The server is currently not available. Please try later.";
                ServerEntry entry = Servers.getServerWithId(file.currentServer);
                if (entry != null)
                {
                  if (entry.isAvailable(file.getPower(), file.isPaying()))
                  {
                    player.getCommunicator().sendReconnect(entry.EXTERNALIP, 
                      Integer.parseInt(entry.EXTERNALPORT), password);
                    logger.log(Level.INFO, this.conn.getIp() + ", " + name + " redirected from " + Servers.localServer.id + " to server ID: " + file.currentServer);
                  }
                  else
                  {
                    Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                    message = "The server is currently not available. Please try later.";
                    sendLoginAnswer(false, message, 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, 300);
                    
                    logger.log(Level.INFO, this.conn.getIp() + ", " + name + " could not be redirected from " + Servers.localServer.id + " to server ID: " + file.currentServer + " not avail.");
                  }
                }
                else
                {
                  logger.warning(this.conn.getIp() + ", " + name + " could not be redirected from " + Servers.localServer.id + " to non-existant server ID: " + file.currentServer + ", the database entry is wrong");
                  
                  Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                  sendLoginAnswer(false, message, 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, -1);
                }
                return;
              }
              catch (IOException ioe)
              {
                return;
              }
            }
          }
          file.load();
          if (password.equals(file.getPassword()))
          {
            player = new Player(file, this.conn);
            player.setSteamID(SteamId.fromSteamID64(Long.valueOf(steamIDAsString).longValue()));
            Ban ban = Players.getInstance().getAnyBan(this.conn.getIp(), player, steamIDAsString);
            if (ban != null)
            {
              try
              {
                String time = Server.getTimeFor(ban.getExpiry() - System.currentTimeMillis());
                String message = ban.getIdentifier() + " is banned for " + time + " more. Reason: " + ban.getReason();
                
                Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                if (ban.getExpiry() - System.currentTimeMillis() > 29030400000L) {
                  message = ban.getIdentifier() + " is permanently banned. Reason: " + ban.getReason();
                }
                sendLoginAnswer(false, message, 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, 0);
              }
              catch (IOException ioe)
              {
                if (logger.isLoggable(Level.FINE)) {
                  logger.log(Level.FINE, this.conn.getIp() + ", " + name + " problem sending banned IP login denied message", ioe);
                }
              }
              logger.log(Level.INFO, name + " is banned, trying to log on from " + this.conn.getIp());
              return;
            }
            if (file.currentServer != Servers.localServer.id)
            {
              String message = "The server is currently not available. Please try later.";
              try
              {
                ServerEntry entry = Servers.getServerWithId(file.currentServer);
                if (entry != null)
                {
                  if (entry.isAvailable(file.getPower(), file.isPaying()))
                  {
                    player.getCommunicator().sendReconnect(entry.EXTERNALIP, 
                      Integer.parseInt(entry.EXTERNALPORT), password);
                    logger.log(Level.INFO, this.conn.getIp() + ", " + name + " redirected from " + Servers.localServer.id + " to server ID: " + file.currentServer);
                  }
                  else
                  {
                    Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                    message = "The server is currently not available. Please try later.";
                    sendLoginAnswer(false, message, 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, 300);
                    
                    logger.log(Level.INFO, this.conn.getIp() + ", " + name + " could not be redirected from " + Servers.localServer.id + " to server ID: " + file.currentServer + " not avail.");
                  }
                }
                else
                {
                  Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                  logger.warning(this.conn.getIp() + ", " + name + " could not be redirected from " + Servers.localServer.id + " to non-existant server ID: " + file.currentServer + ", the database entry is wrong");
                  
                  sendLoginAnswer(false, message, 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, -1);
                }
              }
              catch (IOException ioe)
              {
                if (logger.isLoggable(Level.FINE)) {
                  logger.log(Level.FINE, this.conn.getIp() + ", " + name + " problem redirecting from " + Servers.localServer.id + " to server ID: " + file.currentServer, ioe);
                }
              }
              file.lastLogin = (System.currentTimeMillis() - 10000L);
              file.logout();
              file.save();
              redirects += 1;
              this.conn.ticksToDisconnect = 400;
              return;
            }
            if ((!Constants.isGameServer) && (file.currentServer == Servers.localServer.id))
            {
              logger.log(Level.WARNING, name + " tried to logon locally.");
              String message = "You can not log on to this type of server. Contact a GM or Dev";
              try
              {
                Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                sendLoginAnswer(false, "You can not log on to this type of server. Contact a GM or Dev", 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, -1);
              }
              catch (IOException ioe)
              {
                if (logger.isLoggable(Level.FINE)) {
                  logger.log(Level.FINE, this.conn.getIp() + ", " + name + ", problem sending login denied message: " + "You can not log on to this type of server. Contact a GM or Dev", ioe);
                }
              }
              file.lastLogin = (System.currentTimeMillis() - 10000L);
              file.logout();
              file.save();
              this.conn.ticksToDisconnect = 400;
              return;
            }
            if (player.getSaveFile().realdeath > 4)
            {
              String message = "Your account has suffered real death. You can not log on.";
              try
              {
                Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                sendLoginAnswer(false, "Your account has suffered real death. You can not log on.", 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, -1);
              }
              catch (IOException ioe)
              {
                if (logger.isLoggable(Level.FINE)) {
                  logger.log(Level.FINE, this.conn.getIp() + ", " + name + ", problem sending login denied message: " + "Your account has suffered real death. You can not log on.", ioe);
                }
              }
              return;
            }
            player.setLoginHandler(this);
            
            this.conn.setConnectionListener(player.getCommunicator());
            logins += 1;
            if ((player.getPower() < 1) && (!player.isPaying()) && 
              (Players.getInstance().numberOfPlayers() > Servers.localServer.pLimit))
            {
              String message = "The server is full. If you pay for a premium account you will be able to enter anyway. Retrying in 60 seconds.";
              try
              {
                Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                sendLoginAnswer(false, "The server is full. If you pay for a premium account you will be able to enter anyway. Retrying in 60 seconds.", 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, 60);
              }
              catch (IOException ioe)
              {
                if (logger.isLoggable(Level.FINE)) {
                  logger.log(Level.FINE, this.conn.getIp() + ", " + name + ", problem sending login denied message: " + "The server is full. If you pay for a premium account you will be able to enter anyway. Retrying in 60 seconds.", ioe);
                }
              }
              return;
            }
            if ((player.getPower() < 1) && (!player.isPaying()) && (Servers.localServer.ISPAYMENT))
            {
              String message = "This server is a premium only server. You can not log on until you have purchased premium time in the webshop.";
              try
              {
                Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                sendLoginAnswer(false, "This server is a premium only server. You can not log on until you have purchased premium time in the webshop.", 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, 60);
              }
              catch (IOException ioe)
              {
                if (logger.isLoggable(Level.FINE)) {
                  logger.log(Level.FINE, this.conn.getIp() + ", " + name + ", problem sending login denied message: " + "This server is a premium only server. You can not log on until you have purchased premium time in the webshop.", ioe);
                }
              }
              return;
            }
            if (Constants.maintaining) {
              if (player.getPower() <= 1)
              {
                try
                {
                  Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                  sendLoginAnswer(false, "The server is in maintenance mode. Retrying in 60 seconds.", 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, 60);
                }
                catch (IOException ioe)
                {
                  if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, this.conn.getIp() + ", " + name + " problem sending maintenance mode login denied", ioe);
                  }
                }
                return;
              }
            }
            String timeLeft;
            if ((Constants.enableSpyPrevention) && (Servers.localServer.PVPSERVER) && (!Servers.localServer.testServer)) {
              if (player.getPower() < 1)
              {
                byte kingdom = Players.getInstance().getKingdomForPlayer(player.getWurmId());
                KingdomIp kip = KingdomIp.getKIP(this.conn.getIp(), kingdom);
                if (kip != null)
                {
                  long answer = kip.mayLogonKingdom(kingdom);
                  if (answer < 0L)
                  {
                    try
                    {
                      Kingdom k = Kingdoms.getKingdom(kip.getKingdom());
                      Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                      if (k != null) {
                        sendLoginAnswer(false, "Spy prevention: Someone is playing on kingdom " + k
                          .getName() + " from this ip address.", 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, 60);
                      } else {
                        sendLoginAnswer(false, "Spy prevention: Someone is playing on another kingdom from this ip address.", 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, 60);
                      }
                    }
                    catch (IOException ioe)
                    {
                      if (logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, this.conn.getIp() + ", " + name + " problem sending spy prevention login denied", ioe);
                      }
                    }
                    return;
                  }
                  if (answer > 1L)
                  {
                    timeLeft = Server.getTimeFor(answer);
                    if (answer < 0L)
                    {
                      try
                      {
                        Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                        Kingdom k = Kingdoms.getKingdom(kingdom);
                        if (k != null) {
                          sendLoginAnswer(false, "Spy prevention: You have to wait " + timeLeft + " because someone was recently playing " + k
                            .getName() + " from this ip address.", 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, 60);
                        } else {
                          sendLoginAnswer(false, "Spy prevention: You have to wait " + timeLeft + " because someone was recently playing in another kingdom from this ip address.", 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, 60);
                        }
                      }
                      catch (IOException ioe)
                      {
                        if (logger.isLoggable(Level.FINE)) {
                          logger.log(Level.FINE, this.conn.getIp() + ", " + name + " problem sending spy prevention login denied", ioe);
                        }
                      }
                      return;
                    }
                  }
                  kip.logon(kingdom);
                }
              }
            }
            logger.log(Level.INFO, this.conn
              .getIp() + "," + name + " successfully logged on, id: " + player.getWurmId() + '.');
            
            Server.getInstance().addPlayer(player);
            player.initialisePlayer(file);
            if (!reconnecting) {
              player.setSendExtraBytes(sendExtraBytes);
            }
            player.checkBodyInventoryConsistency();
            player.getBody().createBodyParts();
            Server.getInstance().startSendingFinals(player);
            long start = System.nanoTime();
            player.loadSkills();
            
            Items.loadAllItemsForCreature(player, player.getStatus().getInventoryId());
            player.getCommunicator().sendMapInfo();
            Players.loadAllPrivatePOIForPlayer(player);
            player.resetLastSentToolbelt();
            player.sendAllMapAnnotations();
            ValreiMapData.sendAllMapData(player);
            player.getCommunicator().sendClearTickets();
            player.getCommunicator().sendClearFriendsList();
            if (player.getCultist() != null) {
              player.getCultist().sendBuffs();
            }
            AffinitiesTimed.sendTimedAffinitiesFor(player);
            Players.getInstance().sendConnectInfo(player, " has logged in.", player.getLastLogin(), PlayerOnlineStatus.ONLINE, true);
            
            Players.getInstance().addToGroups(player);
            if (player.getBridgeId() != -10L)
            {
              BridgePart[] bridgeParts = player.getCurrentTile().getBridgeParts();
              boolean foundBridge = false;
              timeLeft = bridgeParts;ioe = timeLeft.length;
              for (localNoSuchZoneException2 = 0; localNoSuchZoneException2 < ioe; localNoSuchZoneException2++)
              {
                BridgePart bp = timeLeft[localNoSuchZoneException2];
                
                foundBridge = true;
                if (!bp.isFinished())
                {
                  foundBridge = false;
                  break;
                }
              }
              if (foundBridge)
              {
                timeLeft = bridgeParts;ioe = timeLeft.length;
                for (localNoSuchZoneException2 = 0; localNoSuchZoneException2 < ioe; localNoSuchZoneException2++)
                {
                  BridgePart bp = timeLeft[localNoSuchZoneException2];
                  if ((bp.isFinished()) && (bp.hasAnExit()) && (bp.getStructureId() != player.getBridgeId()))
                  {
                    logger.info(String.format("Player %s logged in at [%s, %s] where bridge ID %s used to be built, but has since been replaced by the bridge ID %s.", new Object[] { player.getName(), Integer.valueOf(player.getTileX()), Integer.valueOf(player.getTileY()), Long.valueOf(player.getBridgeId()), Long.valueOf(bp.getStructureId()) }));
                    player.setBridgeId(bp.getStructureId());
                    break;
                  }
                }
              }
              else
              {
                logger.info(String.format("Player %s logged in at [%s, %s] where a bridge used to be, but no longer exists.", new Object[] { player.getName(), Integer.valueOf(player.getTileX()), Integer.valueOf(player.getTileY()) }));
                player.setBridgeId(-10L);
              }
            }
            if (logger.isLoggable(Level.FINE)) {
              logger.info("Loading all skills and items took " + (float)(System.nanoTime() - start) / 1000000.0F + " millis for " + name);
            }
          }
          else
          {
            logger.log(Level.INFO, this.conn.getIp() + "," + name + ", tried to log in with wrong password.");
            if (file.getPower() > 0)
            {
              Players.getInstance().sendConnectAlert(this.conn
                .getIp() + "," + name + ", tried to log in with wrong password.");
              
              Players.addGmMessage(name, this.conn
                .getIp() + "," + name + ", tried to log in with wrong password.");
              
              WCGmMessage wc = new WCGmMessage(WurmId.getNextWCCommandId(), name, "(" + Servers.localServer.id + ") " + this.conn.getIp() + "," + name + ", tried to log in with wrong password.", false);
              
              wc.sendToLoginServer();
            }
            String message = "Password incorrect. Please try again or create a new player with a different name than " + name + ".";
            
            HackerIp ip = (HackerIp)failedIps.get(this.conn.getIp());
            if (ip != null)
            {
              ip.name = name;
              ip.timesFailed += 1;
              long atime = 0L;
              if (ip.timesFailed == 10) {
                atime = 180000L;
              }
              if (ip.timesFailed == 20) {
                atime = 600000L;
              } else if (ip.timesFailed % 20 == 0) {
                atime = 10800000L;
              }
              if (ip.timesFailed == 100) {
                Players.addGmMessage("System", "The ip " + this.conn.getIp() + " has failed the password for " + name + " 100 times. It is now banned one hour every failed attempt.");
              }
              if (ip.timesFailed > 100) {
                atime = 3600000L;
              }
              ip.mayTryAgain = (System.currentTimeMillis() + atime);
              if (atime > 0L) {
                message = message + " Because of the repeated failures you may try again in " + Server.getTimeFor(atime) + ".";
              }
            }
            else
            {
              failedIps.put(this.conn.getIp(), new HackerIp(1, System.currentTimeMillis(), name));
            }
            Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
            sendLoginAnswer(false, message, 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, 0);
            return;
          }
        }
        catch (Exception ex)
        {
          if ((!isUndead) && (!Servers.localServer.testServer) && (logger.isLoggable(Level.INFO)) && 
            (!Server.getInstance().isPS())) {
            logger.log(Level.INFO, "Caught Exception while trying to log player in:" + ex.getMessage() + " for " + name, ex);
          }
          try
          {
            if ((sendExtraBytes) || (Servers.localServer.testServer) || (isUndead) || (Server.getInstance().isPS()))
            {
              if (Constants.maintaining)
              {
                try
                {
                  Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                  sendLoginAnswer(false, "The server is in maintenance mode. Retrying in 60 seconds.", 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, 60);
                }
                catch (IOException ioe)
                {
                  if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, this.conn.getIp() + ", " + name + " problem sending maintenance mode login denied", ioe);
                  }
                }
                return;
              }
              if (Players.getInstance().numberOfPlayers() > Servers.localServer.pLimit)
              {
                String message = "The server is full. If you pay for a premium account you will be able to enter anyway. Retrying in 60 seconds.";
                try
                {
                  Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                  sendLoginAnswer(false, "The server is full. If you pay for a premium account you will be able to enter anyway. Retrying in 60 seconds.", 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, 60);
                }
                catch (IOException ioe)
                {
                  if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, this.conn.getIp() + ", " + name + ", problem sending login denied message: " + "The server is full. If you pay for a premium account you will be able to enter anyway. Retrying in 60 seconds.", ioe);
                  }
                }
                return;
              }
              if ((Servers.localServer.id != Servers.loginServer.id) && (!isUndead) && (!Servers.localServer.testServer))
              {
                String message = "There are multiple login servers in the cluster, please remove so it is only one";
                try
                {
                  Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                  sendLoginAnswer(false, "There are multiple login servers in the cluster, please remove so it is only one", 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, -1);
                }
                catch (IOException ioe)
                {
                  if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, this.conn.getIp() + ", " + name + ", problem sending login denied message: " + "There are multiple login servers in the cluster, please remove so it is only one", ioe);
                  }
                }
                return;
              }
              logger.log(Level.INFO, this.conn.getIp() + "," + name + " was created successfully.");
              player = Player.doNewPlayer(1, this.conn);
              player.setName(name);
              float posX = Servers.localServer.SPAWNPOINTJENNX * 4 + Server.rand.nextInt(10);
              float posY = Servers.localServer.SPAWNPOINTJENNY * 4 + Server.rand.nextInt(10);
              
              int r = Server.rand.nextInt(3);
              
              float rot = Server.rand.nextInt(360);
              byte kingdom = 1;
              if (isUndead)
              {
                kingdom = 0;
                float[] txty = Player.findRandomSpawnX(false, false);
                posX = txty[0];
                posY = txty[1];
              }
              else
              {
                if (Servers.localServer.KINGDOM != 0)
                {
                  kingdom = Servers.localServer.KINGDOM;
                }
                else if (r == 1)
                {
                  kingdom = 2;
                  posX = Servers.localServer.SPAWNPOINTMOLX * 4 + Server.rand.nextInt(10);
                  posY = Servers.localServer.SPAWNPOINTMOLY * 4 + Server.rand.nextInt(10);
                }
                else if (r == 2)
                {
                  kingdom = 3;
                  posX = Servers.localServer.SPAWNPOINTLIBX * 4 + Server.rand.nextInt(10);
                  posY = Servers.localServer.SPAWNPOINTLIBY * 4 + Server.rand.nextInt(10);
                }
                if (Servers.localServer.randomSpawns)
                {
                  float[] txty = Player.findRandomSpawnX(true, true);
                  posX = txty[0];
                  posY = txty[1];
                }
              }
              Spawnpoint sp = getInitialSpawnPoint(kingdom);
              if (sp != null)
              {
                posX = sp.tilex * 4 + Server.rand.nextInt(10);
                posY = sp.tiley * 4 + Server.rand.nextInt(10);
              }
              long wurmId = WurmId.getNextPlayerId();
              player = (Player)player.setWurmId(wurmId, posX, posY, rot, 0);
              Ban ban = Players.getInstance().getAnyBan(this.conn.getIp(), player, steamIDAsString);
              if (ban != null)
              {
                try
                {
                  String time = Server.getTimeFor(ban.getExpiry() - System.currentTimeMillis());
                  
                  String message = ban.getIdentifier() + " is banned for " + time + " more. Reason: " + ban.getReason();
                  
                  Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                  if (ban.getExpiry() - System.currentTimeMillis() > 29030400000L) {
                    message = ban.getIdentifier() + " is permanently banned. Reason: " + ban.getReason();
                  }
                  sendLoginAnswer(false, message, 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, 0);
                }
                catch (IOException ioe)
                {
                  if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, this.conn.getIp() + ", " + name + " problem sending IP banned login denied message", ioe);
                  }
                }
                logger.log(Level.INFO, name + " is banned, trying to log on from " + this.conn.getIp());
                
                return;
              }
              putOutsideWall(player);
              if (player.isOnSurface())
              {
                putOutsideHouse(player, false);
                putOutsideFence(player);
              }
              String message = "Welcome to Wurm, " + name + "! " + (Servers.localServer.hasMotd() ? Servers.localServer.getMotd() : Constants.motd);
              
              player.getMovementScheme().setPosition(player.getStatus().getPositionX(), player
                .getStatus().getPositionY(), player.getStatus().getPositionZ(), player
                .getStatus().getRotation(), player.isOnSurface() ? 0 : -1);
              VolaTile targetTile = Zones.getTileOrNull((int)(player.getStatus().getPositionX() / 4.0F), 
                (int)(player.getStatus().getPositionY() / 4.0F), player.isOnSurface());
              if (targetTile != null)
              {
                float height = player.getFloorLevel() > 0 ? player.getFloorLevel() * 3 : 0.0F;
                
                player.getMovementScheme().setGroundOffset((int)(height * 10.0F), true);
                player.calculateFloorLevel(targetTile, true);
              }
              player.getStatus().checkStaminaEffects(65535);
              player.getMovementScheme().haltSpeedModifier();
              player.setTeleporting(true);
              player.setTeleportCounter(player.getTeleportCounter() + 1);
              
              player.setNewPlayer(true);
              
              file.initialize(name, player.getWurmId(), password, "What is your mother's maiden name?", "Sawyer", Server.rand
              
                .nextLong(), sendExtraBytes);
              file.setEmailAddress(name + "@test.com");
              player.setSaveFile(file);
              player.setSteamID(SteamId.fromSteamID64(Long.valueOf(steamIDAsString).longValue()));
              if (player.isUndead()) {
                file.setUndeadData();
              }
              logins += 1;
              
              player.setLoginHandler(this);
              this.conn.setConnectionListener(player.getCommunicator());
              Server.getInstance().addIp(this.conn.getIp());
              player.setIpaddress(this.conn.getIp());
              player.setSteamID(SteamId.fromSteamID64(Long.valueOf(steamIDAsString).longValue()));
              player.setFlag(3, true);
              player.setFlag(53, true);
              
              Server.getInstance().addPlayer(player);
              
              player.getBody().createBodyParts();
              player.getLoginhandler().sendLoginAnswer(true, message, player.getStatus().getPositionX(), player.getStatus()
                .getPositionY(), player.getStatus().getPositionZ(), player.getStatus().getRotation(), player
                .isOnSurface() ? 0 : -1, player.getModelName(), (byte)0, 0, (byte)0, kingdom, 0L, player
                .getTeleportCounter(), player.getBlood(), player.getBridgeId(), player.getMovementScheme().getGroundOffset());
              SelectSpawnQuestion question = new SelectSpawnQuestion(player, "Define your character", "Please select gender:", player.getWurmId(), message, isUndead);
              question.sendQuestion();
              if (player.getStatus().getBody().getBodyItem() != null) {
                player.getStatus().getBody().getBodyItem().addWatcher(-1L, player);
              }
              player.setFlag(76, true);
            }
            else
            {
              String message = "You need to register an account on www.wurmonline.com.";
              try
              {
                Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                sendLoginAnswer(false, "You need to register an account on www.wurmonline.com.", 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, 0);
              }
              catch (IOException ioe)
              {
                if (logger.isLoggable(Level.FINE)) {
                  logger.log(Level.FINE, this.conn.getIp() + ", " + name + ", problem sending login denied message: " + "You need to register an account on www.wurmonline.com.", ioe);
                }
              }
              return;
            }
          }
          catch (Exception ex2)
          {
            logger.log(Level.WARNING, "Failed to create player with name " + name, ex2);
          }
        }
      }
    }
  }
  
  private void setHasLoadedItems(int step)
  {
    if (logger.isLoggable(Level.FINER)) {
      logger.finer("Setting loadedItems to " + step);
    }
    this.loadedItems = step;
  }
  
  int loadPlayer(final Player player, int step)
  {
    if (logger.isLoggable(Level.FINEST)) {
      logger.finest("Loading " + player + ", step: " + step);
    }
    if (step == 0)
    {
      player.sendAddChampionPoints();
      
      player.getSaveFile().frozenSleep = true;
      
      return step;
    }
    if (step == 1)
    {
      final Player p = player;
      final LoginHandler l = this;
      if (this.loadedItems == 0)
      {
        Thread t = new Thread("PlayerLoader-Thread-" + p.getWurmId())
        {
          public void run()
          {
            try
            {
              p.getBody().load();
              
              player.getSaveFile().loadIgnored(player.getWurmId());
              player.getSaveFile().loadFriends(player.getWurmId());
              player.getSaveFile().loadTitles(player.getWurmId());
              player.getSaveFile().loadHistoryIPs(player.getWurmId());
              player.getSaveFile().loadHistoryEmails(player.getWurmId());
              l.setHasLoadedItems(1);
            }
            catch (Exception sex2)
            {
              try
              {
                LoginHandler.logger.log(Level.WARNING, p.getName() + " has no body. Creating!", sex2);
                p.getStatus().createNewBody();
                l.setHasLoadedItems(1);
              }
              catch (Exception sex)
              {
                LoginHandler.logger.log(Level.WARNING, p.getName() + " has no body.", sex);
                l.setHasLoadedItems(-1);
              }
            }
          }
        };
        this.loadedItems = Integer.MAX_VALUE;
        t.setPriority(4);
        t.start();
      }
      if (logger.isLoggable(Level.FINER)) {
        logger.finer("Body step=" + step + ", loadedItems=" + this.loadedItems);
      }
      if ((this.loadedItems == 1) || (this.loadedItems == -1)) {
        return this.loadedItems;
      }
      return step - 1;
    }
    if (step == 2)
    {
      if (!player.isReallyPaying()) {
        if (player.hasFlag(8))
        {
          if (player.getPaymentExpire() == 0L) {
            logger.log(Level.INFO, player.getName() + " logged on to prevent expiry.");
          }
          player.setFlag(8, false);
        }
      }
      this.loadedItems = 2;
      return this.loadedItems;
    }
    if (step == 3)
    {
      putOutsideWall(player);
      if (player.isOnSurface())
      {
        putOutsideHouse(player, true);
        putOutsideFence(player);
      }
      player.getCommunicator().sendWeather();
      player.getCommunicator().checkSendWeather();
      if (!player.isDead()) {
        putInBoatAndAssignSeat(player, false);
      }
      return step;
    }
    byte power;
    if (step == 4)
    {
      player.getMovementScheme().setPosition(player.getStatus().getPositionX(), player.getStatus().getPositionY(), player
        .getStatus().getPositionZ(), player.getStatus().getRotation(), player.isOnSurface() ? 0 : -1);
      VolaTile targetTile = Zones.getTileOrNull((int)(player.getStatus().getPositionX() / 4.0F), 
        (int)(player.getStatus().getPositionY() / 4.0F), player.isOnSurface());
      if (targetTile != null)
      {
        float height = player.getFloorLevel() > 0 ? player.getFloorLevel() * 3 : 0.0F;
        if (player.getBridgeId() > 0L) {
          height = 0.0F;
        }
        player.getMovementScheme().setGroundOffset((int)(height * 10.0F), true);
        player.calculateFloorLevel(targetTile, true);
      }
      player.getMovementScheme().haltSpeedModifier();
      try
      {
        String message = "Welcome back, " + player.getName() + "! " + (Servers.localServer.hasMotd() ? Servers.localServer.getMotd() : Constants.motd);
        
        player.setTeleporting(true);
        player.setTeleportCounter(player.getTeleportCounter() + 1);
        
        power = Players.isArtist(player.getWurmId(), false, false) ? 2 : (byte)player.getPower();
        sendLoginAnswer(true, message, player.getStatus().getPositionX(), player.getStatus().getPositionY(), player
          .getStatus().getPositionZ(), player.getStatus().getRotation(), player.isOnSurface() ? 0 : -1, player
          .getModelName(), power, 0, (byte)0, player
          
          .getKingdomId(), player.getFace(), player.getTeleportCounter(), player.getBlood(), player
          .getBridgeId(), player.getMovementScheme().getGroundOffset());
        if (logger.isLoggable(Level.FINE)) {
          logger.log(Level.FINE, player.getName() + ": sent Position X,Y,Z,Rotation: " + player
            .getStatus().getPositionX() + "," + player.getStatus().getPositionY() + "," + player
            .getStatus().getPositionZ() + "," + player.getStatus().getRotation());
        }
      }
      catch (IOException ioe)
      {
        logger.log(Level.FINE, "Player " + player.getName() + " dropped during login.", ioe);
        return -1;
      }
      sendAllItemModelNames(player);
      sendAllEquippedArmor(player);
      return step;
    }
    if (step == 5)
    {
      if (!player.isDead()) {
        if (!willGoOnBoat(player)) {
          try
          {
            player.createVisionArea();
          }
          catch (Exception ve)
          {
            logger.log(Level.WARNING, "Failed to create visionarea for player " + player.getName(), ve);
            return -1;
          }
        }
      }
      if (!player.hasLink())
      {
        player.destroyVisionArea();
        return -1;
      }
      return step;
    }
    Door[] doors;
    byte b1;
    if (step == 6)
    {
      player.getCommunicator().sendToggle(0, player.isClimbing());
      player.getCommunicator().sendToggle(2, player.isLegal());
      player.getCommunicator().sendToggle(1, player.faithful);
      player.getCommunicator().sendToggle(3, player.isStealth());
      player.getCommunicator().sendToggle(4, player.isAutofight());
      if (player.isStealth()) {
        player.getMovementScheme().setStealthMod(true);
      }
      if (player.getShield() != null) {
        player.getCommunicator().sendToggleShield(true);
      }
      if (player.getPower() > 0)
      {
        player.getStatus().visible = false;
        player.getCommunicator().sendNormalServerMessage("You should not be visible now.");
      }
      player.sendActionControl("", false, 0);
      if (!player.isDead())
      {
        player.getCommunicator().sendClimb(player.isClimbing());
        player.sendToWorld();
        
        player.getCommunicator().sendSelfToLocal();
        player.getCommunicator().sendAllKingdoms();
        
        Achievements.sendAchievementList(player);
        if (player.getVisionArea() != null) {
          if (this.redirected)
          {
            player.getCommunicator().sendAlertServerMessage("You may not move right now.");
            player.transferCounter = 10;
          }
          else
          {
            player.getCommunicator().setReady(true);
          }
        }
        VolaTile tile = player.getCurrentTile();
        if (tile != null)
        {
          doors = tile.getDoors();
          if (doors != null)
          {
            power = doors;b1 = power.length;
            for (byte b2 = 0; b2 < b1; b2++)
            {
              Door lDoor = power[b2];
              if (lDoor.covers(player.getPosX(), player.getPosY(), player.getPositionZ(), player.getFloorLevel(), player
                .followsGround())) {
                if (lDoor.canBeOpenedBy(player, false)) {
                  if ((lDoor instanceof FenceGate)) {
                    player.getCommunicator().sendOpenFence(((FenceGate)lDoor).getFence(), true, true);
                  } else {
                    player.getCommunicator().sendOpenDoor(lDoor);
                  }
                }
              }
            }
          }
        }
        else
        {
          logger.log(Level.WARNING, player.getName() + "- tile is null!", new Exception());
        }
      }
      else
      {
        player.getCommunicator().sendDead();
      }
      return step;
    }
    if (step == 7)
    {
      player.getBody().getBodyItem().addWatcher(-1L, player);
      return step;
    }
    if (step == 8)
    {
      player.getInventory().addWatcher(-1L, player);
      return step;
    }
    if (step == 9)
    {
      setStamina(player);
      if (player.isDead()) {
        player.sendSpawnQuestion();
      } else {
        player.checkChallengeWarnQuestion();
      }
      player.recalcLimitingFactor(null);
      return step;
    }
    if (step == 10)
    {
      player.getBody().loadWounds();
      
      return step;
    }
    if (step == 11)
    {
      if (player.mayHearDevTalk()) {
        Players.sendGmMessages(player);
      }
      if (player.mayHearMgmtTalk()) {
        Players.sendMgmtMessages(player);
      }
      return step;
    }
    if (step == 12)
    {
      player.createSpellEffects();
      player.addNewbieBuffs();
      player.getSaveFile().setLogin();
      player.sendSpellResistances();
      return step;
    }
    if (step == 13)
    {
      sendStatus(player);
      Team t = Groups.getTeamForOfflineMember(player.getWurmId());
      if (t != null) {
        player.setTeam(t, false);
      }
      return step;
    }
    if (step == 14)
    {
      player.getStatus().sendStateString();
      player.setBestLightsource(null, true);
      return step;
    }
    if (step == 15)
    {
      if (player.hasLink())
      {
        player.setIpaddress(this.conn.getIp());
        Server.getInstance().addIp(this.conn.getIp());
        MissionPerformer.sendEpicMissionsPerformed(player, player.getCommunicator());
        MissionPerformer mp = MissionPerformed.getMissionPerformer(player.getWurmId());
        if (mp != null) {
          mp.sendAllMissionPerformed(player.getCommunicator());
        }
        return step;
      }
      return Integer.MAX_VALUE;
    }
    if (step == 16)
    {
      sendLoggedInPeople(player);
      player.sendReligion();
      player.sendKarma();
      player.sendScenarioKarma();
      player.setFullyLoaded();
      if (player.getCultist() != null) {
        player.getCultist().sendBuffs();
      }
      AffinitiesTimed.sendTimedAffinitiesFor(player);
      if (player.isDeathProtected()) {
        player.getCommunicator().sendAddStatusEffect(SpellEffectsEnum.DEATH_PROTECTION, Integer.MAX_VALUE);
      }
      player.recalcLimitingFactor(null);
      player.getCommunicator().sendSafeServerMessage("Type /help for available commands.");
      if (player.isOnHostileHomeServer()) {
        player.getCommunicator().sendAlertServerMessage("These enemy lands drain you of your confidence. You fight less effectively.");
      }
      boolean isEducated = false;
      doors = player.getTitles();power = doors.length;
      for (b1 = 0; b1 < power; b1++)
      {
        Object t = doors[b1];
        if (t == Titles.Title.Educated) {
          isEducated = true;
        }
      }
      if (!isEducated) {
        PlayerTutorial.getTutorialForPlayer(player.getWurmId(), true).sendCurrentStageBML();
      }
      return step;
    }
    if (step == 17)
    {
      checkReimbursement(player);
      if (player.getSaveFile().pet != -10L) {
        try
        {
          Creature c = Creatures.getInstance().getCreature(player.getSaveFile().pet);
          if (c.dominator != player.getWurmId())
          {
            player.getCommunicator().sendNormalServerMessage(c.getNameWithGenus() + " is no longer your pet.");
            player.setPet(-10L);
          }
        }
        catch (NoSuchCreatureException nsc)
        {
          try
          {
            Creature c = Creatures.getInstance().loadOfflineCreature(player.getSaveFile().pet);
            if (c.dominator != player.getWurmId())
            {
              if (logger.isLoggable(Level.FINER)) {
                logger.finer(c.getName() + "," + c.getWurmId() + " back from offline - no longer dominated by " + player
                  .getWurmId());
              }
              player.getCommunicator().sendNormalServerMessage(c.getNameWithGenus() + " is no longer your pet.");
              player.setPet(-10L);
            }
          }
          catch (NoSuchCreatureException nsc2)
          {
            if (logger.isLoggable(Level.FINER)) {
              logger.finer("Failed to load from offline to " + player.getSaveFile().pet);
            }
            player.getCommunicator().sendNormalServerMessage("Your pet is nowhere to be found. It may have died of old age.");
            
            player.setPet(-10L);
          }
        }
      }
      Creatures.getInstance().returnCreaturesForPlayer(player.getWurmId());
      if ((player.getVisionArea() != null) && (player.getVisionArea().getSurface() != null)) {
        player.getVisionArea().getSurface().sendCreatureItems(player);
      }
      checkPutOnBoat(player);
      if (!player.checkTileInvulnerability())
      {
        player.getCommunicator().sendAlertServerMessage("You are not invulnerable here.");
        player.getCommunicator().setInvulnerable(false);
      }
      if (player.isStealth()) {
        player.setStealth(false);
      }
      return step;
    }
    return Integer.MAX_VALUE;
  }
  
  static int createPlayer(Player player, int step)
  {
    if (logger.isLoggable(Level.FINEST)) {
      logger.finest("Creating player " + player + ", step: " + step);
    }
    if (step == 0)
    {
      try
      {
        player.loadSkills();
        player.sendSkills();
      }
      catch (Exception ex)
      {
        logger.log(Level.INFO, "Failed to create skills: " + ex.getMessage(), ex);
        return -1;
      }
      return step;
    }
    if (step == 1)
    {
      try
      {
        player.getBody().createBodyParts();
      }
      catch (Exception ex)
      {
        logger.log(Level.INFO, "Failed to create bodyparts: " + ex.getMessage(), ex);
        return -1;
      }
      return step;
    }
    if (step == 2)
    {
      try
      {
        player.createPossessions();
      }
      catch (Exception ex)
      {
        logger.log(Level.INFO, "Failed to create possessions: " + ex.getMessage(), ex);
        return -1;
      }
      return step;
    }
    if (step == 3) {
      return step;
    }
    if (step == 4)
    {
      try
      {
        player.createVisionArea();
      }
      catch (Exception ve)
      {
        logger.log(Level.WARNING, "Failed to create visionarea for player " + player.getName(), ve);
        
        return -1;
      }
      if (!player.hasLink())
      {
        player.destroyVisionArea();
        return -1;
      }
      player.getCommunicator().setReady(true);
      return step;
    }
    if (step == 5)
    {
      player.createSomeItems(1.0F, false);
      return step;
    }
    if (step == 6)
    {
      player.setFullyLoaded();
      player.getCommunicator().sendToggle(0, player.isClimbing());
      player.getCommunicator().sendToggle(2, player.isLegal());
      player.getCommunicator().sendToggle(1, player.faithful);
      player.getCommunicator().sendToggle(3, player.isStealth());
      player.getCommunicator().sendToggle(4, player.isAutofight());
      return step;
    }
    if (step == 7)
    {
      player.sendReligion();
      player.sendKarma();
      player.sendScenarioKarma();
      player.sendToWorld();
      player.getCommunicator().sendWeather();
      player.getCommunicator().checkSendWeather();
      player.getCommunicator().sendSelfToLocal();
      return step;
    }
    if (step == 8)
    {
      if (!player.isGuest())
      {
        player.getStatus().setStatusExists(true);
        try
        {
          player.save();
        }
        catch (Exception ex)
        {
          logger.log(Level.INFO, "Failed to save player: " + ex.getMessage(), ex);
          return -1;
        }
      }
      return step;
    }
    if (step == 9) {
      return step;
    }
    if (step == 10)
    {
      player.createSpellEffects();
      player.getSaveFile().setLogin();
      return step;
    }
    if (step == 11)
    {
      sendStatus(player);
      return step;
    }
    if (step == 12)
    {
      player.getStatus().sendStateString();
      return step;
    }
    if (step == 13)
    {
      sendLoggedInPeople(player);
      MissionPerformer.sendEpicMissionsPerformed(player, player.getCommunicator());
      MissionPerformer mp = MissionPerformed.getMissionPerformer(player.getWurmId());
      if (mp != null) {
        mp.sendAllMissionPerformed(player.getCommunicator());
      }
      return step;
    }
    if (step == 14)
    {
      checkReimbursement(player);
      if (player.isNew())
      {
        Item mirroritem = player.getCarriedItem(781);
        if (mirroritem != null) {
          player.getCommunicator().sendCustomizeFace(player.getFace(), mirroritem.getWurmId());
        }
      }
      player.setNewPlayer(false);
      if ((player.getVisionArea() != null) && (player.getVisionArea().getSurface() != null)) {
        player.getVisionArea().getSurface().sendCreatureItems(player);
      }
      sendAllEquippedArmor(player);
      sendAllItemModelNames(player);
      player.getCommunicator().sendPlonk((short)50);
      
      return Integer.MAX_VALUE;
    }
    return Integer.MAX_VALUE;
  }
  
  private static void checkReimbursement(Player player)
  {
    player.reimburse();
  }
  
  private static void setStamina(Player player)
  {
    if (System.currentTimeMillis() - player.getSaveFile().getLastLogin() < 21600000L)
    {
      player.getStatus().modifyStamina2(
        (float)(System.currentTimeMillis() - player.getSaveFile().lastLogout) / 2.16E7F);
    }
    else
    {
      player.getStatus().modifyStamina2(1.0F);
      player.getStatus().modifyHunger(55536, 0.5F);
      player.getStatus().modifyThirst(-10000.0F);
    }
  }
  
  private static boolean creatureIsInsideWrongHouse(Player player, boolean load)
  {
    if (player.getPower() > 1) {
      return false;
    }
    VolaTile startTile = null;
    int tilex = player.getTileX();
    int tiley = player.getTileY();
    startTile = Zones.getTileOrNull(tilex, tiley, player.isOnSurface());
    if (startTile != null)
    {
      Structure struct = startTile.getStructure();
      if ((struct != null) && (struct.isFinished())) {
        return !struct.mayPass(player);
      }
    }
    return false;
  }
  
  public static boolean putOutsideHouse(Player player, boolean load)
  {
    if (player.getPower() > 1) {
      return false;
    }
    VolaTile startTile = null;
    int tilex = player.getTileX();
    int tiley = player.getTileY();
    startTile = Zones.getTileOrNull(tilex, tiley, player.isOnSurface());
    if (startTile != null)
    {
      Structure struct = startTile.getStructure();
      if ((struct != null) && (struct.isFinished()) && (struct.isTypeHouse()))
      {
        Item[] keys = player.getKeys();
        Item[] arrayOfItem1 = keys;int i = arrayOfItem1.length;
        for (Item localItem1 = 0; localItem1 < i; localItem1++)
        {
          lKey = arrayOfItem1[localItem1];
          if (lKey.getWurmId() == struct.getWritId()) {
            return false;
          }
        }
        if (struct.mayPass(player)) {
          return false;
        }
        Door[] doors = struct.getAllDoors();
        Door[] arrayOfDoor1 = doors;localItem1 = arrayOfDoor1.length;
        for (Item lKey = 0; lKey < localItem1; lKey++)
        {
          Door door = arrayOfDoor1[lKey];
          if (!door.isLocked()) {
            if (door.getOuterTile().getStructure() != struct) {
              return false;
            }
          }
        }
        arrayOfDoor1 = doors;Item localItem2 = arrayOfDoor1.length;
        for (lKey = 0; lKey < localItem2; lKey++)
        {
          Door door = arrayOfDoor1[lKey];
          if (door.getOuterTile().getStructure() != struct)
          {
            startTile = door.getOuterTile();
            break;
          }
        }
        if (startTile == null) {
          startTile = Zones.getOrCreateTile(Server.rand.nextBoolean() ? struct.getMaxX() + 1 : struct.getMinX() - 1, Server.rand
            .nextBoolean() ? struct.getMaxY() + 1 : struct.getMinY() - 1, true);
        }
        float posX = (startTile.getTileX() << 2) + 2;
        float posY = (startTile.getTileY() << 2) + 2;
        if (Servers.localServer.entryServer)
        {
          posX = (startTile.getTileX() << 2) + 0.5F + Server.rand.nextFloat() * 3.0F;
          posY = (startTile.getTileY() << 2) + 0.5F + Server.rand.nextFloat() * 3.0F;
        }
        if (logger.isLoggable(Level.FINE)) {
          logger.fine("Setting " + player.getName() + " outside structure " + struct.getName() + " on " + startTile
            .getTileX() + ", " + startTile.getTileY() + ". New or reconnect=" + (!load));
        }
        MountTransfer mt = MountTransfer.getTransferFor(player.getWurmId());
        if (mt != null) {
          mt.remove(player.getWurmId());
        }
        player.setPositionX(posX);
        player.setPositionY(posY);
        if (player.getVehicle() != -10L) {
          player.disembark(false);
        }
        try
        {
          player.setPositionZ(Zones.calculateHeight(posX, posY, true));
        }
        catch (NoSuchZoneException nsz)
        {
          logger.log(Level.WARNING, player.getName() + " ending up outside map: " + player.getStatus().getPositionX() + ", " + player
            .getStatus().getPositionY());
          player.calculateSpawnPoints();
          if (player.spawnpoints != null)
          {
            Iterator localIterator = player.spawnpoints.iterator();
            if (localIterator.hasNext())
            {
              Spawnpoint p = (Spawnpoint)localIterator.next();
              
              tilex = p.tilex;
              tiley = p.tiley;
              posX = tilex * 4;
              posY = tiley * 4;
              
              player.setPositionX(posX + 2.0F);
              player.setPositionY(posY + 2.0F);
              try
              {
                player.setPositionZ(Zones.calculateHeight(posX, posY, true));
              }
              catch (NoSuchZoneException nsz2)
              {
                logger.log(Level.WARNING, player.getName() + " Respawn failed at spawnpoint " + tilex + "," + tiley);
              }
              player.getCommunicator().sendNormalServerMessage("You have been respawned since your position was out of bounds.");
              
              return true;
            }
          }
        }
        putOutsideEnemyDeed(player, load);
        return true;
      }
    }
    return putOutsideEnemyDeed(player, load);
  }
  
  private static final VolaTile getStartTileForDeed(Player player)
  {
    int tilex = player.getTileX();
    int tiley = player.getTileY();
    Village v = Zones.getVillage(tilex, tiley, player.isOnSurface());
    if (v != null) {
      if (v.isEnemy(player, true))
      {
        player.getCommunicator().sendSafeServerMessage("You find yourself outside the " + v.getName() + " settlement.");
        
        int ntx = v.getEndX() + Server.rand.nextInt(10);
        if (Server.rand.nextBoolean()) {
          ntx = v.getStartX() - Server.rand.nextInt(10);
        }
        int nty = v.getEndY() + Server.rand.nextInt(10);
        if (Server.rand.nextBoolean()) {
          nty = v.getStartY() - Server.rand.nextInt(10);
        }
        VolaTile startTile = Zones.getTileOrNull(ntx, nty, player.isOnSurface());
        if (startTile != null)
        {
          Structure struct = startTile.getStructure();
          if ((struct == null) || (!struct.isFinished())) {
            return startTile;
          }
        }
        else
        {
          return Zones.getOrCreateTile(ntx, nty, true);
        }
        ntx = v.getEndX() + Server.rand.nextInt(10);
        if (Server.rand.nextBoolean()) {
          ntx = v.getStartX() - Server.rand.nextInt(10);
        }
        nty = v.getStartY() - 10 + Server.rand.nextInt(v.getEndY() + 20 - v.getStartY());
        startTile = Zones.getTileOrNull(ntx, nty, player.isOnSurface());
        if (startTile != null)
        {
          Structure struct = startTile.getStructure();
          if ((struct == null) || (!struct.isFinished())) {
            return startTile;
          }
        }
        else
        {
          return Zones.getOrCreateTile(ntx, nty, true);
        }
        for (int x = 0; x < 20; x++)
        {
          nty = v.getEndY() + Server.rand.nextInt(10);
          if (Server.rand.nextBoolean()) {
            nty = v.getStartY() - Server.rand.nextInt(10);
          }
          ntx = v.getStartX() - 10 + Server.rand.nextInt(v.getEndX() + 20 - v.getStartX());
          startTile = Zones.getTileOrNull(ntx, nty, player.isOnSurface());
          if (startTile != null)
          {
            Structure struct = startTile.getStructure();
            if ((struct == null) || (!struct.isFinished())) {
              return startTile;
            }
          }
          else
          {
            return Zones.getOrCreateTile(ntx, nty, true);
          }
        }
      }
    }
    return null;
  }
  
  public static boolean putOutsideEnemyDeed(Player player, boolean load)
  {
    if ((load) && (player.getPower() == 0))
    {
      VolaTile startTile = getStartTileForDeed(player);
      if (startTile != null)
      {
        float posX = (startTile.getTileX() << 2) + 2;
        float posY = (startTile.getTileY() << 2) + 2;
        
        MountTransfer mt = MountTransfer.getTransferFor(player.getWurmId());
        if (mt != null) {
          mt.remove(player.getWurmId());
        }
        player.setPositionX(posX);
        player.setPositionY(posY);
        if (player.getVehicle() != -10L) {
          player.disembark(false);
        }
        try
        {
          player.setPositionZ(Zones.calculateHeight(posX, posY, true));
        }
        catch (NoSuchZoneException nsz)
        {
          logger.log(Level.WARNING, player.getName() + " ending up outside map: " + player.getStatus().getPositionX() + ", " + player
            .getStatus().getPositionY());
          player.calculateSpawnPoints();
          if (player.spawnpoints != null)
          {
            Iterator localIterator = player.spawnpoints.iterator();
            if (localIterator.hasNext())
            {
              Spawnpoint p = (Spawnpoint)localIterator.next();
              
              int tilex = p.tilex;
              int tiley = p.tiley;
              posX = tilex * 4;
              posY = tiley * 4;
              
              player.setPositionX(posX + 2.0F);
              player.setPositionY(posY + 2.0F);
              try
              {
                player.setPositionZ(Zones.calculateHeight(posX, posY, true));
              }
              catch (NoSuchZoneException nsz2)
              {
                logger.log(Level.WARNING, player.getName() + " Respawn failed at spawnpoint " + tilex + "," + tiley);
              }
              player.getCommunicator().sendNormalServerMessage("You have been respawned since your position was out of bounds.");
              
              return true;
            }
          }
        }
        return true;
      }
    }
    return false;
  }
  
  public static void putOutsideWall(Player player)
  {
    if (player.getStatus().getLayer() < 0)
    {
      int tilex = player.getTileX();
      int tiley = player.getTileY();
      int tile = Server.caveMesh.getTile(tilex, tiley);
      if (Tiles.isSolidCave(Tiles.decodeType(tile)))
      {
        boolean saved = false;
        int y;
        for (int x = -1; x <= 1; x++) {
          for (y = -1; y <= 1; y++)
          {
            tile = Server.caveMesh.getTile(tilex + x, tiley + y);
            if (!Tiles.isSolidCave(Tiles.decodeType(tile)))
            {
              float posX = (tilex + x) * 4;
              float posY = (tiley + y) * 4;
              
              player.setPositionX(posX + 2.0F);
              player.setPositionY(posY + 2.0F);
              
              saved = true;
              break;
            }
          }
        }
        if (!saved) {
          player.setLayer(0, false);
        }
        try
        {
          player.setPositionZ(Zones.calculateHeight(player.getStatus().getPositionX(), player.getStatus()
            .getPositionY(), player.isOnSurface()));
        }
        catch (NoSuchZoneException nsz)
        {
          logger.log(Level.WARNING, player.getName() + " ending up outside map: " + player.getStatus().getPositionX() + ", " + player
            .getStatus().getPositionY() + ". Respawning.");
          player.calculateSpawnPoints();
          if (player.spawnpoints != null)
          {
            y = player.spawnpoints.iterator();
            if (y.hasNext())
            {
              Spawnpoint p = (Spawnpoint)y.next();
              
              tilex = p.tilex;
              tiley = p.tiley;
              float posX = tilex * 4;
              float posY = tiley * 4;
              
              player.setPositionX(posX + 2.0F);
              player.setPositionY(posY + 2.0F);
              try
              {
                player.setPositionZ(Zones.calculateHeight(posX, posY, true));
              }
              catch (NoSuchZoneException nsz2)
              {
                logger.log(Level.WARNING, player.getName() + " Respawn failed at spawnpoint " + tilex + "," + tiley);
              }
              player.getCommunicator().sendNormalServerMessage("You have been respawned since your position was out of bounds.");
              
              return;
            }
          }
        }
        return;
      }
    }
  }
  
  public static boolean putOutsideFence(Player player)
  {
    boolean moved = true;
    
    int tilex = player.getTileX();
    int tiley = player.getTileY();
    
    float posX = tilex * 4;
    float posY = tiley * 4;
    if (player.getBridgeId() <= 0L)
    {
      posX = posX + 0.5F + Server.rand.nextFloat() * 3.0F;
      posY = posY + 0.5F + Server.rand.nextFloat() * 3.0F;
    }
    else
    {
      posX += 2.0F;
      posY += 2.0F;
    }
    player.setPositionX(posX);
    player.setPositionY(posY);
    if (player.getFloorLevel() <= 0) {
      try
      {
        player.setPositionZ(Zones.calculateHeight(posX, posY, true));
      }
      catch (NoSuchZoneException nsz)
      {
        logger.log(Level.WARNING, player.getName() + " ending up outside map: " + player.getStatus().getPositionX() + ", " + player
        
          .getStatus().getPositionY() + ". Respawning.");
        player.calculateSpawnPoints();
        if (player.spawnpoints != null)
        {
          Iterator localIterator = player.spawnpoints.iterator();
          if (localIterator.hasNext())
          {
            Spawnpoint p = (Spawnpoint)localIterator.next();
            
            tilex = p.tilex;
            tiley = p.tiley;
            posX = tilex * 4;
            posY = tiley * 4;
            
            player.setPositionX(posX + 2.0F);
            player.setPositionY(posY + 2.0F);
            try
            {
              player.setPositionZ(Zones.calculateHeight(posX, posY, true));
            }
            catch (NoSuchZoneException nsz2)
            {
              logger.log(Level.WARNING, player.getName() + " Respawn failed at spawnpoint " + tilex + "," + tiley);
            }
            player.getCommunicator().sendNormalServerMessage("You have been respawned since your position was out of bounds.");
            
            return true;
          }
        }
      }
    }
    return true;
  }
  
  public static final boolean willGoOnBoat(Player player)
  {
    MountTransfer mt = MountTransfer.getTransferFor(player.getWurmId());
    if (mt != null)
    {
      long vehicleId = mt.getVehicleId();
      Vehicle vehic = Vehicles.getVehicleForId(vehicleId);
      if (vehic != null)
      {
        if (!vehic.creature) {
          try
          {
            Item i = Items.getItem(vehicleId);
            if (i.isBoat()) {
              return true;
            }
          }
          catch (Exception ex)
          {
            logger.log(Level.WARNING, "Failed to locate boat with id " + vehicleId + " for player " + player
              .getName(), ex);
          }
        }
        try
        {
          Creatures.getInstance().getCreature(vehicleId);
          return true;
        }
        catch (Exception ex)
        {
          logger.log(Level.WARNING, "Failed to locate creature with id " + vehicleId + " for player " + player
            .getName(), ex);
        }
      }
    }
    return false;
  }
  
  public static final boolean putInBoatAndAssignSeat(Player player, boolean reconnect)
  {
    MountTransfer mt = MountTransfer.getTransferFor(player.getWurmId());
    if ((mt == null) && ((player.getVehicle() == -10L) || (reconnect)))
    {
      long vehicleId = player.getSaveFile().lastvehicle;
      if (reconnect) {
        vehicleId = player.getVehicle();
      }
      Vehicle vehic = Vehicles.getVehicleForId(vehicleId);
      if (vehic != null) {
        try
        {
          Item i = null;
          Creature creature = null;
          int freeseatnum = -1;
          float offz = 0.0F;
          float offx = 0.0F;
          float offy = 0.0F;
          int start = 9999;
          float posx = 50.0F;
          float posy = 50.0F;
          int layer = 0;
          if (WurmId.getType(vehicleId) == 2)
          {
            i = Items.getItem(vehicleId);
            if ((reconnect) || (i.isBoat()))
            {
              posx = i.getPosX();
              posy = i.getPosY();
              layer = i.isOnSurface() ? 0 : -1;
              if (((VehicleBehaviour.hasKeyForVehicle(player, i)) || 
                (VehicleBehaviour.mayDriveVehicle(player, i, null))) && 
                (VehicleBehaviour.canBeDriverOfVehicle(player, vehic))) {
                start = 1;
              } else if ((VehicleBehaviour.hasKeyForVehicle(player, i)) || 
                (VehicleBehaviour.mayEmbarkVehicle(player, i))) {
                start = 1;
              } else {
                logger.log(Level.INFO, player.getName() + " may no longer embark the vehicle " + i.getName());
              }
            }
            else
            {
              return false;
            }
          }
          else if (WurmId.getType(vehicleId) == 1)
          {
            creature = Creatures.getInstance().getCreature(vehicleId);
            posx = creature.getPosX();
            posy = creature.getPosY();
            layer = creature.getLayer();
            if ((VehicleBehaviour.mayDriveVehicle(player, creature)) && 
              (VehicleBehaviour.canBeDriverOfVehicle(player, vehic))) {
              start = 1;
            } else if (VehicleBehaviour.mayEmbarkVehicle(player, creature)) {
              start = 1;
            } else {
              logger.log(Level.INFO, player.getName() + " may no longer mount the " + creature.getName());
            }
          }
          for (int x = 0; x < vehic.seats.length; x++) {
            if (vehic.seats[x].occupant == player.getWurmId())
            {
              freeseatnum = x;
              offz = vehic.seats[x].offz;
              offy += vehic.seats[x].offy;
              offx += vehic.seats[x].offx;
            }
          }
          if (freeseatnum < 0) {
            for (int x = start; x < vehic.seats.length; x++) {
              if (vehic.seats[x].occupant == -10L) {
                if (freeseatnum < 0)
                {
                  freeseatnum = x;
                  offz = vehic.seats[x].offz;
                  offy += vehic.seats[x].offy;
                  offx += vehic.seats[x].offx;
                }
              }
            }
          }
          player.setPositionX(posx + offx);
          player.setPositionY(posy + offy);
          
          VolaTile tile = Zones.getOrCreateTile((int)(player.getPosX() / 4.0F), (int)(player.getPosY() / 4.0F), player
            .getLayer() >= 0);
          boolean skipSetZ = false;
          if (tile != null)
          {
            Structure structure = tile.getStructure();
            if (structure != null) {
              skipSetZ = (structure.isTypeHouse()) || (structure.getWurmId() == player.getBridgeId());
            }
          }
          if (!skipSetZ) {
            player.setPositionZ(Math.max(Zones.calculateHeight(posx + offx, posy + offy, layer >= 0) + offz, freeseatnum >= 0 ? offz : -1.45F));
          }
          if (freeseatnum >= 0)
          {
            mt = new MountTransfer(vehicleId, freeseatnum == 0 ? player.getWurmId() : -10L);
            mt.addToSeat(player.getWurmId(), freeseatnum);
          }
          return true;
        }
        catch (NoSuchItemException nsi)
        {
          logger.log(Level.WARNING, "No item to board for " + player.getName() + ":" + vehicleId, nsi);
        }
        catch (Exception ex)
        {
          logger.log(Level.WARNING, ex.getMessage(), ex);
        }
      }
    }
    return false;
  }
  
  public static final boolean checkPutOnBoat(Player player)
  {
    MountTransfer mt = MountTransfer.getTransferFor(player.getWurmId());
    if (mt != null)
    {
      long vehicleId = mt.getVehicleId();
      Vehicle vehic = Vehicles.getVehicleForId(vehicleId);
      if (vehic != null)
      {
        if (vehic.isChair()) {
          return false;
        }
        try
        {
          Item i = null;
          Creature creature = null;
          if (WurmId.getType(vehicleId) == 2) {
            i = Items.getItem(vehicleId);
          } else if (WurmId.getType(vehicleId) == 1) {
            creature = Creatures.getInstance().getCreature(vehicleId);
          }
          int seatnum = mt.getSeatFor(player.getWurmId());
          if (seatnum >= 0)
          {
            vehic.seats[seatnum].occupant = player.getWurmId();
            if (mt.getPilotId() == player.getWurmId())
            {
              vehic.pilotId = player.getWurmId();
              player.setVehicleCommander(true);
            }
            MountAction m = new MountAction(creature, i, vehic, seatnum, mt.getPilotId() == player.getWurmId(), vehic.seats[seatnum].offz);
            
            player.setMountAction(m);
            
            player.setVehicle(vehicleId, false, vehic.seats[seatnum].getType());
            return true;
          }
        }
        catch (NoSuchItemException nsi)
        {
          logger.log(Level.WARNING, "No item to board for " + player.getName() + ":" + vehicleId, nsi);
        }
        catch (NoSuchCreatureException nsc)
        {
          logger.log(Level.WARNING, "No creature to mount for " + player.getName() + ":" + vehicleId, nsc);
        }
        catch (Exception ex)
        {
          logger.log(Level.WARNING, ex.getMessage(), ex);
        }
      }
    }
    return false;
  }
  
  public static void sendWho(Player player, boolean loggingin)
  {
    if (player.isUndead()) {
      return;
    }
    String[] names = Players.getInstance().getPlayerNames();
    String playerList = "none!";
    Communicator comm = player.getCommunicator();
    int otherServers = 0;
    String localServerName = Servers.localServer.name;
    if (localServerName.length() > 1)
    {
      localServerName = localServerName.toLowerCase();
      localServerName = Character.toUpperCase(localServerName.charAt(0)) + localServerName.substring(1);
    }
    int epic = 0;
    for (ServerEntry entry : Servers.getAllServers()) {
      if (!entry.EPIC)
      {
        if (!entry.isLocal) {
          otherServers += entry.currentPlayers;
        }
      }
      else {
        epic += entry.currentPlayers;
      }
    }
    if (player.getPower() > 0)
    {
      comm.sendSafeServerMessage("These other players are online on " + localServerName + ":");
      int nums = names.length;
      if (names.length > 1)
      {
        playerList = "";
        for (int x = 0; x < names.length; x++)
        {
          if (!names[x].equals(player.getName()))
          {
            PlayerInfo p = PlayerInfoFactory.createPlayerInfo(names[x]);
            if (player.getPower() >= p.getPower()) {
              playerList = playerList + names[x] + " ";
            } else {
              nums--;
            }
          }
          if ((x != 0) && (x % 10 == 0))
          {
            comm.sendSafeServerMessage(playerList);
            playerList = "";
          }
        }
        if (playerList.length() > 0) {
          comm.sendSafeServerMessage(playerList);
        }
        String ss = "";
        if (names.length > 1) {
          ss = "s";
        }
        comm.sendSafeServerMessage(nums + " player" + ss + " on this server. (" + (nums + otherServers + epic) + " totally in Wurm)");
      }
      else
      {
        comm.sendSafeServerMessage("none! (" + (nums + otherServers + epic) + " totally in Wurm)");
      }
    }
    else if (names.length > 1)
    {
      comm.sendSafeServerMessage(names.length - 1 + " other players are online. You are on " + localServerName + " (" + (names.length + otherServers + epic) + " totally in Wurm).");
    }
    else
    {
      comm.sendSafeServerMessage("No other players are online on " + localServerName + " (" + (1 + otherServers) + " totally in Wurm).");
    }
  }
  
  private static void sendLoggedInPeople(Player player)
  {
    if (player.isUndead()) {
      return;
    }
    if (player.isSignedIn()) {
      player.getCommunicator().signIn("Just transferred.");
    } else if ((player.canSignIn()) && (player.getPower() <= 1)) {
      player.getCommunicator().remindToSignIn();
    }
    sendWho(player, true);
    Village vill = player.getCitizenVillage();
    if (vill != null) {
      vill.sendCitizensToPlayer(player);
    }
    if ((player.mayHearMgmtTalk()) || (player.mayHearDevTalk())) {
      Players.getInstance().sendGmsToPlayer(player);
    }
    if (player.seesPlayerAssistantWindow()) {
      Players.getInstance().sendPAWindow(player);
    }
    if ((player.seesGVHelpWindow()) && (!Servers.isThisLoginServer())) {
      Players.getInstance().sendGVHelpWindow(player);
    }
    Players.getInstance().sendAltarsToPlayer(player);
    Players.getInstance().sendTicketsToPlayer(player);
    player.checkKingdom();
    if (player.isGlobalChat()) {
      Players.getInstance().sendStartGlobalKingdomChat(player);
    }
    if (player.isKingdomChat()) {
      Players.getInstance().sendStartKingdomChat(player);
    }
    if (player.isTradeChannel()) {
      Players.getInstance().sendStartGlobalTradeChannel(player);
    }
  }
  
  private static void sendStatus(Player player)
  {
    player.getStatus().sendHunger();
    player.getStatus().sendThirst();
    player.getStatus().lastSentStamina = 65336;
    player.getStatus().sendStamina();
    player.sendDeityEffectBonuses();
    player.getCommunicator().sendOwnTitles();
    player.getCommunicator().sendSleepInfo();
    player.sendAllPoisonEffect();
    Abilities.sendEffectsToCreature(player);
  }
  
  private void sendLoginAnswer(boolean ok, String message, float x, float y, float z, float rot, int layer, String bodyName, byte power, int retrySeconds)
    throws IOException
  {
    sendLoginAnswer(ok, message, x, y, z, rot, layer, bodyName, power, retrySeconds, (byte)0, (byte)0, 0L, 0, (byte)0, -10L, 0.0F);
  }
  
  public void sendLoginAnswer(boolean ok, String message, float x, float y, float z, float rot, int layer, String bodyName, byte power, int retrySeconds, byte commandType, byte templateKingdomId, long face, int teleportCounter, byte blood, long bridgeId, float groundOffset)
    throws IOException
  {
    try
    {
      if (Constants.useQueueToSendDataToPlayers) {}
      byte[] messageb = message.getBytes("UTF-8");
      ByteBuffer bb = this.conn.getBuffer();
      
      bb.put((byte)-15);
      if (ok) {
        bb.put((byte)1);
      } else {
        bb.put((byte)0);
      }
      bb.putShort((short)messageb.length);
      bb.put(messageb);
      
      bb.put((byte)layer);
      bb.putLong(WurmCalendar.currentTime);
      bb.putLong(System.currentTimeMillis());
      bb.putFloat(rot);
      bb.putFloat(x);
      bb.putFloat(y);
      bb.putFloat(z);
      byte[] bodyb = bodyName.getBytes("UTF-8");
      bb.putShort((short)bodyb.length);
      bb.put(bodyb);
      if (power == 0) {
        bb.put((byte)0);
      } else if (power == 1) {
        bb.put((byte)2);
      } else {
        bb.put((byte)1);
      }
      bb.put(commandType);
      bb.putShort((short)retrySeconds);
      
      bb.putLong(face);
      bb.put(templateKingdomId);
      bb.putInt(teleportCounter);
      bb.put(blood);
      bb.putLong(bridgeId);
      bb.putFloat(groundOffset);
      bb.putInt(Zones.worldTileSizeX);
      this.conn.flush();
    }
    catch (IOException ioe)
    {
      throw ioe;
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, "Failed to send login answer.", ex);
    }
  }
  
  public void sendAuthenticationAnswer(boolean wasSucces, String failedMessage)
  {
    ByteBuffer bb = this.conn.getBuffer();
    bb.put((byte)-52);
    if (wasSucces) {
      bb.put((byte)1);
    } else {
      bb.put((byte)0);
    }
    try
    {
      byte[] failedMessageb = failedMessage.getBytes("UTF-8");
      bb.putShort((short)failedMessageb.length);
      bb.put(failedMessageb);
    }
    catch (Exception e)
    {
      bb.putShort((short)0);
    }
    try
    {
      this.conn.flush();
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, "Failed to send Auth answer.", ex);
    }
  }
  
  public static byte[] createAndReturnPlayer(String name, String password, String pwQuestion, String pwAnswer, String email, byte kingdom, byte power, long appearance, byte gender, boolean titleKeeper, boolean addPremium, boolean passwordIsHashed)
    throws Exception
  {
    return createAndReturnPlayer(name, password, pwQuestion, pwAnswer, email, kingdom, power, appearance, gender, titleKeeper, addPremium, passwordIsHashed, -10L);
  }
  
  public static byte[] createAndReturnPlayer(String name, String password, String pwQuestion, String pwAnswer, String email, byte kingdom, byte power, long appearance, byte gender, boolean titleKeeper, boolean addPremium, boolean passwordIsHashed, long wurmId)
    throws Exception
  {
    if ((Servers.localServer.HOMESERVER) && (Servers.localServer.KINGDOM != kingdom)) {
      throw new WurmServerException("Illegal kingdom");
    }
    name = raiseFirstLetter(name);
    if (!passwordIsHashed) {
      try
      {
        password = hashPassword(password, encrypt(name));
      }
      catch (Exception ex)
      {
        throw new WurmServerException("We failed to encrypt your password. Please try another.");
      }
    }
    if (wurmId < 0L)
    {
      String result = checkName2(name);
      if (result.length() > 0) {
        throw new WurmServerException(result);
      }
      if (Players.getInstance().getWurmIdByPlayerName(name) != -1L) {
        throw new WurmServerException("That name is taken.");
      }
    }
    Player player = Player.doNewPlayer(1);
    player.setName(name);
    int startx = Servers.localServer.SPAWNPOINTJENNX;
    int starty = Servers.localServer.SPAWNPOINTJENNY;
    Spawnpoint spawn = getInitialSpawnPoint(kingdom);
    if (spawn != null)
    {
      startx = spawn.tilex;
      starty = spawn.tiley;
    }
    else if (kingdom == 3)
    {
      if (Servers.localServer.SPAWNPOINTLIBX > 0)
      {
        startx = Servers.localServer.SPAWNPOINTLIBX;
        starty = Servers.localServer.SPAWNPOINTLIBY;
      }
    }
    else if (kingdom == 2)
    {
      if (Servers.localServer.SPAWNPOINTMOLX > 0)
      {
        startx = Servers.localServer.SPAWNPOINTMOLX;
        starty = Servers.localServer.SPAWNPOINTMOLY;
      }
    }
    if (Servers.localServer.id == 5)
    {
      startx = 2884;
      starty = 3004;
    }
    float posX = startx * 4 + Server.rand.nextFloat() * 2.0F * 4.0F - 4.0F;
    float posY = starty * 4 + Server.rand.nextFloat() * 2.0F * 4.0F - 4.0F;
    
    float rot = Server.rand.nextInt(45) - 22.5F;
    if (wurmId < 0L) {
      player.setWurmId(WurmId.getNextPlayerId(), posX, posY, rot, 0);
    } else {
      player.setWurmId(wurmId, posX, posY, rot, 0);
    }
    putOutsideWall(player);
    if (player.isOnSurface())
    {
      putOutsideHouse(player, false);
      putOutsideFence(player);
    }
    player.setNewPlayer(true);
    
    PlayerInfo file = PlayerInfoFactory.createPlayerInfo(name);
    file.initialize(name, player.getWurmId(), password, pwQuestion, pwAnswer, appearance, false);
    player.getStatus().setStatusExists(true);
    file.setEmailAddress(email);
    file.loaded = true;
    player.setSaveFile(file);
    file.togglePlayerAssistantWindow(true);
    
    player.loadSkills();
    
    player.getBody().createBodyParts();
    player.createPossessions();
    player.createSomeItems(1.0F, false);
    player.setPower(power);
    checkReimbursement(player);
    player.setSex(gender);
    player.getStatus().setKingdom(kingdom);
    player.setFlag(53, true);
    player.setFlag(76, true);
    
    Players.loadAllPrivatePOIForPlayer(player);
    player.sendAllMapAnnotations();
    ValreiMapData.sendAllMapData(player);
    
    player.setFullyLoaded();
    if (power > 0) {
      file.setReimbursed(false);
    }
    if (titleKeeper) {
      if (kingdom == 3) {
        player.addTitle(Titles.Title.Destroyer_Faith);
      } else {
        player.addTitle(Titles.Title.Keeper_Faith);
      }
    }
    if (addPremium) {
      file.setPaymentExpire(System.currentTimeMillis());
    }
    player.sleep();
    PlayerInfoFactory.addPlayerInfo(file);
    Server.addNewbie();
    return PlayerTransfer.createPlayerData(Wounds.emptyWounds, player.getSaveFile(), player.getStatus(), player
      .getAllItems(), player.getSkills().getSkillsNoTemp(), null, Servers.localServer.id, 0L, kingdom);
  }
  
  public static final Spawnpoint getInitialSpawnPoint(byte kingdom)
  {
    if ((!Servers.localServer.entryServer) || (Server.getInstance().isPS()))
    {
      Village[] villages = Villages.getPermanentVillages(kingdom);
      if (villages.length > 0)
      {
        Village chosen = villages[Server.rand.nextInt(villages.length)];
        return new Spawnpoint(chosen.getName(), (byte)1, chosen.getMotto(), 
          (short)chosen.getTokenX(), (short)chosen.getTokenY(), true, chosen.kingdom);
      }
    }
    return null;
  }
  
  public static long createPlayer(String name, String password, String pwQuestion, String pwAnswer, String email, byte kingdom, byte power, long appearance, byte gender)
    throws Exception
  {
    try
    {
      password = hashPassword(password, encrypt(raiseFirstLetter(name)));
    }
    catch (Exception ex)
    {
      throw new WurmServerException("We failed to encrypt your password. Please try another.");
    }
    return createPlayer(name, password, pwQuestion, pwAnswer, email, kingdom, power, appearance, gender, false, false, -10L);
  }
  
  public static long createPlayer(String name, String hashedPassword, String pwQuestion, String pwAnswer, String email, byte kingdom, byte power, long appearance, byte gender, boolean titleKeeper, boolean addPremium, long wurmId)
    throws Exception
  {
    if ((Servers.localServer.HOMESERVER) && 
      (Servers.localServer.KINGDOM != kingdom)) {
      kingdom = Servers.localServer.KINGDOM;
    }
    name = raiseFirstLetter(name);
    if (wurmId < 0L)
    {
      String result = checkName2(name);
      if (result.length() > 0) {
        throw new WurmServerException(result);
      }
      if (Players.getInstance().getWurmIdByPlayerName(name) != -1L) {
        throw new WurmServerException("That name is taken.");
      }
    }
    Player player = Player.doNewPlayer(1);
    
    player.setName(name);
    
    int startx = Servers.localServer.SPAWNPOINTJENNX;
    int starty = Servers.localServer.SPAWNPOINTJENNY;
    Spawnpoint spawn = getInitialSpawnPoint(kingdom);
    if (spawn != null)
    {
      startx = spawn.tilex;
      starty = spawn.tiley;
    }
    else if (kingdom == 3)
    {
      if (Servers.localServer.SPAWNPOINTLIBX > 0)
      {
        startx = Servers.localServer.SPAWNPOINTLIBX;
        starty = Servers.localServer.SPAWNPOINTLIBY;
      }
    }
    else if (kingdom == 2)
    {
      if (Servers.localServer.SPAWNPOINTMOLX > 0)
      {
        startx = Servers.localServer.SPAWNPOINTMOLX;
        starty = Servers.localServer.SPAWNPOINTMOLY;
      }
    }
    float posX = startx * 4 + Server.rand.nextFloat() * 2.0F * 4.0F - 4.0F;
    float posY = starty * 4 + Server.rand.nextFloat() * 2.0F * 4.0F - 4.0F;
    
    float rot = 4.0F;
    if (wurmId < 0L) {
      player.setWurmId(WurmId.getNextPlayerId(), posX, posY, 4.0F, 0);
    } else {
      player.setWurmId(wurmId, posX, posY, 4.0F, 0);
    }
    Players.getInstance().addPlayer(player);
    
    player.setNewPlayer(true);
    
    PlayerInfo file = PlayerInfoFactory.createPlayerInfo(name);
    file.initialize(name, player.getWurmId(), hashedPassword, pwQuestion, pwAnswer, appearance, false);
    player.getStatus().setStatusExists(true);
    file.loaded = true;
    player.setSaveFile(file);
    file.togglePlayerAssistantWindow(true);
    file.setEmailAddress(email);
    putOutsideWall(player);
    if (player.isOnSurface())
    {
      putOutsideHouse(player, false);
      putOutsideFence(player);
    }
    player.loadSkills();
    
    player.createPossessions();
    
    player.getBody().createBodyParts();
    player.createSomeItems(1.0F, false);
    player.setPower(power);
    checkReimbursement(player);
    player.setSex(gender);
    player.getStatus().setKingdom(kingdom);
    player.setFlag(53, true);
    player.setFlag(76, true);
    
    Players.loadAllPrivatePOIForPlayer(player);
    player.sendAllMapAnnotations();
    ValreiMapData.sendAllMapData(player);
    
    Kingdom k = Kingdoms.getKingdom(kingdom);
    float nposX;
    int rand;
    int current;
    if (k != null) {
      if (k.isCustomKingdom())
      {
        player.calculateSpawnPoints();
        Set<Spawnpoint> spawns = player.spawnpoints;
        if (spawns != null) {
          for (Spawnpoint sp : spawns) {
            if ((sp.tilex > 20) && (sp.tilex < Zones.worldTileSizeX - 20) && 
              (sp.tiley > 20) && (sp.tiley < Zones.worldTileSizeY - 20))
            {
              nposX = sp.tilex * 4 + 1 + Server.rand.nextFloat() * 2.0F;
              float nposY = sp.tiley * 4 + 1 + Server.rand.nextFloat() * 2.0F;
              player.getStatus().setPositionXYZ(nposX, nposY, Zones.calculateHeight(nposX, nposY, true));
              break;
            }
          }
        }
      }
      else
      {
        if (kingdom == 3)
        {
          player.setDeity(Deities.getDeity(4));
          player.setFaith(1.0F);
        }
        if (Servers.localServer.entryServer) {
          if (Players.getInstance().getNumberOfPlayers() > 100)
          {
            player.calculateSpawnPoints();
            Set<Spawnpoint> spawns = player.spawnpoints;
            if (spawns != null) {
              if (spawns.size() > 0)
              {
                rand = Server.rand.nextInt(spawns.size());
                current = 0;
                for (Spawnpoint sp : spawns)
                {
                  if (rand == current)
                  {
                    startx = sp.tilex;
                    starty = sp.tiley;
                    float posNX = startx * 4 + Server.rand.nextFloat() * 2.0F * 4.0F - 4.0F;
                    float posNY = starty * 4 + Server.rand.nextFloat() * 2.0F * 4.0F - 4.0F;
                    player.getStatus().getPosition().setPosX(posNX);
                    player.getStatus().getPosition().setPosY(posNY);
                    player.updateEffects();
                    break;
                  }
                  current++;
                }
              }
            }
          }
        }
      }
    }
    player.setFullyLoaded();
    if (power > 0) {
      file.setReimbursed(false);
    }
    if (titleKeeper) {
      if (kingdom == 3) {
        player.addTitle(Titles.Title.Destroyer_Faith);
      } else {
        player.addTitle(Titles.Title.Keeper_Faith);
      }
    }
    if (addPremium) {
      file.setPaymentExpire(System.currentTimeMillis());
    }
    player.sleep();
    PlayerInfoFactory.addPlayerInfo(file);
    Server.addNewbie();
    Players.getInstance().removePlayer(player);
    return player.getWurmId();
  }
  
  private boolean checkName(String name)
  {
    String result = checkName2(name);
    if (result.length() > 0)
    {
      try
      {
        sendLoginAnswer(false, result, 0.0F, 0.0F, 0.0F, 0.0F, 0, "model.player.broken", (byte)0, 0);
      }
      catch (IOException ioe)
      {
        if (logger.isLoggable(Level.FINE)) {
          logger.log(Level.FINE, this.conn.getIp() + ", problem sending login denied message: " + result, ioe);
        }
      }
      return false;
    }
    return true;
  }
  
  private static void sendAllItemModelNames(Player player)
  {
    for (ItemTemplate item : ItemTemplateFactory.getInstance().getTemplates()) {
      if (!item.isNoTake()) {
        player.getCommunicator().sendItemTemplateList(item.getTemplateId(), item.getModelName());
      }
    }
  }
  
  private static void sendAllEquippedArmor(Player player)
  {
    for (Item item : player.getBody().getContainersAndWornItems()) {
      if (item != null) {
        try
        {
          byte armorSlot = item.isArmour() ? BodyTemplate.convertToArmorEquipementSlot((byte)item.getParent().getPlace()) : BodyTemplate.convertToItemEquipementSlot((byte)item.getParent().getPlace());
          
          player.getCommunicator().sendWearItem(-1L, item.getTemplateId(), armorSlot, 
            WurmColor.getColorRed(item.getColor()), WurmColor.getColorGreen(item.getColor()), 
            WurmColor.getColorBlue(item.getColor()), 
            WurmColor.getColorRed(item.getColor2()), WurmColor.getColorGreen(item.getColor2()), 
            WurmColor.getColorBlue(item.getColor2()), item.getMaterial(), item
            .getRarity());
        }
        catch (Exception localException) {}
      }
    }
  }
  
  public static final String checkName2(String name)
  {
    boolean notok = containsIllegalCharacters(name);
    if (notok) {
      return "Please use only letters from a to z in your name.";
    }
    if (name.length() < 3) {
      return "Please use a name at least 3 letters long.";
    }
    if (name.length() > 40) {
      return "Please use a name no longer than 40 letters.";
    }
    if ((!Deities.isNameOkay(name)) || (!CreatureTemplateFactory.isNameOkay(name))) {
      return "Illegal name.";
    }
    return "";
  }
  
  public static String hashPassword(String password, String salt)
    throws NoSuchAlgorithmException, InvalidKeySpecException
  {
    char[] passwordChars = password.toCharArray();
    byte[] saltBytes = salt.getBytes();
    
    PBEKeySpec spec = new PBEKeySpec(passwordChars, saltBytes, 1000, 192);
    
    SecretKeyFactory key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
    byte[] hashedPassword = key.generateSecret(spec).getEncoded();
    return String.format("%x", new Object[] { new BigInteger(hashedPassword) });
  }
  
  public static String encrypt(String plaintext)
    throws Exception
  {
    MessageDigest md = null;
    try
    {
      md = MessageDigest.getInstance("SHA");
    }
    catch (NoSuchAlgorithmException e)
    {
      throw new WurmServerException("No such algorithm 'SHA'", e);
    }
    try
    {
      md.update(plaintext.getBytes("UTF-8"));
    }
    catch (UnsupportedEncodingException e)
    {
      throw new WurmServerException("No such encoding: UTF-8", e);
    }
    byte[] raw = md.digest();
    String hash = new BASE64Encoder().encode(raw);
    return hash;
  }
  
  public String getConnectionIp()
  {
    if (this.conn != null) {
      return this.conn.getIp();
    }
    return "";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\LoginHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */