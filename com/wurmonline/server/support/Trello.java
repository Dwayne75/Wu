package com.wurmonline.server.support;

import com.wurmonline.server.Constants;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.VoteServer;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.net.ssl.HttpsURLConnection;

public final class Trello
{
  private static Logger logger = Logger.getLogger(Trello.class.getName());
  private static final TrelloThread trelloThread = new TrelloThread();
  private static final ConcurrentLinkedDeque<TrelloCard> trelloCardQueue = new ConcurrentLinkedDeque();
  private static int tickerCount = 0;
  private static final String[] trelloLists = { "None", "Waiting GM Calls", "Waiting ARCH or Dev action", "Resolved/Cancelled", "Watching", "Feedback" };
  private static String[] trelloListIds = { "", "", "", "", "", "" };
  private static String trelloFeedbackTemplateCardId = "";
  private static final String trelloMutes = "Mutes";
  private static final String trelloMutewarns = "Mutewarns";
  private static final String trelloVotings = "Voting";
  private static final String trelloHighways = "Highways";
  private static final String trelloDeaths = "Deaths";
  private static String trelloMuteIds = "";
  private static String trelloMutewarnIds = "";
  private static String trelloVotingIds = "";
  private static String trelloHighwaysIds = "";
  private static String trelloDeathsIds = "";
  private static boolean trelloMuteStorage = false;
  public static final byte LIST_NONE = 0;
  public static final byte LIST_WAITING_GM = 1;
  public static final byte LIST_WAITING_ARCH = 2;
  public static final byte LIST_CLOSED = 3;
  public static final byte LIST_WATCHING = 4;
  public static final byte LIST_FEEDBACK = 5;
  private static final String METHOD_GET = "GET";
  private static final String METHOD_POST = "POST";
  private static final String METHOD_PUT = "PUT";
  private static final String GZIP_ENCODING = "gzip";
  private static final String HTTP_CHARACTER_ENCODING = "UTF-8";
  
  public static final TrelloThread getTrelloThread()
  {
    return trelloThread;
  }
  
  private static final class TrelloThread
    implements Runnable
  {
    public void run()
    {
      if (Trello.logger.isLoggable(Level.FINEST)) {
        Trello.logger.finest("Running newSingleThreadScheduledExecutor for calling Tickets.ticker()");
      }
      try
      {
        long now = System.nanoTime();
        if ((Servers.isThisLoginServer()) && (Constants.trelloApiKey.length() > 0))
        {
          Trello.access$100();
          
          Trello.access$200();
        }
        float lElapsedTime = (float)(System.nanoTime() - now) / 1000000.0F;
        if (lElapsedTime > (float)Constants.lagThreshold) {
          Trello.logger.info("Finished calling Tickets.ticker(), which took " + lElapsedTime + " millis.");
        }
      }
      catch (RuntimeException e)
      {
        Trello.logger.log(Level.WARNING, "Caught exception in ScheduledExecutorService while calling Tickets.ticker()", e);
        
        throw e;
      }
    }
  }
  
  public static void addHighwayMessage(String server, String title, String description)
  {
    if (Constants.trelloMVBoardId.length() == 0) {
      return;
    }
    StringBuilder buf = new StringBuilder();
    buf.append(Tickets.convertTime(System.currentTimeMillis()));
    buf.append(" (");
    buf.append(server);
    buf.append(") ");
    buf.append(title);
    String tList = trelloHighwaysIds;
    
    TrelloCard tc = new TrelloCard(Constants.trelloMVBoardId, tList, buf.toString(), description, "");
    trelloCardQueue.add(tc);
  }
  
  public static void addImportantDeathsMessage(String server, String title, String description)
  {
    if (Constants.trelloMVBoardId.length() == 0) {
      return;
    }
    StringBuilder buf = new StringBuilder();
    buf.append(Tickets.convertTime(System.currentTimeMillis()));
    buf.append(" (");
    buf.append(server);
    buf.append(") ");
    buf.append(title);
    String tList = trelloDeathsIds;
    
    TrelloCard tc = new TrelloCard(Constants.trelloMVBoardId, tList, buf.toString(), description, "");
    trelloCardQueue.add(tc);
  }
  
  public static void addMessage(String sender, String playerName, String reason, int hours)
  {
    if (Constants.trelloMVBoardId.length() == 0) {
      return;
    }
    StringBuilder buf = new StringBuilder();
    buf.append(Tickets.convertTime(System.currentTimeMillis()) + " " + sender + " ");
    
    String tList = "";
    String tLabel = "";
    if (hours == 0)
    {
      tList = trelloMutewarnIds;
      buf.append("Mutewarn " + playerName);
      
      tLabel = "Orange";
    }
    else if (reason.length() == 0)
    {
      tList = trelloMuteIds;
      buf.append("Unmute " + playerName);
      tLabel = "Unmute";
    }
    else
    {
      tList = trelloMuteIds;
      buf.append("Mute " + playerName + " for " + hours + " hour" + (hours == 1 ? " " : "s "));
      tLabel = "Mute";
    }
    TrelloCard tc = new TrelloCard(Constants.trelloMVBoardId, tList, buf.toString(), reason, tLabel);
    trelloCardQueue.add(tc);
  }
  
  private static void updateTicketsInTrello()
  {
    if (trelloListIds[1].length() == 0)
    {
      obtainListIds();
      return;
    }
    try
    {
      for (Ticket ticket : Tickets.getDirtyTickets())
      {
        if (ticket.getTrelloCardId().length() == 0) {
          createCard(ticket);
        } else {
          updateCard(ticket);
        }
        for (TicketAction ta : ticket.getDirtyTicketActions()) {
          addAction(ticket, ta);
        }
        if ((ticket.hasFeedback()) && (ticket.getTrelloFeedbackCardId().length() == 0)) {
          createFeedbackCard(ticket);
        }
      }
      for (Ticket ticket : Tickets.getArchiveTickets())
      {
        if (ticket.getTrelloCardId().length() == 0) {
          createCard(ticket);
        }
        archiveCard(ticket);
      }
      tickerCount = 0;
    }
    catch (RuntimeException e)
    {
      if (tickerCount % 10 == 0) {
        logger.log(Level.INFO, "Problem communicating with Trello " + (tickerCount + 1) + " times.", e);
      }
      if (tickerCount >= 1000) {
        throw e;
      }
      tickerCount += 1;
    }
  }
  
  private static void updateMuteVoteInTrello()
  {
    if (!trelloMuteStorage)
    {
      obtainMVListIds();
      return;
    }
    try
    {
      for (VoteQuestion vq : VoteQuestions.getFinishedQuestions()) {
        if (vq.getTrelloCardId().length() == 0) {
          createCard(vq);
        }
      }
      for (VoteQuestion vq : VoteQuestions.getArchiveVoteQuestions())
      {
        if (vq.getTrelloCardId().length() == 0) {
          createCard(vq);
        }
        archiveCard(vq);
      }
      TrelloCard card = (TrelloCard)trelloCardQueue.pollFirst();
      while (card != null)
      {
        createCard(card);
        card = (TrelloCard)trelloCardQueue.pollFirst();
      }
      tickerCount = 0;
    }
    catch (RuntimeException e)
    {
      if (tickerCount % 10 == 0) {
        logger.log(Level.INFO, "Problem communicating with Trello " + (tickerCount + 1) + " times.", e);
      }
      if (tickerCount >= 1000) {
        throw e;
      }
      tickerCount += 1;
    }
  }
  
  private static void archiveCardsInList(String trelloList, String listName)
  {
    long archiveTime = System.currentTimeMillis() - (Servers.isThisATestServer() ? 604800000L : 2419200000L);
    int archived = 0;
    
    String lurl = TrelloURL.make("https://api.trello.com/1/lists/{0}/cards", new String[] { trelloList });
    Map<String, String> argumentsMap = new HashMap();
    argumentsMap.put("fields", "id");
    JSONArray lja = doGetArray(lurl, argumentsMap);
    for (int x = 0; x < lja.length(); x++)
    {
      JSONObject jo = lja.getJSONObject(x);
      String id = jo.getString("id");
      
      String createdDateHex = id.substring(0, 8);
      long dms = Long.parseLong(createdDateHex, 16) * 1000L;
      if (archiveTime > dms) {
        if (archiveCard(id)) {
          archived++;
        }
      }
    }
    if (archived > 0) {
      logger.log(Level.INFO, "Archived " + archived + " " + listName + " cards in Trello");
    }
  }
  
  private static void addAction(Ticket ticket, TicketAction ta)
  {
    try
    {
      String url = TrelloURL.make("https://api.trello.com/1/cards/{0}/actions/comments", new String[] { ticket.getTrelloCardId() });
      ta.setTrelloCommentId(addComment(url, ta.getTrelloComment()));
    }
    catch (TrelloCardNotFoundException tcnfe)
    {
      ta.setTrelloCommentId("Failed");
      
      logger.log(Level.WARNING, tcnfe.getMessage(), tcnfe);
    }
  }
  
  private static String addComment(String url, String text)
    throws TrelloCardNotFoundException
  {
    Map<String, String> keyValueMap = new HashMap();
    keyValueMap.put("text", text);
    
    JSONObject jo = doPost(url, keyValueMap);
    return jo.get("id").toString();
  }
  
  private static void updateCard(Ticket ticket)
  {
    try
    {
      String cardId = ticket.getTrelloCardId();
      if (ticket.hasSummaryChanged()) {
        updateCard(TrelloURL.make("https://api.trello.com/1/cards/{0}/name", new String[] { cardId }), ticket.getTrelloName());
      }
      if (ticket.hasDescriptionChanged()) {
        updateCard(TrelloURL.make("https://api.trello.com/1/cards/{0}/desc", new String[] { cardId }), ticket.getDescription());
      }
      if (ticket.hasListChanged()) {
        updateCard(TrelloURL.make("https://api.trello.com/1/cards/{0}/idList", new String[] { cardId }), trelloListIds[ticket.getTrelloListCode()]);
      }
      Tickets.setTicketIsDirty(ticket, false);
    }
    catch (TrelloCardNotFoundException tcnfe)
    {
      Tickets.setTicketIsDirty(ticket, false);
      
      logger.log(Level.WARNING, tcnfe.getMessage(), tcnfe);
    }
  }
  
  private static boolean archiveCard(String cardId)
  {
    try
    {
      updateCard(TrelloURL.make("https://api.trello.com/1/cards/{0}/closed", new String[] { cardId }), "true");
      return true;
    }
    catch (TrelloCardNotFoundException tcnfe)
    {
      logger.log(Level.WARNING, tcnfe.getMessage(), tcnfe);
    }
    return false;
  }
  
  private static void archiveCard(VoteQuestion voteQuestion)
  {
    try
    {
      updateCard(TrelloURL.make("https://api.trello.com/1/cards/{0}/closed", new String[] { voteQuestion.getTrelloCardId() }), "true");
    }
    catch (TrelloCardNotFoundException tcnfe)
    {
      logger.log(Level.WARNING, tcnfe.getMessage(), tcnfe);
    }
    VoteQuestions.queueSetArchiveState(voteQuestion.getQuestionId(), (byte)3);
  }
  
  private static void archiveCard(Ticket ticket)
  {
    try
    {
      updateCard(TrelloURL.make("https://api.trello.com/1/cards/{0}/closed", new String[] { ticket.getTrelloCardId() }), "true");
      if ((ticket.hasFeedback()) && (ticket.getTrelloFeedbackCardId().length() > 0)) {
        updateCard(TrelloURL.make("https://api.trello.com/1/cards/{0}/closed", new String[] { ticket.getTrelloFeedbackCardId() }), "true");
      }
    }
    catch (TrelloCardNotFoundException tcnfe)
    {
      logger.log(Level.WARNING, tcnfe.getMessage(), tcnfe);
    }
    Tickets.setTicketArchiveState(ticket, (byte)3);
  }
  
  private static void updateCard(String url, String value)
    throws TrelloCardNotFoundException
  {
    Map<String, String> keyValueMap = new HashMap();
    keyValueMap.put("value", value);
    doPut(url, keyValueMap);
  }
  
  private static void createCard(Ticket ticket)
  {
    try
    {
      String url = TrelloURL.make("https://api.trello.com/1/cards", new String[0]);
      Map<String, String> keyValueMap = new HashMap();
      keyValueMap.put("name", ticket.getTrelloName());
      keyValueMap.put("desc", ticket.getDescription());
      keyValueMap.put("idList", trelloListIds[ticket.getTrelloListCode()]);
      
      JSONObject jo = doPost(url, keyValueMap);
      
      String shortLink = getShortLink(jo.getString("shortUrl"));
      Tickets.setTicketTrelloCardId(ticket, shortLink);
    }
    catch (TrelloCardNotFoundException tcnfe)
    {
      logger.log(Level.WARNING, tcnfe.getMessage(), tcnfe);
    }
  }
  
  private static void createCard(TrelloCard card)
  {
    try
    {
      String url = TrelloURL.make("https://api.trello.com/1/cards", new String[0]);
      Map<String, String> keyValueMap = new HashMap();
      keyValueMap.put("name", card.getTitle());
      keyValueMap.put("desc", card.getDescription());
      
      keyValueMap.put("idList", card.getListId());
      
      JSONObject localJSONObject = doPost(url, keyValueMap);
    }
    catch (TrelloCardNotFoundException tcnfe)
    {
      logger.log(Level.WARNING, tcnfe.getMessage(), tcnfe);
    }
  }
  
  private static void createCard(VoteQuestion question)
  {
    try
    {
      String name = Tickets.convertTime(question.getVoteStart()) + " " + question.getQuestionTitle() + " (" + Tickets.convertTime(question.getVoteEnd()) + ")";
      String desc = question.getQuestionText();
      String url = TrelloURL.make("https://api.trello.com/1/cards", new String[0]);
      Map<String, String> keyValueMap = new HashMap();
      keyValueMap.put("name", name);
      keyValueMap.put("desc", desc);
      keyValueMap.put("idList", trelloVotingIds);
      
      JSONObject jo = doPost(url, keyValueMap);
      String shortLink = getShortLink(jo.getString("shortUrl"));
      
      addVoteQuestionDetails(question, shortLink);
      
      VoteQuestions.queueSetTrelloCardId(question.getQuestionId(), shortLink);
    }
    catch (TrelloCardNotFoundException tcnfe)
    {
      logger.log(Level.WARNING, tcnfe.getMessage(), tcnfe);
    }
  }
  
  private static void addVoteQuestionDetails(VoteQuestion question, String shortLink)
  {
    try
    {
      String url = TrelloURL.make("https://api.trello.com/1/cards/{0}/actions/comments", new String[] { shortLink });
      StringBuilder buf = new StringBuilder();
      buf.append("SUMMARY\n\n");
      buf.append(getOptionSummary(question.getOption1Text(), question.getOption1Count(), question.getVoteCount()));
      buf.append(getOptionSummary(question.getOption2Text(), question.getOption2Count(), question.getVoteCount()));
      buf.append(getOptionSummary(question.getOption3Text(), question.getOption3Count(), question.getVoteCount()));
      buf.append(getOptionSummary(question.getOption4Text(), question.getOption4Count(), question.getVoteCount()));
      buf.append("\nTotal Players Voted: " + question.getVoteCount());
      
      String reply = addComment(url, buf.toString());
      
      buf = new StringBuilder();
      buf.append("OPTIONS\n");
      buf.append("\nVote Start: " + Tickets.convertTime(question.getVoteStart()));
      buf.append("\nVote End: " + Tickets.convertTime(question.getVoteEnd()));
      buf.append("\nAllow Multiple: " + question.isAllowMultiple());
      buf.append("\nPrem Only: " + question.isPremOnly());
      buf.append("\nJK: " + question.isJK());
      buf.append("\nMR: " + question.isMR());
      buf.append("\nHots: " + question.isHots());
      buf.append("\nFreedom: " + question.isFreedom());
      reply = addComment(url, buf.toString());
      
      buf = new StringBuilder();
      buf.append("SERVERS\n");
      for (VoteServer vs : question.getServers()) {
        buf.append("\n" + Servers.getServerWithId(vs.getServerId()).name);
      }
      reply = addComment(url, buf.toString());
    }
    catch (TrelloCardNotFoundException tcnfe)
    {
      logger.log(Level.WARNING, tcnfe.getMessage(), tcnfe);
    }
  }
  
  private static String getOptionSummary(String text, int count, int total)
  {
    if (text.length() == 0) {
      return "";
    }
    int perc = -1;
    String percText = " (Nan%)";
    if (total > 0)
    {
      perc = count * 100 / total;
      percText = " (" + perc + "%)";
    }
    StringBuilder buf = new StringBuilder();
    buf.append(text + " [" + count + percText + "]\n");
    return buf.toString();
  }
  
  private static void createFeedbackCard(Ticket ticket)
  {
    try
    {
      String url = TrelloURL.make("https://api.trello.com/1/cards", new String[0]);
      Map<String, String> keyValueMap = new HashMap();
      keyValueMap.put("name", ticket.getTrelloFeedbackTitle());
      keyValueMap.put("idList", trelloListIds[5]);
      keyValueMap.put("idCardSource", trelloFeedbackTemplateCardId);
      JSONObject fjo = doPost(url, keyValueMap);
      
      String shortLink = getShortLink(fjo.getString("shortUrl"));
      
      updateCard(TrelloURL.make("https://api.trello.com/1/cards/{0}/desc", new String[] { shortLink }), ticket.getFeedbackText());
      tickSelected(ticket, shortLink);
      
      Tickets.setTicketTrelloFeedbackCardId(ticket, shortLink);
    }
    catch (TrelloCardNotFoundException tcnfe)
    {
      logger.log(Level.WARNING, tcnfe.getMessage(), tcnfe);
    }
  }
  
  private static void tickSelected(Ticket ticket, String cardId)
  {
    try
    {
      String curl = TrelloURL.make("https://api.trello.com/1/cards/{0}/checklists", new String[] { cardId });
      Map<String, String> argumentsMap = new HashMap();
      argumentsMap.put("card_fields", "checkItemStates,idChecklists,name");
      argumentsMap.put("checkItem_fields", "name");
      argumentsMap.put("fields", "name");
      JSONArray ja = doGetArray(curl, argumentsMap);
      
      String[] nService = { "Quality Of Service", "Superior", "Good", "Average", "Fair", "Poor" };
      String[] nCourteous = { "Courteous", "Strongly Agree", "Somewhat Agree", "Neutral", "Somewhat Disagree", "Strongly Disagree" };
      String[] nKnowledgeable = { "Knowledgeable", "Strongly Agree", "Somewhat Agree", "Neutral", "Somewhat Disagree", "Strongly Disagree" };
      String[] nGeneral = { "General", "Wrong Info", "No Understand", "Unclear", "No Solve", "Disorganized", "Other", "Fine" };
      String[] nQuality = { "Quality", "Patient", "Enthusiastic", "Listened", "Friendly", "Responsive", "Nothing" };
      String[] nIrked = { "Irked", "Patient", "Enthusiastic", "Listened", "Friendly", "Responsive", "Nothing" };
      
      String[] idService = { "", "", "", "", "", "" };
      String[] idCourteous = { "", "", "", "", "", "" };
      String[] idKnowledgeable = { "", "", "", "", "", "" };
      String[] idGeneral = { "", "", "", "", "", "", "", "" };
      String[] idQuality = { "", "", "", "", "", "", "" };
      String[] idIrked = { "", "", "", "", "", "", "" };
      
      idService[0] = ja.getJSONObject(nService[0]).getString("id");
      for (int i = 1; i < idService.length; i++) {
        idService[i] = ja.getJSONObject(nService[0]).getJSONArray("checkItems").getJSONObject(nService[i]).getString("id");
      }
      idCourteous[0] = ja.getJSONObject(nCourteous[0]).getString("id");
      for (int i = 1; i < idCourteous.length; i++) {
        idCourteous[i] = ja.getJSONObject(nCourteous[0]).getJSONArray("checkItems").getJSONObject(nCourteous[i]).getString("id");
      }
      idKnowledgeable[0] = ja.getJSONObject(nKnowledgeable[0]).getString("id");
      for (int i = 1; i < idKnowledgeable.length; i++) {
        idKnowledgeable[i] = ja.getJSONObject(nKnowledgeable[0]).getJSONArray("checkItems").getJSONObject(nKnowledgeable[i]).getString("id");
      }
      idGeneral[0] = ja.getJSONObject(nGeneral[0]).getString("id");
      for (int i = 1; i < idGeneral.length; i++) {
        idGeneral[i] = ja.getJSONObject(nGeneral[0]).getJSONArray("checkItems").getJSONObject(nGeneral[i]).getString("id");
      }
      idQuality[0] = ja.getJSONObject(nQuality[0]).getString("id");
      for (int i = 1; i < idQuality.length; i++) {
        idQuality[i] = ja.getJSONObject(nQuality[0]).getJSONArray("checkItems").getJSONObject(nQuality[i]).getString("id");
      }
      idIrked[0] = ja.getJSONObject(nIrked[0]).getString("id");
      for (int i = 1; i < idIrked.length; i++) {
        idIrked[i] = ja.getJSONObject(nIrked[0]).getJSONArray("checkItems").getJSONObject(nIrked[i]).getString("id");
      }
      TicketAction ta = ticket.getFeedback();
      if (ta.wasServiceSuperior()) {
        tick(cardId, idService[0], idService[1]);
      }
      if (ta.wasServiceGood()) {
        tick(cardId, idService[0], idService[2]);
      }
      if (ta.wasServiceAverage()) {
        tick(cardId, idService[0], idService[3]);
      }
      if (ta.wasServiceFair()) {
        tick(cardId, idService[0], idService[4]);
      }
      if (ta.wasServicePoor()) {
        tick(cardId, idService[0], idService[5]);
      }
      if (ta.wasCourteousStronglyAgree()) {
        tick(cardId, idCourteous[0], idCourteous[1]);
      }
      if (ta.wasCourteousSomewhatAgree()) {
        tick(cardId, idCourteous[0], idCourteous[2]);
      }
      if (ta.wasCourteousNeutral()) {
        tick(cardId, idCourteous[0], idCourteous[3]);
      }
      if (ta.wasCourteousSomewhatDisagree()) {
        tick(cardId, idCourteous[0], idCourteous[4]);
      }
      if (ta.wasCourteousStronglyDisagree()) {
        tick(cardId, idCourteous[0], idCourteous[5]);
      }
      if (ta.wasKnowledgeableStronglyAgree()) {
        tick(cardId, idKnowledgeable[0], idKnowledgeable[1]);
      }
      if (ta.wasKnowledgeableSomewhatAgree()) {
        tick(cardId, idKnowledgeable[0], idKnowledgeable[2]);
      }
      if (ta.wasKnowledgeableNeutral()) {
        tick(cardId, idKnowledgeable[0], idKnowledgeable[3]);
      }
      if (ta.wasKnowledgeableSomewhatDisagree()) {
        tick(cardId, idKnowledgeable[0], idKnowledgeable[4]);
      }
      if (ta.wasKnowledgeableStronglyDisagree()) {
        tick(cardId, idKnowledgeable[0], idKnowledgeable[5]);
      }
      if (ta.wasGeneralWrongInfo()) {
        tick(cardId, idGeneral[0], idGeneral[1]);
      }
      if (ta.wasGeneralNoUnderstand()) {
        tick(cardId, idGeneral[0], idGeneral[2]);
      }
      if (ta.wasGeneralUnclear()) {
        tick(cardId, idGeneral[0], idGeneral[3]);
      }
      if (ta.wasGeneralNoSolve()) {
        tick(cardId, idGeneral[0], idGeneral[4]);
      }
      if (ta.wasGeneralDisorganized()) {
        tick(cardId, idGeneral[0], idGeneral[5]);
      }
      if (ta.wasGeneralOther()) {
        tick(cardId, idGeneral[0], idGeneral[6]);
      }
      if (ta.wasGeneralFine()) {
        tick(cardId, idGeneral[0], idGeneral[7]);
      }
      if (ta.wasQualityPatient()) {
        tick(cardId, idQuality[0], idQuality[1]);
      }
      if (ta.wasQualityEnthusiastic()) {
        tick(cardId, idQuality[0], idQuality[2]);
      }
      if (ta.wasQualityListened()) {
        tick(cardId, idQuality[0], idQuality[3]);
      }
      if (ta.wasQualityFriendly()) {
        tick(cardId, idQuality[0], idQuality[4]);
      }
      if (ta.wasQualityResponsive()) {
        tick(cardId, idQuality[0], idQuality[5]);
      }
      if (ta.wasQualityNothing()) {
        tick(cardId, idQuality[0], idQuality[6]);
      }
      if (ta.wasIrkedPatient()) {
        tick(cardId, idIrked[0], idIrked[1]);
      }
      if (ta.wasIrkedEnthusiastic()) {
        tick(cardId, idIrked[0], idIrked[2]);
      }
      if (ta.wasIrkedListened()) {
        tick(cardId, idIrked[0], idIrked[3]);
      }
      if (ta.wasIrkedFriendly()) {
        tick(cardId, idIrked[0], idIrked[4]);
      }
      if (ta.wasIrkedResponsive()) {
        tick(cardId, idIrked[0], idIrked[5]);
      }
      if (ta.wasIrkedNothing()) {
        tick(cardId, idIrked[0], idIrked[6]);
      }
    }
    catch (TrelloCardNotFoundException tcnfe)
    {
      logger.log(Level.WARNING, tcnfe.getMessage(), tcnfe);
    }
  }
  
  private static void tick(String cardId, String checkListId, String checkItemId)
    throws TrelloCardNotFoundException
  {
    String url = TrelloURL.make("https://api.trello.com/1/cards/{0}/checklist/{1}/checkItem/{2}/state", new String[] { cardId, checkListId, checkItemId });
    
    Map<String, String> keyValueMap = new HashMap();
    keyValueMap.put("idCheckList", checkListId);
    keyValueMap.put("idCheckItem", checkItemId);
    keyValueMap.put("value", "true");
    
    JSONObject jo = doPut(url, keyValueMap);
  }
  
  private static void obtainListIds()
  {
    String url = TrelloURL.make("https://api.trello.com/1/boards/{0}/lists", new String[] { Constants.trelloBoardid });
    JSONArray ja = doGetArray(url);
    
    int count = 0;
    for (int x = 0; x < ja.length(); x++)
    {
      JSONObject jo = ja.getJSONObject(x);
      String name = jo.getString("name");
      for (int y = 1; y <= 5; y++) {
        if (name.equalsIgnoreCase(trelloLists[y]))
        {
          trelloListIds[y] = jo.getString("id");
          count++;
          break;
        }
      }
    }
    if (count != trelloListIds.length - 1) {
      throw new JSONException("Not all the required lists found on Trello Ticket board.");
    }
    String lurl = TrelloURL.make("https://api.trello.com/1/lists/{0}/cards", new String[] { trelloListIds[5] });
    Map<String, String> argumentsMap = new HashMap();
    argumentsMap.put("fields", "name");
    argumentsMap.put("card_fields", "name");
    JSONArray lja = doGetArray(lurl, argumentsMap);
    for (int x = 0; x < lja.length(); x++)
    {
      JSONObject jo = lja.getJSONObject(x);
      String name = jo.getString("name");
      if (name.equalsIgnoreCase("Feedback Checklist Template"))
      {
        trelloFeedbackTemplateCardId = jo.getString("id");
        break;
      }
    }
    if (trelloFeedbackTemplateCardId.length() == 0) {
      throw new JSONException("Could not find the Feedback Checklist Template on Trello Ticket board.");
    }
  }
  
  private static void obtainMVListIds()
  {
    if (Constants.trelloMVBoardId.length() == 0) {
      return;
    }
    String url = TrelloURL.make("https://api.trello.com/1/boards/{0}/lists", new String[] { Constants.trelloMVBoardId });
    JSONArray ja = doGetArray(url);
    for (int x = 0; x < ja.length(); x++)
    {
      JSONObject jo = ja.getJSONObject(x);
      String name = jo.getString("name");
      if (name.equals("Mutes")) {
        trelloMuteIds = jo.getString("id");
      } else if (name.equals("Mutewarns")) {
        trelloMutewarnIds = jo.getString("id");
      } else if (name.equals("Voting")) {
        trelloVotingIds = jo.getString("id");
      } else if (name.equals("Highways")) {
        trelloHighwaysIds = jo.getString("id");
      } else if (name.equals("Deaths")) {
        trelloDeathsIds = jo.getString("id");
      }
    }
    if ((trelloMuteIds.length() == 0) || (trelloMutewarnIds.length() == 0) || (trelloVotingIds.length() == 0) || (trelloHighwaysIds.length() == 0)) {
      throw new JSONException("Not all the required lists found on Trello Mute Vote board.");
    }
    trelloMuteStorage = true;
    
    archiveCardsInList(trelloMuteIds, "Mutes");
    archiveCardsInList(trelloMutewarnIds, "Mutewarns");
    archiveCardsInList(trelloHighwaysIds, "Highways");
  }
  
  private static String getShortLink(String shortUrl)
  {
    String[] parts = shortUrl.split("/");
    String shortLink = parts[(parts.length - 1)];
    return shortLink;
  }
  
  private static JSONArray doGetArray(String url)
  {
    try
    {
      InputStream in = doRequest(url, "GET", null);
      if (in == null) {
        throw new JSONException("Failed read permissions for Trello board.");
      }
      JSONTokener tk = new JSONTokener(in);
      return new JSONArray(tk);
    }
    catch (TrelloCardNotFoundException tcnfe)
    {
      throw new JSONException("Cannot find ticket, but were not looking for one");
    }
  }
  
  private static JSONArray doGetArray(String url, Map<String, String> map)
  {
    String lurl = url;
    try
    {
      StringBuilder sb = new StringBuilder();
      boolean hasMap = (map != null) && (!map.isEmpty());
      if (hasMap)
      {
        for (String key : map.keySet())
        {
          sb.append("&");
          sb.append(URLEncoder.encode(key, "UTF-8"));
          sb.append("=");
          sb.append(URLEncoder.encode((String)map.get(key), "UTF-8"));
        }
        lurl = url + sb.toString();
      }
    }
    catch (UnsupportedEncodingException e)
    {
      logger.log(Level.WARNING, e.getMessage(), e);
    }
    try
    {
      InputStream in = doRequest(lurl, "GET", null);
      if (in == null) {
        throw new JSONException("Failed read permissions for Trello board.");
      }
      JSONTokener tk = new JSONTokener(in);
      return new JSONArray(tk);
    }
    catch (TrelloCardNotFoundException tcnfe)
    {
      throw new JSONException("Cannot find ticket, but were not looking for one");
    }
  }
  
  private static JSONObject doPut(String url, Map<String, String> map)
    throws TrelloCardNotFoundException
  {
    InputStream in = doRequest(url, "PUT", map);
    if (in == null) {
      throw new JSONException("Failed read permissions for Trello board.");
    }
    JSONTokener tk = new JSONTokener(in);
    return new JSONObject(tk);
  }
  
  private static JSONObject doPost(String url, Map<String, String> map)
    throws TrelloCardNotFoundException
  {
    InputStream in = doRequest(url, "POST", map);
    if (in == null) {
      throw new JSONException("Failed read permissions for Trello board.");
    }
    JSONTokener tk = new JSONTokener(in);
    return new JSONObject(tk);
  }
  
  private static InputStream doRequest(String url, String requestMethod, Map<String, String> map)
    throws TrelloCardNotFoundException
  {
    try
    {
      boolean hasMap = (map != null) && (!map.isEmpty());
      HttpsURLConnection conn = (HttpsURLConnection)new URL(url).openConnection();
      conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
      conn.setDoOutput((requestMethod.equals("POST")) || (requestMethod.equals("PUT")));
      conn.setRequestMethod(requestMethod);
      
      conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      
      String arguments = "";
      if (hasMap)
      {
        StringBuilder sb = new StringBuilder();
        for (String key : map.keySet())
        {
          sb.append(sb.length() > 0 ? "&" : "");
          sb.append(URLEncoder.encode(key, "UTF-8"));
          sb.append("=");
          sb.append(URLEncoder.encode((String)map.get(key), "UTF-8"));
        }
        conn.getOutputStream().write(sb.toString().getBytes());
        conn.getOutputStream().close();
        arguments = sb.toString();
      }
      conn.connect();
      
      int rc = conn.getResponseCode();
      String responseMessage = conn.getResponseMessage();
      if (rc != 200) {
        logger.info("response " + rc + " (" + responseMessage + ") from " + requestMethod + " " + url + " args:" + arguments);
      }
      if (rc == 404) {
        throw new TrelloCardNotFoundException("Ticket not found");
      }
      if (rc > 399)
      {
        String str = stream2String(conn.getErrorStream());
        logger.info("error response:" + str);
        return null;
      }
      return getWrappedInputStream(conn
        .getInputStream(), "gzip".equalsIgnoreCase(conn.getContentEncoding()));
    }
    catch (IOException e)
    {
      throw new TrelloException(e.getMessage());
    }
  }
  
  private static InputStream getWrappedInputStream(InputStream is, boolean gzip)
    throws IOException
  {
    if (gzip) {
      return new BufferedInputStream(new GZIPInputStream(is));
    }
    return new BufferedInputStream(is);
  }
  
  private static String stream2String(InputStream in)
  {
    InputStreamReader is = new InputStreamReader(in);
    BufferedReader br = new BufferedReader(is);
    StringBuilder sb = new StringBuilder();
    try
    {
      String read = br.readLine();
      while (read != null)
      {
        sb.append(read);
        read = br.readLine();
      }
    }
    catch (IOException e)
    {
      return "Error trying to read stream:" + e.getMessage();
    }
    return sb.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\support\Trello.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */