package com.wurmonline.server.tutorial;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.tutorial.stages.FishingStage;
import com.wurmonline.server.tutorial.stages.WelcomeStage;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;

public class PlayerTutorial
{
  private static HashMap<Long, PlayerTutorial> currentTutorials = new HashMap();
  
  public static PlayerTutorial getTutorialForPlayer(long wurmId, boolean create)
  {
    if ((!currentTutorials.containsKey(Long.valueOf(wurmId))) && (create)) {
      addTutorial(wurmId, new PlayerTutorial(wurmId));
    }
    return (PlayerTutorial)currentTutorials.get(Long.valueOf(wurmId));
  }
  
  public static void addTutorial(long wurmId, PlayerTutorial tutorial)
  {
    currentTutorials.put(Long.valueOf(wurmId), tutorial);
  }
  
  public static void removeTutorial(long wurmId)
  {
    currentTutorials.remove(Long.valueOf(wurmId));
  }
  
  public static void firePlayerTrigger(long wurmId, PlayerTutorial.PlayerTrigger trigger)
  {
    if (!currentTutorials.containsKey(Long.valueOf(wurmId))) {
      return;
    }
    if (!((PlayerTutorial)currentTutorials.get(Long.valueOf(wurmId))).getCurrentStage().awaitingTrigger(trigger)) {
      return;
    }
    ((PlayerTutorial)currentTutorials.get(Long.valueOf(wurmId))).getCurrentStage().clearTrigger();
    ((PlayerTutorial)currentTutorials.get(Long.valueOf(wurmId))).sendUpdateStageBML();
  }
  
  public static boolean doesTutorialExist(long wurmId)
  {
    return currentTutorials.containsKey(Long.valueOf(wurmId));
  }
  
  public static void endTutorial(Player p)
  {
    if (doesTutorialExist(p.getWurmId())) {
      getTutorialForPlayer(p.getWurmId(), false).customMethods.tutorialSkipped(p);
    }
  }
  
  public static void startTutorialCommand(Player player, String message)
  {
    getTutorialForPlayer(player.getWurmId(), true).sendCurrentStageBML();
  }
  
  public static void skipTutorialCommand(Player player, String message)
  {
    if (doesTutorialExist(player.getWurmId()))
    {
      player.getCommunicator().sendCloseWindow(getTutorialForPlayer(player.getWurmId(), false).getCurrentStage().getWindowId());
      endTutorial(player);
      removeTutorial(player.getWurmId());
    }
    else
    {
      player.getCommunicator().sendNormalServerMessage("You do not currently have an active tutorial. Nothing to skip.");
    }
  }
  
  public static void testTutorialCommand(Player player, String message)
  {
    StringTokenizer st = new StringTokenizer(message);
    st.nextToken();
    if (doesTutorialExist(player.getWurmId()))
    {
      player.getCommunicator().sendCloseWindow(getTutorialForPlayer(player.getWurmId(), false).getCurrentStage().getWindowId());
      endTutorial(player);
      removeTutorial(player.getWurmId());
    }
    else if (st.hasMoreTokens())
    {
      String tutorialType = st.nextToken();
      int fastForward = 0;
      if (st.hasMoreTokens()) {
        fastForward = Integer.parseInt(st.nextToken());
      }
      switch (tutorialType.toLowerCase())
      {
      case "fishing": 
        startNewTutorial(player, PlayerTutorial.TutorialType.FISHING, fastForward);
        break;
      default: 
        startNewTutorial(player, PlayerTutorial.TutorialType.DEFAULT, fastForward);
      }
    }
    else
    {
      getTutorialForPlayer(player.getWurmId(), true).sendCurrentStageBML();
    }
  }
  
  public static void startNewTutorial(Player p, PlayerTutorial.TutorialType t, int fastForward)
  {
    if (doesTutorialExist(p.getWurmId()))
    {
      p.getCommunicator().sendCloseWindow(getTutorialForPlayer(p.getWurmId(), false).getCurrentStage().getWindowId());
      if (!p.hasFlag(42)) {
        endTutorial(p);
      }
      removeTutorial(p.getWurmId());
    }
    switch (PlayerTutorial.3.$SwitchMap$com$wurmonline$server$tutorial$PlayerTutorial$TutorialType[t.ordinal()])
    {
    case 1: 
      PlayerTutorial newTut = new PlayerTutorial(p.getWurmId(), new FishingStage(p.getWurmId()), new PlayerTutorial.1());
      
      addTutorial(p.getWurmId(), newTut);
      if (fastForward > 0)
      {
        for (int i = 0; i < fastForward; i++) {
          if (!newTut.skipCurrentStage())
          {
            p.getCommunicator().sendNormalServerMessage("Cannot skip to stage " + fastForward + " as there are only " + i + " stages in this tutorial.");
            break;
          }
        }
        newTut.getCurrentStage().setForceOpened(true);
      }
      newTut.sendCurrentStageBML();
      break;
    default: 
      PlayerTutorial newBasicTut = getTutorialForPlayer(p.getWurmId(), true);
      if (fastForward > 0)
      {
        for (int i = 0; i < fastForward; i++) {
          if (!newBasicTut.skipCurrentStage())
          {
            p.getCommunicator().sendNormalServerMessage("Cannot skip to stage " + fastForward + " as there are only " + i + " stages in this tutorial.");
            break;
          }
        }
        newBasicTut.getCurrentStage().setForceOpened(true);
      }
      newBasicTut.sendCurrentStageBML();
    }
  }
  
  public static void sendTutorialList(Player p)
  {
    p.getCommunicator().sendPersonalJournalTutorial((byte)-1, (byte)PlayerTutorial.TutorialType.DEFAULT.ordinal(), "New Player Tutorial");
    p.getCommunicator().sendPersonalJournalTutorial((byte)-1, (byte)PlayerTutorial.TutorialType.FISHING.ordinal(), "Fishing Tutorial");
    for (int i = 0; i < DEFAULT_STAGE_NAMES.length; i++) {
      p.getCommunicator().sendPersonalJournalTutorial((byte)PlayerTutorial.TutorialType.DEFAULT.ordinal(), (byte)i, DEFAULT_STAGE_NAMES[i]);
    }
    for (int i = 0; i < FISHING_STAGE_NAMES.length; i++) {
      p.getCommunicator().sendPersonalJournalTutorial((byte)PlayerTutorial.TutorialType.FISHING.ordinal(), (byte)i, FISHING_STAGE_NAMES[i]);
    }
  }
  
  public static final String[] DEFAULT_STAGE_NAMES = { "Welcome to Wurm", "Looking Around", "Movement", "Inventory & Items", "Starting Out", "Activating & Equipping", "World Interaction", "Dropping & Taking", "Terraforming", "Woodcutting", "Creating Items", "Mining", "Skills", "Combat", "Keybinds, Rules & Settings" };
  public static final String[] FISHING_STAGE_NAMES = { "Intro, Net & Spear Fishing", "Rod & Pole Fishing", "Final Tips" };
  private long playerId;
  private TutorialStage currentStage;
  private final TutorialStage initialStage;
  private final PlayerTutorial.TutorialMethods customMethods;
  
  public PlayerTutorial(long playerId)
  {
    this(playerId, new WelcomeStage(playerId), new PlayerTutorial.2());
  }
  
  public PlayerTutorial(long playerId, TutorialStage initialStage, PlayerTutorial.TutorialMethods customMethods)
  {
    this.playerId = playerId;
    this.initialStage = initialStage;
    this.currentStage = initialStage;
    this.customMethods = customMethods;
    
    addTutorial(playerId, this);
  }
  
  public long getPlayerId()
  {
    return this.playerId;
  }
  
  public TutorialStage getCurrentStage()
  {
    return this.currentStage;
  }
  
  public boolean skipCurrentStage()
  {
    if (this.currentStage.getNextStage() != null)
    {
      this.currentStage = this.currentStage.getNextStage();
      return true;
    }
    return false;
  }
  
  public boolean increaseCurrentStage()
  {
    if (!getCurrentStage().increaseSubStage())
    {
      if (this.currentStage.isForceOpened()) {
        this.currentStage = null;
      } else {
        this.currentStage = this.currentStage.getNextStage();
      }
      if (this.currentStage == null)
      {
        removeTutorial(getPlayerId());
        return false;
      }
    }
    return true;
  }
  
  public void restart()
  {
    this.currentStage = this.initialStage;
    this.currentStage.resetSubStage();
    sendCurrentStageBML();
  }
  
  public void decreaseCurrentStage()
  {
    if (getCurrentStage().decreaseSubStage())
    {
      this.currentStage = this.currentStage.getLastStage();
      this.currentStage.toLastSubStage();
      if (this.currentStage == null) {
        this.currentStage = this.initialStage;
      }
    }
  }
  
  public void sendCurrentStageBML()
  {
    try
    {
      Player p = Players.getInstance().getPlayer(getPlayerId());
      p.getCommunicator().sendBml(getCurrentStage().getWindowId(), 320, 450, 0.0F, 0.5F, false, p.hasFlag(42), 
        getCurrentStage().getCurrentBML(), 255, 255, 255, "Tutorial");
    }
    catch (NoSuchPlayerException e)
    {
      removeTutorial(getPlayerId());
    }
  }
  
  public void sendUpdateStageBML()
  {
    try
    {
      Player p = Players.getInstance().getPlayer(getPlayerId());
      p.getCommunicator().sendBml(getCurrentStage().getWindowId(), 320, 450, 0.0F, 0.5F, false, p.hasFlag(42), 
        getCurrentStage().getUpdateBML(), 255, 255, 255, "Tutorial");
    }
    catch (NoSuchPlayerException e)
    {
      removeTutorial(getPlayerId());
    }
  }
  
  public void updateReceived(Properties answers)
  {
    String skipTutorial = answers.getProperty("close");
    if ((skipTutorial != null) && (skipTutorial.equals("true")))
    {
      try
      {
        Player p = Players.getInstance().getPlayer(getPlayerId());
        endTutorial(p);
      }
      catch (NoSuchPlayerException localNoSuchPlayerException) {}
      removeTutorial(getPlayerId());
    }
    String nextStage = answers.getProperty("next");
    String skipStage = answers.getProperty("skip");
    if (((nextStage != null) && (nextStage.equals("true"))) || ((skipStage != null) && (skipStage.equals("true"))))
    {
      boolean wasForced = getCurrentStage().isForceOpened();
      if (increaseCurrentStage())
      {
        sendCurrentStageBML();
        if (getCurrentStage().shouldSkipTrigger()) {
          sendUpdateStageBML();
        }
      }
      else if (!wasForced)
      {
        try
        {
          Player p = Players.getInstance().getPlayer(this.playerId);
          this.customMethods.tutorialCompleted(p);
        }
        catch (NoSuchPlayerException localNoSuchPlayerException1) {}
      }
    }
    String lastStage = answers.getProperty("back");
    if ((lastStage != null) && (lastStage.equals("true")))
    {
      decreaseCurrentStage();
      sendCurrentStageBML();
      getCurrentStage().clearTrigger();
      sendUpdateStageBML();
    }
    String restartTut = answers.getProperty("restart");
    if ((restartTut != null) && (restartTut.equals("true"))) {
      getTutorialForPlayer(getPlayerId(), false).restart();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\tutorial\PlayerTutorial.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */