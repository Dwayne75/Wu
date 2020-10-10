package com.wurmonline.server.questions;

import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Questions
  implements TimeConstants
{
  private static Map<Integer, Question> questions = new HashMap();
  private static Logger logger = Logger.getLogger(Questions.class.getName());
  
  static void addQuestion(Question question)
  {
    questions.put(Integer.valueOf(question.getId()), question);
    Question lastQuestion = ((Player)question.getResponder()).getCurrentQuestion();
    if (lastQuestion != null) {
      lastQuestion.timedOut();
    }
    ((Player)question.getResponder()).setQuestion(question);
  }
  
  public static Question getQuestion(int id)
    throws NoSuchQuestionException
  {
    Integer iid = Integer.valueOf(id);
    Question question = (Question)questions.get(iid);
    if (question == null) {
      throw new NoSuchQuestionException(String.valueOf(id));
    }
    return question;
  }
  
  public static final int getNumUnanswered()
  {
    return questions.size();
  }
  
  public static void removeQuestion(Question question)
  {
    if (question != null)
    {
      Integer iid = Integer.valueOf(question.getId());
      questions.remove(iid);
    }
  }
  
  public static void removeQuestions(Player player)
  {
    Question[] quests = (Question[])questions.values().toArray(new Question[questions.values().size()]);
    for (int x = 0; x < quests.length; x++) {
      if (quests[x].getResponder() == player)
      {
        quests[x].clearResponder();
        questions.remove(Integer.valueOf(quests[x].getId()));
      }
    }
  }
  
  public static void trimQuestions()
  {
    long now = System.currentTimeMillis();
    Set<Question> toRemove = new HashSet();
    for (Question lQuestion : questions.values())
    {
      long maxTime = 900000L;
      if ((lQuestion instanceof CultQuestion)) {
        maxTime = 1800000L;
      }
      if ((lQuestion instanceof SpawnQuestion)) {
        maxTime = 7200000L;
      }
      if (!(lQuestion instanceof SelectSpawnQuestion)) {
        if ((now - lQuestion.getSendTime() > maxTime) || (!lQuestion.getResponder().hasLink())) {
          toRemove.add(lQuestion);
        }
      }
    }
    for (Question lQuestion : toRemove)
    {
      lQuestion.timedOut();
      removeQuestion(lQuestion);
      if (lQuestion.getResponder().isPlayer()) {
        if (((Player)lQuestion.getResponder()).question == lQuestion) {
          ((Player)lQuestion.getResponder()).question = null;
        }
      }
    }
    if ((logger.isLoggable(Level.FINER)) && (questions.size() > 0)) {
      logger.finer("Size of question list=" + questions.size());
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\questions\Questions.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */