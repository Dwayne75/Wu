package com.wurmonline.server.questions;

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
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public final class TitleQuestion
  extends Question
{
  private final List<Titles.Title> titlelist = new LinkedList();
  
  public TitleQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget)
  {
    super(aResponder, aTitle, aQuestion, 39, aTarget);
  }
  
  public void answer(Properties answers)
  {
    setAnswer(answers);
    QuestionParser.parseTitleQuestion(this);
  }
  
  public void sendQuestion()
  {
    StringBuilder sb = new StringBuilder(getBmlHeader());
    
    final boolean isMale = ((Player)getResponder()).isNotFemale();
    Titles.Title[] titles = ((Player)getResponder()).getTitles();
    Arrays.sort(titles, new Comparator()
    {
      public int compare(Titles.Title t1, Titles.Title t2)
      {
        return t1.getName(isMale).compareTo(t2.getName(isMale));
      }
    });
    String suff = "";
    String pre = "";
    if (!getResponder().hasFlag(24)) {
      pre = getResponder().getAbilityTitle();
    }
    if ((getResponder().getCultist() != null) && (!getResponder().hasFlag(25))) {
      suff = suff + " " + getResponder().getCultist().getCultistTitleShort();
    }
    Titles.Title lTempTitle = getResponder().getTitle();
    if (getResponder().isKing()) {
      suff = suff + " [" + King.getRulerTitle(getResponder().getSex() == 0, getResponder().getKingdomId()) + "]";
    }
    if (lTempTitle != null) {
      if (lTempTitle.isRoyalTitle())
      {
        if ((getResponder().getAppointments() != 0L) || (getResponder().isAppointed())) {
          suff = suff + " [" + getResponder().getKingdomTitle() + "]";
        }
      }
      else {
        suff = suff + " [" + lTempTitle.getName(getResponder().isNotFemale()) + "]";
      }
    }
    if ((getResponder().isChampion()) && (getResponder().getDeity() != null)) {
      suff = suff + " [Champion of " + getResponder().getDeity().name + "]";
    }
    String playerName = pre + StringUtilities.raiseFirstLetterOnly(getResponder().getName()) + suff;
    sb.append("text{text=\"You are currently known as: " + playerName + "\"}");
    sb.append("text{text=\"\"}");
    
    Titles.Title currentTitle = ((Player)getResponder()).getTitle();
    int totalTitles = 0;
    if (titles.length == 0)
    {
      if ((getResponder().getAppointments() != 0L) || (getResponder().isAppointed()))
      {
        int defaultTitle = 0;
        sb.append("harray{text{text=\"Title: \"};dropdown{id=\"TITLE\";options=\"None");
        
        sb.append(",");
        sb.append(Titles.Title.Kingdomtitle.getName(getResponder().isNotFemale()));
        this.titlelist.add(Titles.Title.Kingdomtitle);
        if (currentTitle != null) {
          if (currentTitle.isRoyalTitle()) {
            defaultTitle = 1;
          }
        }
        sb.append("\";default=\"" + defaultTitle + "\"}}");
        totalTitles++;
      }
      else
      {
        sb.append("text{text=\"You have no titles to select from.\"}");
      }
    }
    else
    {
      int defaultTitle = 0;
      
      sb.append("harray{text{text=\"Title: \"};dropdown{id=\"TITLE\";options=\"None");
      for (int x = 0; x < titles.length; x++)
      {
        sb.append(",");
        
        sb.append(titles[x].getName(getResponder().isNotFemale()));
        if (currentTitle != null) {
          if (titles[x].id == currentTitle.id) {
            defaultTitle = x + 1;
          }
        }
        this.titlelist.add(titles[x]);
        totalTitles++;
      }
      if ((getResponder().getAppointments() != 0L) || (getResponder().isAppointed()))
      {
        sb.append(",");
        sb.append(Titles.Title.Kingdomtitle.getName(getResponder().isNotFemale()));
        this.titlelist.add(Titles.Title.Kingdomtitle);
        if (currentTitle != null) {
          if (currentTitle.isRoyalTitle()) {
            defaultTitle = titles.length + 1;
          }
        }
        totalTitles++;
      }
      sb.append("\";default=\"" + defaultTitle + "\"}}");
      sb.append("text{text=\"\"}");
      sb.append("text{text=\"You have a total of " + totalTitles + " titles.\"}");
      sb.append("text{text=\"\"}");
      sb.append("text{type=\"italic\";text=\"Note: Armour smiths that use their title gets faster armour improvement rate.\"}");
    }
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
  
  Titles.Title getTitle(int aPosition)
  {
    return (Titles.Title)this.titlelist.get(aPosition);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\TitleQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */