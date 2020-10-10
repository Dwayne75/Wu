package com.wurmonline.server.questions;

import com.wurmonline.server.Features.Feature;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.kingdom.Appointment;
import com.wurmonline.server.kingdom.Appointments;
import com.wurmonline.server.kingdom.King;
import com.wurmonline.server.players.Cultist;
import com.wurmonline.server.players.Cults;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.Titles.Title;
import com.wurmonline.shared.util.StringUtilities;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class TitleCompoundQuestion
  extends Question
{
  private final List<Titles.Title> firstTitleList = new LinkedList();
  private final List<Titles.Title> secondTitleList = new LinkedList();
  private int totalTitles = 0;
  
  public TitleCompoundQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget)
  {
    super(aResponder, aTitle, aQuestion, 39, aTarget);
  }
  
  public void answer(Properties answers)
  {
    logger.info(String.format("%s answered question.", new Object[] { getResponder().getName() }));
    setAnswer(answers);
    QuestionParser.parseTitleCompoundQuestion(this);
  }
  
  private StringBuilder getKingdomTitleBox(Titles.Title[] titles, String key, List<Titles.Title> titleList, @Nullable Titles.Title currentTitle)
  {
    StringBuilder sb = new StringBuilder();
    this.totalTitles = 0;
    if (titles.length == 0)
    {
      if ((getResponder().getAppointments() != 0L) || (getResponder().isAppointed()))
      {
        int defaultTitle = 0;
        sb.append("harray{text{text=\"" + key + ": \"};dropdown{id=\"" + key + "\";options=\"None");
        
        sb.append(",");
        sb.append(Titles.Title.Kingdomtitle.getName(getResponder().isNotFemale()));
        titleList.add(Titles.Title.Kingdomtitle);
        if (currentTitle != null) {
          if (currentTitle.isRoyalTitle()) {
            defaultTitle = 1;
          }
        }
        sb.append("\";default=\"" + defaultTitle + "\"}}");
        this.totalTitles += 1;
      }
      else
      {
        sb.append("text{text=\"You have no titles to select from.\"}");
      }
    }
    else
    {
      int defaultTitle = 0;
      
      sb.append("harray{text{text=\"" + key + ": \"};dropdown{id=\"" + key + "\";options=\"None");
      for (int x = 0; x < titles.length; x++)
      {
        sb.append(",");
        
        sb.append(titles[x].getName(getResponder().isNotFemale()));
        if (currentTitle != null) {
          if (titles[x].id == currentTitle.id) {
            defaultTitle = x + 1;
          }
        }
        titleList.add(titles[x]);
        this.totalTitles += 1;
      }
      if ((getResponder().getAppointments() != 0L) || (getResponder().isAppointed()))
      {
        sb.append(",");
        sb.append(Titles.Title.Kingdomtitle.getName(getResponder().isNotFemale()));
        titleList.add(Titles.Title.Kingdomtitle);
        if (currentTitle != null) {
          if (currentTitle.isRoyalTitle()) {
            defaultTitle = titles.length + 1;
          }
        }
        this.totalTitles += 1;
      }
      sb.append("\";default=\"" + defaultTitle + "\"}}");
    }
    return sb;
  }
  
  public void sendQuestion()
  {
    StringBuilder sb = new StringBuilder(getBmlHeader());
    
    boolean isMale = ((Player)getResponder()).isNotFemale();
    Titles.Title[] titles = ((Player)getResponder()).getTitles();
    Arrays.sort(titles, new TitleCompoundQuestion.1(this, isMale));
    
    String suff = "";
    String pre = "";
    if (!getResponder().hasFlag(24)) {
      pre = getResponder().getAbilityTitle();
    }
    if ((getResponder().getCultist() != null) && (!getResponder().hasFlag(25))) {
      suff = suff + " " + getResponder().getCultist().getCultistTitleShort();
    }
    if (getResponder().isKing()) {
      suff = suff + " [" + King.getRulerTitle(getResponder().getSex() == 0, getResponder().getKingdomId()) + "]";
    }
    if ((getResponder().getTitle() != null) || ((Features.Feature.COMPOUND_TITLES.isEnabled()) && (getResponder().getSecondTitle() != null))) {
      suff = suff + " [" + getResponder().getTitleString() + "]";
    }
    if ((getResponder().isChampion()) && (getResponder().getDeity() != null)) {
      suff = suff + " [Champion of " + getResponder().getDeity().name + "]";
    }
    String playerName = pre + StringUtilities.raiseFirstLetterOnly(getResponder().getName()) + suff;
    sb.append("text{text=\"You are currently known as: " + playerName + "\"}");
    sb.append("text{text=\"\"}");
    
    sb.append(getKingdomTitleBox(titles, "First", this.firstTitleList, getResponder().getTitle()));
    sb.append("text{text=\"\"}");
    sb.append(getKingdomTitleBox(titles, "Second", this.secondTitleList, getResponder().getSecondTitle()));
    sb.append("text{text=\"\"}");
    sb.append("text{text=\"You have a total of " + this.totalTitles + " titles.\"}");
    sb.append("text{text=\"\"}");
    sb.append("text{type=\"italic\";text=\"Note: Armour smiths that use their title gets faster armour improvement rate.\"}");
    String occultist = getResponder().getAbilityTitle();
    String meditation = getResponder().getCultist() != null ? Cults.getNameForLevel(getResponder().getCultist().getPath(), getResponder().getCultist().getLevel()) : "";
    if ((occultist.length() > 0) || (meditation.length() > 0))
    {
      sb.append("text{type=\"bold\";text=\"Select which titles to hide (if any)\"}");
      if (occultist.length() > 0) {
        sb.append("checkbox{id=\"hideoccultist\";text=\"" + occultist + "(Occultist)\";selected=\"" + getResponder().hasFlag(24) + "\"}");
      }
      if (meditation.length() > 0) {
        sb.append("checkbox{id=\"hidemeditation\";text=\"" + meditation + " (Meditation)\";selected=\"" + getResponder().hasFlag(25) + "\"}");
      }
      sb.append("text{text=\"\"}");
    }
    if (Servers.isThisAPvpServer())
    {
      King king = King.getKing(getResponder().getKingdomId());
      if ((king != null) && ((getResponder().getAppointments() != 0L) || (getResponder().isAppointed())))
      {
        sb.append("text{type=\"bold\";text=\"Select which kingdom office to remove (if any)\"}");
        Appointments a = Appointments.getAppointments(king.era);
        for (int x = 0; x < a.officials.length; x++)
        {
          int oId = x + 1500;
          Appointment o = a.getAppointment(oId);
          if (a.officials[x] == getResponder().getWurmId()) {
            sb.append("checkbox{id=\"office" + oId + "\";text=\"" + o.getNameForGender(getResponder().getSex()) + " (Office)\";}");
          }
        }
      }
    }
    sb.append(createAnswerButton2());
    getResponder().getCommunicator().sendBml(500, 300, true, true, sb.toString(), 200, 200, 200, this.title);
  }
  
  Titles.Title getFirstTitle(int aPosition)
  {
    return (Titles.Title)this.firstTitleList.get(aPosition);
  }
  
  Titles.Title getSecondTitle(int aPosition)
  {
    return (Titles.Title)this.secondTitleList.get(aPosition);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\questions\TitleCompoundQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */