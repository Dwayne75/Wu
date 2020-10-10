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
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.BrowseResult;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.SortCriterion;

public abstract class Browse
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
  
  private static Logger log = Logger.getLogger(Browse.class.getName());
  
  public Browse(Service service, String containerId, BrowseFlag flag)
  {
    this(service, containerId, flag, "*", 0L, null, new SortCriterion[0]);
  }
  
  public Browse(Service service, String objectID, BrowseFlag flag, String filter, long firstResult, Long maxResults, SortCriterion... orderBy)
  {
    super(new ActionInvocation(service.getAction("Browse")));
    
    log.fine("Creating browse action for object ID: " + objectID);
    
    getActionInvocation().setInput("ObjectID", objectID);
    getActionInvocation().setInput("BrowseFlag", flag.toString());
    getActionInvocation().setInput("Filter", filter);
    getActionInvocation().setInput("StartingIndex", new UnsignedIntegerFourBytes(firstResult));
    getActionInvocation().setInput("RequestedCount", new UnsignedIntegerFourBytes(maxResults == null ? 
      getDefaultMaxResults() : maxResults.longValue()));
    
    getActionInvocation().setInput("SortCriteria", SortCriterion.toString(orderBy));
  }
  
  public void run()
  {
    updateStatus(Status.LOADING);
    super.run();
  }
  
  public void success(ActionInvocation invocation)
  {
    log.fine("Successful browse action, reading output argument values");
    
    BrowseResult result = new BrowseResult(invocation.getOutput("Result").getValue().toString(), (UnsignedIntegerFourBytes)invocation.getOutput("NumberReturned").getValue(), (UnsignedIntegerFourBytes)invocation.getOutput("TotalMatches").getValue(), (UnsignedIntegerFourBytes)invocation.getOutput("UpdateID").getValue());
    
    boolean proceed = receivedRaw(invocation, result);
    if ((proceed) && (result.getCountLong() > 0L) && (result.getResult().length() > 0))
    {
      try
      {
        DIDLParser didlParser = new DIDLParser();
        DIDLContent didl = didlParser.parse(result.getResult());
        received(invocation, didl);
        updateStatus(Status.OK);
      }
      catch (Exception ex)
      {
        invocation.setFailure(new ActionException(ErrorCode.ACTION_FAILED, "Can't parse DIDL XML response: " + ex, ex));
        
        failure(invocation, null);
      }
    }
    else
    {
      received(invocation, new DIDLContent());
      updateStatus(Status.NO_CONTENT);
    }
  }
  
  public long getDefaultMaxResults()
  {
    return 999L;
  }
  
  public boolean receivedRaw(ActionInvocation actionInvocation, BrowseResult browseResult)
  {
    return true;
  }
  
  public abstract void received(ActionInvocation paramActionInvocation, DIDLContent paramDIDLContent);
  
  public abstract void updateStatus(Status paramStatus);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\contentdirectory\callback\Browse.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */