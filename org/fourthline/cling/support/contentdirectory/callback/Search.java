package org.fourthline.cling.support.contentdirectory.callback;

import java.util.logging.Logger;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionArgumentValue;
import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.contentdirectory.DIDLParser;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.SearchResult;
import org.fourthline.cling.support.model.SortCriterion;

public abstract class Search
  extends ActionCallback
{
  public static final String CAPS_WILDCARD = "*";
  
  public static enum Status
  {
    NO_CONTENT("No Content"),  LOADING("Loading..."),  OK("OK");
    
    private String defaultMessage;
    
    private Status(String defaultMessage)
    {
      this.defaultMessage = defaultMessage;
    }
    
    public String getDefaultMessage()
    {
      return this.defaultMessage;
    }
  }
  
  private static Logger log = Logger.getLogger(Search.class.getName());
  
  public Search(Service service, String containerId, String searchCriteria)
  {
    this(service, containerId, searchCriteria, "*", 0L, null, new SortCriterion[0]);
  }
  
  public Search(Service service, String containerId, String searchCriteria, String filter, long firstResult, Long maxResults, SortCriterion... orderBy)
  {
    super(new ActionInvocation(service.getAction("Search")));
    
    log.fine("Creating browse action for container ID: " + containerId);
    
    getActionInvocation().setInput("ContainerID", containerId);
    getActionInvocation().setInput("SearchCriteria", searchCriteria);
    getActionInvocation().setInput("Filter", filter);
    getActionInvocation().setInput("StartingIndex", new UnsignedIntegerFourBytes(firstResult));
    getActionInvocation().setInput("RequestedCount", new UnsignedIntegerFourBytes((maxResults == null ? 
    
      getDefaultMaxResults() : maxResults).longValue()));
    
    getActionInvocation().setInput("SortCriteria", SortCriterion.toString(orderBy));
  }
  
  public void run()
  {
    updateStatus(Status.LOADING);
    super.run();
  }
  
  public void success(ActionInvocation actionInvocation)
  {
    log.fine("Successful search action, reading output argument values");
    
    SearchResult result = new SearchResult(actionInvocation.getOutput("Result").getValue().toString(), (UnsignedIntegerFourBytes)actionInvocation.getOutput("NumberReturned").getValue(), (UnsignedIntegerFourBytes)actionInvocation.getOutput("TotalMatches").getValue(), (UnsignedIntegerFourBytes)actionInvocation.getOutput("UpdateID").getValue());
    
    boolean proceed = receivedRaw(actionInvocation, result);
    if ((proceed) && (result.getCountLong() > 0L) && (result.getResult().length() > 0))
    {
      try
      {
        DIDLParser didlParser = new DIDLParser();
        DIDLContent didl = didlParser.parse(result.getResult());
        received(actionInvocation, didl);
        updateStatus(Status.OK);
      }
      catch (Exception ex)
      {
        actionInvocation.setFailure(new ActionException(ErrorCode.ACTION_FAILED, "Can't parse DIDL XML response: " + ex, ex));
        
        failure(actionInvocation, null);
      }
    }
    else
    {
      received(actionInvocation, new DIDLContent());
      updateStatus(Status.NO_CONTENT);
    }
  }
  
  public Long getDefaultMaxResults()
  {
    return Long.valueOf(999L);
  }
  
  public boolean receivedRaw(ActionInvocation actionInvocation, SearchResult searchResult)
  {
    return true;
  }
  
  public abstract void received(ActionInvocation paramActionInvocation, DIDLContent paramDIDLContent);
  
  public abstract void updateStatus(Status paramStatus);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\contentdirectory\callback\Search.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */