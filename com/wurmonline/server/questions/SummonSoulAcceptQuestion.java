package com.wurmonline.server.questions;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import java.util.Properties;

public class SummonSoulAcceptQuestion
  extends Question
{
  private final String summonerName;
  private final int summonX;
  private final int summonY;
  private final int summonLayer;
  private final int summonFloor;
  
  public SummonSoulAcceptQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget, Creature summoner)
  {
    super(aResponder, aTitle, aQuestion, 27, aTarget);
    this.summonerName = summoner.getName();
    this.summonX = (summoner.getTileX() * 4);
    this.summonY = (summoner.getTileY() * 4);
    this.summonLayer = summoner.getLayer();
    this.summonFloor = summoner.getFloorLevel();
  }
  
  public void answer(Properties answers)
  {
    setAnswer(answers);
    boolean accept = getAnswer().getProperty("summ").equals("true");
    if (accept)
    {
      if (getResponder().getEnemyPresense() > 0)
      {
        getResponder().getCommunicator().sendNormalServerMessage("You cannot be summoned right now, enemies are nearby.");
        return;
      }
      getResponder().setTeleportPoints(this.summonX, this.summonY, this.summonLayer, this.summonFloor);
      if (getResponder().startTeleporting())
      {
        getResponder().getCommunicator().sendNormalServerMessage("You are summoned to the location of " + this.summonerName + ".");
        getResponder().getCommunicator().sendTeleport(false);
      }
    }
    else
    {
      getResponder().getCommunicator().sendNormalServerMessage("You decline the summon from " + this.summonerName + ".");
    }
  }
  
  public void sendQuestion()
  {
    try
    {
      Creature asker2 = Server.getInstance().getCreature(this.target);
      StringBuilder buf = new StringBuilder();
      buf.append(getBmlHeader());
      buf.append("text{text='" + asker2.getName() + " would like to summon you to their location.'}text{text=''}text{text=''}text{text='Would you like to accept?'}");
      buf.append("text{text=''}");
      buf.append("radio{ group='summ'; id='true';text='Yes'}");
      buf.append("radio{ group='summ'; id='false';text='No';selected='true'}");
      buf.append(createAnswerButton2());
      getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
      asker2.getCommunicator().sendNormalServerMessage("You request " + getResponder().getName() + " to be summoned to your location.");
    }
    catch (NoSuchCreatureException|NoSuchPlayerException localNoSuchCreatureException) {}
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\SummonSoulAcceptQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */