package com.wurmonline.server.creatures.ai;

import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.Message;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.endgames.EndGameItems;
import com.wurmonline.server.epic.EpicMission;
import com.wurmonline.server.epic.EpicServerStatus;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class ChatManager
  implements TimeConstants
{
  private static final Logger logger = Logger.getLogger(ChatManager.class.getName());
  final Creature owner;
  final LinkedList<String> mychats = new LinkedList();
  final ConcurrentHashMap<String, String> localchats = new ConcurrentHashMap();
  final ConcurrentHashMap<Message, String> unansweredLChats = new ConcurrentHashMap();
  final ConcurrentHashMap<String, String> receivedchats = new ConcurrentHashMap();
  final ConcurrentHashMap<String, String> unansweredChats = new ConcurrentHashMap();
  final HashSet<String> localChats = new HashSet();
  int chatPoller = 0;
  long lastChattedLocal = 0L;
  long lastPCChattedLocal = 0L;
  int lastTx = 0;
  int lastTy = 0;
  int chattiness = 1;
  
  public ChatManager(Creature _owner)
  {
    this.owner = _owner;
    this.chattiness = Math.max(1, (int)(this.owner.getWurmId() % 10L));
  }
  
  public void addChat(String chatter, String message)
  {
    this.unansweredChats.put(message, chatter);
  }
  
  public void checkChats()
  {
    if (this.chatPoller > 0)
    {
      this.chatPoller -= 1;
      if (this.chatPoller % 10 == 0)
      {
        pollLocal();
        startLocalChat();
      }
      if (this.chatPoller % 8 == 0) {
        if (this.unansweredChats.size() > 0)
        {
          int answered = 0;
          for (Map.Entry<String, String> entry : this.unansweredChats.entrySet())
          {
            this.receivedchats.put(entry.getKey(), entry.getValue());
            try
            {
              Player p = Players.getInstance().getPlayer((String)entry.getValue());
              if ((answered < 2) || (Server.rand.nextBoolean())) {
                createAndSendMessage(p, getAnswerToMessage((String)entry.getValue(), ((String)entry.getKey()).replace(".", "")), Server.rand.nextInt(10) == 0);
              }
            }
            catch (NoSuchPlayerException nsp)
            {
              logger.log(Level.INFO, nsp.getMessage());
            }
            answered++;
          }
          this.unansweredChats.clear();
        }
      }
      return;
    }
    this.chatPoller = 25;
  }
  
  public final void createAndSendMessage(Player receiver, String message, boolean emote)
  {
    if (!this.mychats.contains(message)) {
      this.mychats.add(message.toLowerCase().replace(".", "").trim());
    }
    receiver.showPM(this.owner.getName(), this.owner.getName(), message, emote);
  }
  
  public final String[] getReceivedChatsAsArr()
  {
    return (String[])this.receivedchats.keySet().toArray(new String[this.receivedchats.size()]);
  }
  
  public final String[] getLocalChatsAsArr()
  {
    return (String[])this.localchats.keySet().toArray(new String[this.localchats.size()]);
  }
  
  public final String getAnswerToMessage(String receiver, String message)
  {
    int rand = Server.rand.nextInt(100);
    int next = Server.rand.nextInt(100);
    boolean toMe = message.toLowerCase().contains(this.owner.getName().toLowerCase());
    String emptyOrReceiver = "";
    if (toMe) {
      emptyOrReceiver = LoginHandler.raiseFirstLetter(receiver) + ", ";
    }
    if (message.endsWith("?"))
    {
      message = message.replace("?", "");
      if ((rand < 10) || (message.toLowerCase().contains("where")))
      {
        if (rand < 5) {
          return emptyOrReceiver + "I heard there's one Key of the Heavens hidden somewhere.";
        }
        String loc = "north";
        if (next < 10) {
          loc = "west";
        } else if (next < 20) {
          loc = "east";
        } else if (next < 30) {
          loc = "south";
        } else if (next < 40) {
          loc = "water";
        } else if (next < 50) {
          loc = "sky";
        } else if (next < 60) {
          loc = "fire";
        } else if (next < 70) {
          loc = "darkest spot";
        } else if (next < 80) {
          loc = "eye of the beholder";
        } else if (next < 90) {
          loc = "back of the room";
        }
        return emptyOrReceiver + "In the " + loc + " as far as I know.";
      }
      if (rand < 20)
      {
        if ((this.localchats.size() > 3) && (Server.rand.nextBoolean()))
        {
          String[] chats = getLocalChatsAsArr();
          String garbled = chats[Server.rand.nextInt(chats.length)];
          StringTokenizer st = new StringTokenizer(garbled);
          String s = st.nextToken();
          while (st.hasMoreTokens())
          {
            String a = st.nextToken();
            if (Server.rand.nextBoolean()) {
              s = a;
            }
          }
          return emptyOrReceiver + "Look for " + s + ".";
        }
        if (rand < 15) {
          return emptyOrReceiver + "The gods may grant you a Key of the Heavens if you do their missions.";
        }
        return "No" + (Server.rand.nextBoolean() ? "!" : ".");
      }
      if (rand < 30)
      {
        if (this.mychats.size() > 0) {
          return emptyOrReceiver + "I just said " + (String)this.mychats.get(Server.rand.nextInt(this.mychats.size())) + ", didn't I?";
        }
        return "I think so" + (Server.rand.nextBoolean() ? "!" : ".");
      }
      String garbled;
      StringTokenizer st;
      String s;
      if (rand < 40)
      {
        if ((this.localchats.size() > 3) && (Server.rand.nextBoolean()))
        {
          String[] chats = getLocalChatsAsArr();
          garbled = chats[Server.rand.nextInt(chats.length)];
          st = new StringTokenizer(garbled);
          s = st.nextToken();
          while (st.hasMoreTokens())
          {
            String a = st.nextToken();
            if (Server.rand.nextBoolean()) {
              s = a;
            }
          }
          return "Would this help you: " + s + ".";
        }
        return "Yes" + (Server.rand.nextBoolean() ? "!" : ".");
      }
      if ((rand < 50) || (message.toLowerCase().contains("who")))
      {
        String loc = "The Forest Giant";
        if (next < 10)
        {
          loc = "You";
        }
        else if (next < 20)
        {
          loc = "Brightberry";
        }
        else if (next < 30)
        {
          loc = "Ceyer";
        }
        else if (next < 40)
        {
          loc = "Fo";
        }
        else if (next < 50)
        {
          loc = "Libila";
        }
        else if (next < 60)
        {
          loc = "Magranon";
        }
        else if (next < 70)
        {
          loc = "The Unknown One";
          garbled = Deities.getDeities();st = garbled.length;
          for (s = 0; s < st; s++)
          {
            Deity d = garbled[s];
            if ((d.isCustomDeity()) && (Server.rand.nextBoolean()))
            {
              loc = d.getName();
              break;
            }
          }
        }
        else if (next < 80)
        {
          loc = "Uttacha";
        }
        else if (next < 90)
        {
          loc = "The Deathcrawler";
        }
        return emptyOrReceiver + loc + " as far as I know.";
      }
      if (rand < 60)
      {
        if (this.receivedchats.size() > 3)
        {
          String[] chats = getReceivedChatsAsArr();
          String garbled = chats[Server.rand.nextInt(chats.length)];
          StringTokenizer st = new StringTokenizer(garbled);
          String s = st.nextToken().toLowerCase();
          while (st.hasMoreTokens())
          {
            String a = st.nextToken();
            if (Server.rand.nextBoolean()) {
              s = a;
            }
          }
          return "Well, someone secretely told me something about " + s + " before.";
        }
        return emptyOrReceiver + "Who?";
      }
      if (rand < 70)
      {
        if (this.receivedchats.size() > 3)
        {
          String[] chats = getReceivedChatsAsArr();
          String garbled = chats[Server.rand.nextInt(chats.length)];
          StringTokenizer st = new StringTokenizer(garbled);
          String s = st.nextToken().toLowerCase();
          while (st.hasMoreTokens())
          {
            String a = st.nextToken();
            if (Server.rand.nextBoolean()) {
              s = a;
            }
          }
          return "All I know is that " + s + ".";
        }
        return emptyOrReceiver + "I thought I answered that already?";
      }
      if (rand < 80)
      {
        StringTokenizer st = new StringTokenizer(message);
        String s = st.nextToken().toLowerCase();
        while (st.hasMoreTokens())
        {
          String a = st.nextToken();
          if (Server.rand.nextBoolean()) {
            s = a;
          }
        }
        return emptyOrReceiver + "Can you explain what you mean by " + s + "?";
      }
      if (rand < 90)
      {
        StringTokenizer st = new StringTokenizer(message);
        String s = st.nextToken().toLowerCase();
        while (st.hasMoreTokens())
        {
          String a = st.nextToken();
          if (Server.rand.nextBoolean()) {
            s = s + " " + a;
          }
        }
        return "I would never agree on " + s + ".";
      }
      StringTokenizer st = new StringTokenizer(message);
      String s = st.nextToken().toLowerCase();
      while (st.hasMoreTokens()) {
        if (Server.rand.nextBoolean()) {
          s = s + " " + st.nextToken();
        }
      }
      return emptyOrReceiver + "Have you heard about " + s + "?";
    }
    String s;
    String a;
    if (message.endsWith("!"))
    {
      message = message.replace("!", "");
      if ((rand < 10) || (message.toLowerCase().contains("what")))
      {
        if (this.localchats.size() > 3)
        {
          String[] chats = getLocalChatsAsArr();
          String garbled = chats[Server.rand.nextInt(chats.length)];
          StringTokenizer st = new StringTokenizer(garbled);
          String s = st.nextToken().toLowerCase();
          while (st.hasMoreTokens())
          {
            String a = st.nextToken();
            if (Server.rand.nextBoolean()) {
              s = a;
            }
          }
          return "Well, as long as people declare things about " + s + " everything goes I guess.";
        }
        return "Isn't it?";
      }
      if ((rand < 20) || (message.toLowerCase().contains("how")))
      {
        if (this.localchats.size() > 3)
        {
          String[] chats = getLocalChatsAsArr();
          String garbled = chats[Server.rand.nextInt(chats.length)];
          StringTokenizer st = new StringTokenizer(garbled);
          String s = st.nextToken().toLowerCase();
          while (st.hasMoreTokens())
          {
            String a = st.nextToken();
            if (Server.rand.nextBoolean()) {
              s = a;
            }
          }
          String name = (String)this.localchats.get(garbled);
          if (name != null) {
            return "Didn't " + name + " say something related like " + s + "?";
          }
          return "That may be related to " + s + ".";
        }
        return "Anything is possible.";
      }
      if (rand < 30)
      {
        if (this.mychats.size() > 0) {
          return "I always claim that " + (String)this.mychats.get(Server.rand.nextInt(this.mychats.size())) + " too!";
        }
        return emptyOrReceiver + "I can only agree.";
      }
      String garbled;
      StringTokenizer st;
      String s;
      if (rand < 40)
      {
        if ((this.receivedchats.size() > 3) && (Server.rand.nextBoolean()))
        {
          String[] chats = getReceivedChatsAsArr();
          garbled = chats[Server.rand.nextInt(chats.length)];
          st = new StringTokenizer(garbled);
          s = st.nextToken().toLowerCase();
          while (st.hasMoreTokens())
          {
            String a = st.nextToken();
            if (Server.rand.nextBoolean()) {
              s = a;
            }
          }
          return "Like " + s + " you mean?";
        }
        return emptyOrReceiver + "Yes!";
      }
      if ((rand < 50) || (message.toLowerCase().contains("who")))
      {
        String loc = "The Forest Giant";
        if (next < 10)
        {
          loc = "You";
        }
        else if (next < 20)
        {
          loc = "Brightberry";
        }
        else if (next < 30)
        {
          loc = "Ceyer";
        }
        else if (next < 40)
        {
          loc = "Fo";
        }
        else if (next < 50)
        {
          loc = "Libila";
        }
        else if (next < 60)
        {
          loc = "Magranon";
        }
        else if (next < 70)
        {
          loc = "Vynora";
          garbled = Deities.getDeities();st = garbled.length;
          for (s = 0; s < st; s++)
          {
            Deity d = garbled[s];
            if ((d.isCustomDeity()) && (Server.rand.nextBoolean()))
            {
              loc = d.getName();
              break;
            }
          }
        }
        else if (next < 80)
        {
          loc = "Uttacha";
        }
        else if (next < 90)
        {
          loc = "The Deathcrawler";
        }
        return "I tend to say " + loc + ".";
      }
      if (rand < 60)
      {
        if ((this.localchats.size() > 3) && (Server.rand.nextBoolean()))
        {
          String[] chats = getLocalChatsAsArr();
          String garbled = chats[Server.rand.nextInt(chats.length)];
          StringTokenizer st = new StringTokenizer(garbled);
          String s = st.nextToken().toLowerCase();
          while (st.hasMoreTokens())
          {
            String a = st.nextToken();
            if (Server.rand.nextBoolean()) {
              s = a;
            }
          }
          String name = (String)this.localchats.get(garbled);
          if (name != null) {
            return "Not unless " + s + ", which I think " + name + " mentioned.";
          }
          return "Not unless " + s + ".";
        }
        return emptyOrReceiver + "What?";
      }
      if (rand < 70)
      {
        if ((this.localchats.size() > 3) && (Server.rand.nextBoolean()))
        {
          String[] chats = getLocalChatsAsArr();
          String garbled = chats[Server.rand.nextInt(chats.length)];
          StringTokenizer st = new StringTokenizer(garbled);
          s = st.nextToken().toLowerCase();
          while (st.hasMoreTokens())
          {
            String a = st.nextToken();
            if (Server.rand.nextBoolean()) {
              s = a;
            }
          }
          return "Look for " + s + ".";
        }
        StringBuilder sb = new StringBuilder("Ha");
        for (int x = 0; x < Server.rand.nextInt(10); x++) {
          sb.append("ha");
        }
        return "!";
      }
      if (rand < 80)
      {
        StringTokenizer st = new StringTokenizer(message);
        String s = st.nextToken().toLowerCase();
        while (st.hasMoreTokens())
        {
          String a = st.nextToken();
          if (Server.rand.nextBoolean()) {
            s = a;
          }
        }
        return emptyOrReceiver + "If " + s + " isn't good enough for you I don't know what is.";
      }
      if (rand < 90)
      {
        StringTokenizer st = new StringTokenizer(message);
        String s = st.nextToken().toLowerCase();
        while (st.hasMoreTokens())
        {
          String a = st.nextToken();
          if (Server.rand.nextBoolean()) {
            s = s + " " + a;
          }
        }
        return "Someone mentioned " + s + " before!";
      }
      StringTokenizer st = new StringTokenizer(message);
      String s = st.nextToken().toLowerCase();
      while (st.hasMoreTokens())
      {
        a = st.nextToken();
        if (Server.rand.nextBoolean()) {
          s = s + " " + a;
        }
      }
      return "The " + s + " is strong in that one.";
    }
    if ((rand < 10) || (message.toLowerCase().contains("the")))
    {
      StringTokenizer st = new StringTokenizer(message);
      String s = st.nextToken().toLowerCase();
      while (st.hasMoreTokens())
      {
        s = st.nextToken();
        if (s.equalsIgnoreCase("the")) {
          s = st.nextToken();
        }
      }
      s = s + ((s.endsWith("s")) || (s.equals("sheep")) || (s.equals("fish")) || (s.equals("feet")) ? "" : "s");
      if (next < 10) {
        return "Them " + s + " are someone elses problem.";
      }
      if (next < 20) {
        return "What about " + s + "?";
      }
      if (next < 30) {
        return "I never understood all that talk about " + s + ".";
      }
      if (next < 40) {
        return "Why do you care about " + s + "?";
      }
      if (next < 50) {
        return "I can't recall anyone mentioning " + s + " before.";
      }
      return "I had no clue that you cared so much about " + s + ".";
    }
    if ((rand < 20) || (message.toLowerCase().contains("what")))
    {
      if ((this.mychats.size() > 0) && (Server.rand.nextBoolean())) {
        return "What you are really saying is that " + (String)this.mychats.get(Server.rand.nextInt(this.mychats.size())) + "?";
      }
      return "I don't understand what you are saying.";
    }
    if (rand < 30)
    {
      if (this.mychats.size() > 0) {
        return "Someone said that " + (String)this.mychats.get(Server.rand.nextInt(this.mychats.size())) + ".";
      }
      return "Obviously.";
    }
    if (rand < 40)
    {
      if (Server.rand.nextInt(3) == 0) {
        return "I might go there actually.";
      }
      if (Server.rand.nextBoolean()) {
        return "Don't do it.";
      }
      return "Make my day, will you.";
    }
    if ((rand < 50) || (message.toLowerCase().contains("you")))
    {
      String loc = "The Forest Giant";
      if (next < 10)
      {
        loc = "You";
      }
      else
      {
        PlayerInfo[] pinfs;
        if (next < 20)
        {
          loc = "Brightberry";
          pinfs = PlayerInfoFactory.getPlayerInfos();
          if (pinfs.length > 0) {
            loc = pinfs[Server.rand.nextInt(pinfs.length)].getName();
          }
        }
        else if (next < 40)
        {
          loc = "Fo";
        }
        else if (next < 50)
        {
          loc = "Libila";
        }
        else if (next < 60)
        {
          loc = "Magranon";
        }
        else if (next < 70)
        {
          loc = "Vynora";
          pinfs = Deities.getDeities();a = pinfs.length;
          for (s = 0; s < a; s++)
          {
            Deity d = pinfs[s];
            if ((d.isCustomDeity()) && (Server.rand.nextBoolean()))
            {
              loc = d.getName();
              break;
            }
          }
        }
        else if (next < 80)
        {
          loc = "Uttacha";
        }
        else if (next < 90)
        {
          loc = "The Deathcrawler";
        }
        else if (next < 95)
        {
          loc = "a tree";
          ItemTemplate[] temps = ItemTemplateFactory.getInstance().getTemplates();
          if (temps.length > 0) {
            loc = temps[Server.rand.nextInt(temps.length)].getNameWithGenus();
          }
        }
      }
      if (Server.rand.nextBoolean()) {
        return emptyOrReceiver + "That's " + loc + " you're referring to.";
      }
      if ((this.receivedchats.size() > 3) && (Server.rand.nextBoolean()))
      {
        String[] chats = getReceivedChatsAsArr();
        String garbled = chats[Server.rand.nextInt(chats.length)];
        StringTokenizer st = new StringTokenizer(garbled);
        String s = st.nextToken().toLowerCase();
        while (st.hasMoreTokens())
        {
          String a = st.nextToken();
          if (Server.rand.nextBoolean()) {
            s = s + " " + a;
          }
        }
        return emptyOrReceiver + "Maybe the " + s + " is something to meditate over?";
      }
      return emptyOrReceiver + "We can also discuss " + loc + " if you want.";
    }
    if (rand < 60)
    {
      if ((this.receivedchats.size() > 3) && (Server.rand.nextBoolean()))
      {
        String[] chats = getReceivedChatsAsArr();
        garbled = chats[Server.rand.nextInt(chats.length)];
        st = new StringTokenizer(garbled);
        s = st.nextToken().toLowerCase();
        while (st.hasMoreTokens())
        {
          String a = st.nextToken();
          if (Server.rand.nextBoolean()) {
            s = s + " " + a;
          }
        }
        return "On the other hand I heard that " + s + ".";
      }
      EpicMission[] ems = EpicServerStatus.getCurrentEpicMissions();
      String garbled = ems;StringTokenizer st = garbled.length;
      for (String s = 0; s < st; s++)
      {
        EpicMission em = garbled[s];
        if (em.isCurrent())
        {
          Deity deity = Deities.getDeity(em.getEpicEntityId());
          if (deity != null) {
            if (deity.getFavoredKingdom() == this.owner.getKingdomId()) {
              return "I'm considering helping " + deity.getName() + " out with " + em.getScenarioName() + ".";
            }
          }
        }
      }
      return "Please.";
    }
    if (rand < 70)
    {
      if ((this.localchats.size() > 3) && (Server.rand.nextBoolean()))
      {
        String[] chats = getLocalChatsAsArr();
        String garbled = chats[Server.rand.nextInt(chats.length)];
        StringTokenizer st = new StringTokenizer(garbled);
        String s = st.nextToken().toLowerCase();
        while (st.hasMoreTokens())
        {
          String a = st.nextToken();
          if (Server.rand.nextInt(3) == 0) {
            s = s + " " + a;
          }
        }
        return "Unless " + s + " of course.";
      }
      return emptyOrReceiver + "Not today.";
    }
    if (rand < 80)
    {
      StringTokenizer st = new StringTokenizer(message);
      String s = st.nextToken().toLowerCase();
      while (st.hasMoreTokens())
      {
        String a = st.nextToken();
        if (Server.rand.nextBoolean()) {
          s = a;
        }
      }
      return "There's always been " + s + " if you need it.";
    }
    if (rand < 90)
    {
      StringTokenizer st = new StringTokenizer(message);
      String s = st.nextToken().toLowerCase();
      while (st.hasMoreTokens())
      {
        String a = st.nextToken();
        if (Server.rand.nextBoolean()) {
          s = s + " " + a;
        }
      }
      return emptyOrReceiver + "I know for a fact that " + s + " has been around these parts somewhere.";
    }
    StringTokenizer st = new StringTokenizer(message);
    String s = st.nextToken().toLowerCase();
    while (st.hasMoreTokens())
    {
      String a = st.nextToken();
      if (Server.rand.nextBoolean()) {
        s = s + " " + a;
      }
    }
    return emptyOrReceiver + "I wouldn't " + s + " for my life.";
  }
  
  public final void startLocalChat()
  {
    int chattinessMod = 1;
    float mod = 1.0F;
    if (System.currentTimeMillis() - this.lastPCChattedLocal < 300000L)
    {
      chattinessMod = Server.rand.nextInt(10);
      mod = 0.3F;
    }
    if ((float)(System.currentTimeMillis() - this.lastChattedLocal) > 60000.0F * Math.max(1.0F, (this.chattiness - chattinessMod) * mod)) {
      if (!this.owner.isDead()) {
        if ((this.lastTx != this.owner.getTileX()) || (this.lastTy != this.owner.getTileY())) {
          for (int x = -8; x <= 8; x++) {
            for (int y = -8; y <= 8; y++)
            {
              VolaTile t = Zones.getTileOrNull(this.owner.getTileX() + x, this.owner.getTileY() + y, this.owner.isOnSurface());
              if (t != null) {
                for (Creature c : t.getCreatures()) {
                  if ((c.getPower() <= 0) && (c.getWurmId() != this.owner.getWurmId()) && (((c.isPlayer()) && (c.isVisibleTo(this.owner))) || (c.isNpc())))
                  {
                    String s = getSayToCreature(c);
                    if ((s != null) && (s.length() > 0)) {
                      if (!this.localChats.contains(s))
                      {
                        VolaTile tile = this.owner.getCurrentTile();
                        if (tile != null)
                        {
                          if (this.owner.isFriendlyKingdom(c.getKingdomId())) {
                            this.owner.turnTowardsCreature(c);
                          }
                          this.localChats.add(s);
                          Message m = new Message(this.owner, (byte)0, ":Local", "<" + this.owner.getName() + "> " + s);
                          
                          tile.broadCastMessage(m);
                          this.lastTx = this.owner.getTileX();
                          this.lastTy = this.owner.getTileY();
                          this.lastChattedLocal = System.currentTimeMillis();
                          return;
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
  
  public final String getSayToCreature(Creature creature)
  {
    int rand = Server.rand.nextInt(100);
    int next = Server.rand.nextInt(100);
    if ((creature.getPlayingTime() < 3600000L) && (next < 5))
    {
      if (this.owner.isFriendlyKingdom(creature.getKingdomId()))
      {
        if (rand < 20) {
          return "Hey " + creature.getName() + "! Nice to see you! I'll do my best to get the Key of the Heavens before you, you know!";
        }
        if (rand < 40) {
          return "Oh " + creature.getName() + "! Have you seen the Key of the Heavens? I badly need it.";
        }
        if (rand < 60) {
          return creature.getName() + "! Please give me a Key of the Heavens if you find one.";
        }
        if (rand < 80) {
          return "Please " + creature.getName() + ", I badly need a Key of the Heavens. I'll pay well.";
        }
        return "Look, a newcomer. " + creature.getName() + ", I bet I find the Key of the Heavens before you do!";
      }
      if (rand < 20) {
        return "Hey " + creature.getName() + "! You'll never get the Key of the Heavens you know!";
      }
      if (rand < 40) {
        return "You! " + creature.getName() + "! Have you seen the Key of the Heavens?";
      }
      if (rand < 60) {
        return creature.getName() + "! Give me a Key of the Heavens!";
      }
      if (rand < 80) {
        return "Now " + creature.getName() + ", I will have the Key of the Heavens. No matter the cost.";
      }
      return "Look, " + creature.getName() + ". You stand no chance of finding the Key of the Heavens before I do!";
    }
    if (next < 20)
    {
      Item[] worn = creature.getBody().getContainersAndWornItems();
      if ((worn != null) && (worn.length > 0))
      {
        Item selected = worn[Server.rand.nextInt(worn.length)];
        if (this.owner.isFriendlyKingdom(creature.getKingdomId()))
        {
          if (rand < 20) {
            return "Nice " + selected.getName() + " there, " + creature.getName() + ".";
          }
          if (rand < 40)
          {
            if (selected.getDamage() > 0.0F) {
              return "Hey " + creature.getName() + ". Your " + selected.getName() + " needs repairing.";
            }
            return "Hey " + creature.getName() + ". Your " + selected.getName() + " looks really nice.";
          }
          if (rand < 60)
          {
            if (selected.getRarity() > 0) {
              return "Hey " + creature.getName() + ". Your " + selected.getName() + " looks really special.";
            }
            return "Nothing special with your " + selected.getName() + " " + creature.getName() + ", is there?";
          }
          if (rand < 80)
          {
            if (selected.isWeapon())
            {
              if (selected.getCurrentQualityLevel() > 40.0F) {
                return "That " + selected.getName() + " looks pretty dangerous " + creature.getName() + ".";
              }
              return "That " + selected.getName() + " doesn't look very dangerous " + creature.getName() + ".";
            }
            if (selected.isArmour())
            {
              if (selected.getCurrentQualityLevel() > 40.0F) {
                return "That " + selected.getName() + " looks pretty darn good, " + creature.getName() + ".";
              }
              return "That " + selected.getName() + " looks pretty darn rotten, " + creature.getName() + ".";
            }
            if (selected.isFood())
            {
              if (selected.getCurrentQualityLevel() > 60.0F) {
                return "That " + selected.getName() + " looks really tasty, " + creature.getName() + ".";
              }
              return "That " + selected.getName() + " looks pretty awful, " + creature.getName() + ".";
            }
            if (selected.isEnchantableJewelry())
            {
              if (selected.getCurrentQualityLevel() > 40.0F) {
                return "You should enchant that " + selected.getName() + " you know, " + creature.getName() + ". If you haven't already.";
              }
              return "That " + selected.getName() + " looks pretty mundane, " + creature.getName() + ".";
            }
          }
          return "Look, " + creature.getName() + ". If you find a Key of the Heavens just give it to me will you?";
        }
        if (rand < 20) {
          return "That " + selected.getName() + " there looks awful, " + creature.getName() + ".";
        }
        if (rand < 40)
        {
          if (selected.getDamage() > 0.0F) {
            return "Haha " + creature.getName() + ". Your " + selected.getName() + " needs repairing.";
          }
          return "Well, " + creature.getName() + ". Your " + selected.getName() + " stinks.";
        }
        if (rand < 60)
        {
          if (selected.getRarity() > 0) {
            return "Ooh " + creature.getName() + ". I think I'll enjoy your " + selected.getName() + ".";
          }
          return "Your " + selected.getName() + " will look better with your blood on it, " + creature.getName() + ".";
        }
        if (rand < 80)
        {
          if (selected.isWeapon())
          {
            if (selected.getCurrentQualityLevel() > 40.0F) {
              return "That " + selected.getName() + " doesn't scare me " + creature.getName() + ".";
            }
            return "That " + selected.getName() + " is the laughing stock of the lands, " + creature.getName() + ".";
          }
          if (selected.isArmour())
          {
            if (selected.getCurrentQualityLevel() > 40.0F) {
              return "That " + selected.getName() + " won't keep you alive, " + creature.getName() + ".";
            }
            return "That " + selected.getName() + " is pathetic, " + creature.getName() + ".";
          }
        }
        return "I'll pry the " + selected.getName() + " from you before your body has gone cold, " + creature.getName() + ".";
      }
      if (this.owner.isFriendlyKingdom(creature.getKingdomId())) {
        return "You should get some stuff, " + creature.getName() + ".";
      }
      return "What a disappointment you are, " + creature.getName() + ".";
    }
    if (next < 40)
    {
      Skill[] skills = creature.getSkills().getSkills();
      if ((skills != null) && (skills.length > 0))
      {
        if (!this.owner.isFriendlyKingdom(creature.getKingdomId()))
        {
          Skill selected = skills[Server.rand.nextInt(skills.length)];
          if (selected.getKnowledge() < 50.0D) {
            return "Can't say I fear your knowledge in " + selected.getName() + ", " + creature.getName() + ".";
          }
          return "Your knowledge in " + selected.getName() + " won't save you now, " + creature.getName() + ".";
        }
        Skill selected = skills[Server.rand.nextInt(skills.length)];
        if (selected.getKnowledge() < 50.0D) {
          return "How is your knowledge in " + selected.getName() + " now, " + creature.getName() + "?";
        }
        return "Word is that you have improved your knowledge in " + selected.getName() + ", " + creature.getName() + ".";
      }
      if (this.owner.isFriendlyKingdom(creature.getKingdomId())) {
        return "You should get some skills, " + creature.getName() + ".";
      }
      return "What a disappointment you are, " + creature.getName() + ".";
    }
    if (next < 60)
    {
      Village[] vills = Villages.getVillages();
      if ((vills != null) && (vills.length > 0))
      {
        if (this.owner.isFriendlyKingdom(creature.getKingdomId())) {
          return "Have you visited " + vills[Server.rand.nextInt(vills.length)].getName() + " lately " + creature.getName() + "?";
        }
        return "I'll send you back to " + vills[Server.rand.nextInt(vills.length)].getName() + " " + creature.getName() + "?";
      }
      if (this.owner.isFriendlyKingdom(creature.getKingdomId())) {
        return "It's just a big empty nothing out here isn't it, " + creature.getName() + "?";
      }
      return "It's just a big empty nothingness to die in here isn't it, " + creature.getName() + "?";
    }
    if (next < 80)
    {
      if (this.owner.isFriendlyKingdom(creature.getKingdomId())) {
        return "Did you hear, " + creature.getName() + "? " + EndGameItems.locateRandomEndGameItem(this.owner);
      }
      return "You'll be dead soon enough, " + creature.getName() + ".";
    }
    if (next < 90)
    {
      if (this.owner.isFriendlyKingdom(creature.getKingdomId())) {
        return "I recently had a vision.. " + Deities.getRandomStatus() + " That made me really afraid.";
      }
      return "There will be no next time, " + creature.getName() + ".";
    }
    if (this.owner.isFriendlyKingdom(creature.getKingdomId())) {
      return "Are you enjoying yourself, " + creature.getName() + "?";
    }
    return "See you in the Soulfall, " + creature.getName() + "!";
  }
  
  int numchatsSinceLast = 0;
  
  private final void pollLocal()
  {
    Message last = null;
    String mess = null;
    for (Message message : this.unansweredLChats.keySet()) {
      try
      {
        mess = message.getMessage().substring(message.getMessage().indexOf(">") + 1, message.getMessage().length());
        mess = mess.replace(".", "");
        mess = mess.trim();
        if (mess.length() > 0)
        {
          this.localchats.put(mess, message.getSender().getName());
          this.numchatsSinceLast += 1;
          last = message;
        }
      }
      catch (Exception ex)
      {
        logger.log(Level.INFO, "Failed chat: " + ex.getMessage());
      }
    }
    this.unansweredLChats.clear();
    if (last != null) {
      answerLocalChat(last, mess);
    }
  }
  
  public final void answerLocalChat(Message message, @Nullable String mess)
  {
    int chattinessMod = 0;
    boolean toMe = false;
    if ((message.getSender() != null) && (message.getSender().isPlayer()))
    {
      this.lastPCChattedLocal = System.currentTimeMillis();
      chattinessMod = Server.rand.nextInt(15);
      if ((mess != null) && (mess.toLowerCase().contains(this.owner.getName().toLowerCase()))) {
        toMe = true;
      }
    }
    if ((toMe) || ((System.currentTimeMillis() - this.lastChattedLocal > 30000L * Math.max(1, this.chattiness - chattinessMod)) && (Server.rand.nextBoolean()) && (this.numchatsSinceLast > 0)))
    {
      this.lastChattedLocal = System.currentTimeMillis();
      try
      {
        if (mess != null)
        {
          Message m = new Message(this.owner, (byte)0, ":Local", "<" + this.owner.getName() + "> " + getAnswerToMessage(message.getSender().getName(), mess));
          VolaTile tile = this.owner.getCurrentTile();
          if (tile != null) {
            tile.broadCastMessage(m);
          }
        }
        this.numchatsSinceLast = 0;
      }
      catch (Exception ex)
      {
        logger.log(Level.INFO, ex.getMessage(), ex);
      }
    }
  }
  
  public final void addLocalChat(Message message)
  {
    this.unansweredLChats.put(message, message.getSender().getName());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\creatures\ai\ChatManager.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */