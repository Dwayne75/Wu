package com.wurmonline.server.questions;

import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.VoteServer;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerVote;
import com.wurmonline.server.players.PlayerVotes;
import com.wurmonline.server.support.Tickets;
import com.wurmonline.server.support.VoteQuestion;
import com.wurmonline.server.support.VoteQuestions;
import com.wurmonline.server.webinterface.WcVoting;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InGameVoteQuestion
  extends Question
{
  private static final Logger logger = Logger.getLogger(InGameVoteQuestion.class.getName());
  private static final byte SHOWLIST = 0;
  private static final byte EDITQUESTION = 2;
  private static final byte VIEWQUESTION = 3;
  private static final byte SHOWRESULTS = 4;
  private byte part = 0;
  private int questionId = 0;
  
  public InGameVoteQuestion(Creature aResponder)
  {
    this(aResponder, "In game voting", "", (byte)0);
  }
  
  public InGameVoteQuestion(Creature aResponder, String aTitle, String aQuestion, byte aPart)
  {
    this(aResponder, aTitle, aQuestion, aPart, 0);
  }
  
  public InGameVoteQuestion(Creature aResponder, String aTitle, String aQuestion, byte aPart, int aQuestionId)
  {
    super(aResponder, aTitle, aQuestion, 107, aResponder.getWurmId());
    this.part = aPart;
    this.questionId = aQuestionId;
  }
  
  public void answer(Properties aAnswer)
  {
    setAnswer(aAnswer);
    if (this.type == 0)
    {
      logger.log(Level.INFO, "Received answer for a question with NOQUESTION.");
      return;
    }
    if (this.type == 107)
    {
      boolean hasPower = getResponder().getPower() >= 2;
      switch (this.part)
      {
      case 0: 
        String strId = aAnswer.getProperty("qid");
        if (strId == null) {
          return;
        }
        this.questionId = Integer.parseInt(strId);
        if (this.questionId != -1)
        {
          byte nextPart = 0;
          
          VoteQuestion vq = VoteQuestions.getVoteQuestion(this.questionId);
          Player p = (Player)getResponder();
          String header;
          String windowTitle;
          String header;
          if (((hasPower) && (vq.isActive())) || (vq.canVote(p)))
          {
            String header;
            if ((hasPower) || (p.hasVoted(this.questionId)))
            {
              nextPart = 3;
              String windowTitle = "View Question";
              header = vq.getQuestionTitle();
            }
            else
            {
              nextPart = 2;
              String windowTitle = "Vote";
              header = vq.getQuestionTitle();
            }
          }
          else
          {
            nextPart = 4;
            windowTitle = "Vote Results";
            header = vq.getQuestionTitle();
          }
          InGameVoteQuestion igvsq = new InGameVoteQuestion(getResponder(), windowTitle, header, nextPart, this.questionId);
          
          igvsq.sendQuestion();
        }
        break;
      case 2: 
        boolean opt1 = false;
        boolean opt2 = false;
        boolean opt3 = false;
        boolean opt4 = false;
        
        VoteQuestion vq = VoteQuestions.getVoteQuestion(this.questionId);
        if (vq.isAllowMultiple())
        {
          String sopt1 = aAnswer.getProperty("opt1");
          String sopt2 = aAnswer.getProperty("opt2");
          String sopt3 = aAnswer.getProperty("opt3");
          String sopt4 = aAnswer.getProperty("opt4");
          opt1 = Boolean.parseBoolean(sopt1);
          opt2 = Boolean.parseBoolean(sopt2);
          opt3 = Boolean.parseBoolean(sopt3);
          opt4 = Boolean.parseBoolean(sopt4);
        }
        else
        {
          String sopts = aAnswer.getProperty("opts");
          int ans = Integer.parseInt(sopts);
          switch (ans)
          {
          case 1: 
            opt1 = true;
            break;
          case 2: 
            opt2 = true;
            break;
          case 3: 
            opt3 = true;
            break;
          case 4: 
            opt4 = true;
            break;
          default: 
            return;
          }
        }
        if ((opt1) || (opt2) || (opt3) || (opt4))
        {
          if ((!Servers.isThisLoginServer()) && (!isLoginAvailable()))
          {
            getResponder().getCommunicator().sendNormalServerMessage("Login server is currently down, so your vote cannot be registered, please try again later.");
          }
          else
          {
            PlayerVote pv = PlayerVotes.addPlayerVote(new PlayerVote(getResponder().getWurmId(), this.questionId, opt1, opt2, opt3, opt4), true);
            
            ((Player)getResponder()).addPlayerVote(pv);
            if (!Servers.isThisLoginServer())
            {
              WcVoting wv = new WcVoting(pv);
              wv.sendToLoginServer();
            }
            getResponder().getCommunicator().sendNormalServerMessage("Your vote has been registered.");
          }
        }
        else {
          getResponder().getCommunicator().sendNormalServerMessage("You did not select anything.");
        }
        break;
      case 3: 
        break;
      }
    }
  }
  
  public void sendQuestion()
  {
    int width = 350;
    int height = 500;
    StringBuilder buf = new StringBuilder();
    buf.append(getBmlHeader());
    boolean hasPower = getResponder().getPower() >= 2;
    try
    {
      String disabled = "";
      switch (this.part)
      {
      case 0: 
        int cols = 5;
        VoteQuestion[] vqs;
        if (hasPower)
        {
          VoteQuestion[] vqs = VoteQuestions.getVoteQuestions();
          cols = 4;
        }
        else
        {
          vqs = VoteQuestions.getVoteQuestions((Player)getResponder());
        }
        if (vqs.length > 0)
        {
          buf.append("text{text=\"Select the questions to view.\"};");
          
          buf.append("table{rows=\"" + (vqs.length + 2) + "\";cols=\"5\";label{text=\"\"};label{type=\"bold\";text=\"Question\"};label{type=\"bold\";text=\"Vote Start\"};label{type=\"bold\";text=\"Vote End\"};label{type=\"bold\";text=\"" + (cols == 5 ? "Voted?" : "") + "\"};");
          for (VoteQuestion vq : vqs)
          {
            boolean voted = ((Player)getResponder()).hasVoted(vq.getQuestionId());
            
            String colour = "";
            if (vq.getSent() == 2)
            {
              if (voted) {
                colour = "color=\"255,255,100\";";
              } else {
                colour = "color=\"100,255,100\";";
              }
            }
            else if (vq.hasSummary()) {
              colour = "color=\"100,100,255\";";
            }
            String strVoted = voted ? "Yes" : "No";
            buf.append((colour.length() == 0 ? "label{text=\"\"}" : new StringBuilder().append("radio{group=\"qid\";id=\"")
              .append(vq.getQuestionId()).append("\"};").toString()) + "label{" + colour + "text=\"" + vq
              .getQuestionTitle() + "\"};label{" + colour + "text=\"" + 
              Tickets.convertTime(vq.getVoteStart()) + "\"};label{" + colour + "text=\"" + 
              Tickets.convertTime(vq.getVoteEnd() - 1000L) + "\"};label{" + colour + "text=\"" + (cols == 5 ? strVoted : "") + "\"};");
          }
          buf.append("radio{group=\"qid\";id=\"-1\";selected=\"true\";hidden=\"true\"};label{text=\"\"};label{text=\"\"};label{text=\"\"};label{text=\"\"};");
          
          buf.append("}");
        }
        else
        {
          buf.append("text{type=\"bold\";text=\"No polls available at the moment.\"};");
        }
        width = 350;
        height = 140 + vqs.length * 16;
        break;
      case 3: 
        disabled = ";enabled=\"false\"";
      case 2: 
        width = 300;
        height = 200;
        VoteQuestion vq = VoteQuestions.getVoteQuestion(this.questionId);
        PlayerVote pv = PlayerVotes.getPlayerVoteByQuestion(getResponder().getWurmId(), this.questionId);
        if ((this.part == 3) && (!hasPower)) {
          if (vq.hasEnded()) {
            buf.append("label{type=\"bolditalic\";color=\"255,100,100\";text=\"Voting has ended so cannot amend\"}");
          } else {
            buf.append("label{type=\"bolditalic\";color=\"255,255,100\";text=\"Already voted and therefore cannot amend\"}");
          }
        }
        String optType = "";
        buf.append("text{color=\"255,150,0\";type=\"bold\";text=\"" + vq.getQuestionText() + "\"};");
        if (vq.isAllowMultiple())
        {
          optType = "checkbox{id=\"opt";
        }
        else
        {
          optType = "radio{group=\"opts\";id=\"";
          buf.append(optType + "0\";text=\"" + vq.getOption1Text() + "\";selected=\"true\";hidden=\"true\"}");
        }
        String selected = "";
        selected = (pv != null) && (pv.getOption1()) ? ";selected=\"true\"" : "";
        buf.append(optType + "1\";text=\"" + vq.getOption1Text() + "\"" + selected + disabled + "}");
        selected = (pv != null) && (pv.getOption2()) ? ";selected=\"true\"" : "";
        buf.append(optType + "2\";text=\"" + vq.getOption2Text() + "\"" + selected + disabled + "}");
        if (vq.getOption3Text().length() > 0)
        {
          selected = (pv != null) && (pv.getOption3()) ? ";selected=\"true\"" : "";
          buf.append(optType + "3\";text=\"" + vq.getOption3Text() + "\"" + selected + disabled + "}");
          height += 16;
        }
        if (vq.getOption4Text().length() > 0)
        {
          selected = (pv != null) && (pv.getOption4()) ? ";selected=\"true\"" : "";
          buf.append(optType + "4\";text=\"" + vq.getOption4Text() + "\"" + selected + disabled + "}");
          height += 16;
        }
        if (hasPower)
        {
          if (vq.isPremOnly()) {
            buf.append("label{text=\"Prem-Only can vote.\"}");
          } else {
            buf.append("label{text=\"Everyone can vote.\"}");
          }
          if (vq.isAllowMultiple()) {
            buf.append("label{text=\"Multiple selections allowed.\"}");
          } else {
            buf.append("label{text=\"Single selection only.\"}");
          }
          buf.append("label{text=\"Kingdoms allowed:");
          int len = 0;
          int cnt = 1;
          if (vq.isJK()) {
            len++;
          }
          if (vq.isMR()) {
            len++;
          }
          if (vq.isHots()) {
            len++;
          }
          if (vq.isFreedom()) {
            len++;
          }
          String comma = "";
          if (vq.isJK())
          {
            buf.append(comma + "JK");
            cnt++;
            if (cnt < len) {
              comma = ", ";
            } else {
              comma = " and ";
            }
          }
          if (vq.isMR())
          {
            buf.append(comma + "MR");
            cnt++;
            if (cnt < len) {
              comma = ", ";
            } else {
              comma = " and ";
            }
          }
          if (vq.isHots())
          {
            buf.append(comma + "Hots");
            cnt++;
            if (cnt < len) {
              comma = ", ";
            } else {
              comma = " and ";
            }
          }
          if (vq.isFreedom()) {
            buf.append(comma + "Freedom");
          }
          buf.append("\"}");
          if (vq.getServers().isEmpty())
          {
            buf.append("label{text=\"List of Servers is only available on Login Server.\"}");
          }
          else
          {
            buf.append("label{text=\"Servers:");
            len = vq.getServers().size();
            cnt = 1;
            comma = "";
            for (VoteServer vs : vq.getServers())
            {
              ServerEntry se = Servers.getServerWithId(vs.getServerId());
              buf.append(comma + se.getAbbreviation());
              cnt++;
              if (cnt < len) {
                comma = ", ";
              } else {
                comma = " and ";
              }
            }
            buf.append("\"}");
          }
          height += 32;
        }
        else if (vq.isAllowMultiple())
        {
          buf.append("label{color=\"0,255,255\";type=\"italic\";text=\"Multiple selections are allowed.\"}");
        }
        else
        {
          buf.append("label{color=\"0,255,255\";type=\"italic\";text=\"Only one selection is allowed.\"}");
        }
        buf.append("text{type=\"bold\";text=\"Note: Votes will be used as guidelines to assess player sentiment. There is no guarantee that any majority vote will result in the implementation or removal of anything.\"}");
        
        height += 32;
        break;
      case 4: 
        VoteQuestion vq1 = VoteQuestions.getVoteQuestion(this.questionId);
        int rows = 3;
        if (vq1.getOption3Text().length() > 0) {
          rows++;
        }
        if (vq1.getOption4Text().length() > 0) {
          rows++;
        }
        buf.append("table{rows=\"" + rows + "\";cols=\"2\";");
        buf.append("label{text=\"" + vq1.getOption1Text() + "\"}label{text=\"" + vq1
          .getOption1Count() + "\"}");
        buf.append("label{text=\"" + vq1.getOption2Text() + "\"}label{text=\"" + vq1
          .getOption2Count() + "\"}");
        if (vq1.getOption3Text().length() > 0) {
          buf.append("label{text=\"" + vq1.getOption3Text() + "\"}label{text=\"" + vq1
            .getOption3Count() + "\"}");
        }
        if (vq1.getOption4Text().length() > 0) {
          buf.append("label{text=\"" + vq1.getOption4Text() + "\"}label{text=\"" + vq1
            .getOption4Count() + "\"}");
        }
        buf.append("label{text=\"Total Players who voted\"}label{text=\"" + vq1
          .getVoteCount() + "\"}");
        buf.append("}");
        buf.append("text{text=\"\"}");
      }
    }
    catch (Exception e)
    {
      logger.log(Level.WARNING, e.getMessage(), e);
      getResponder().getCommunicator().sendNormalServerMessage("Exception:" + e.getMessage());
    }
    buf.append(createAnswerButton2());
    
    getResponder().getCommunicator().sendBml(width, height, true, true, buf.toString(), 200, 200, 200, this.title);
  }
  
  private boolean isLoginAvailable()
  {
    if (Servers.isThisLoginServer()) {
      return true;
    }
    return Servers.loginServer.isAvailable(5, true);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\InGameVoteQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */