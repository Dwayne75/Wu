package org.fourthline.cling.controlpoint;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.controlpoint.event.ExecuteAction;
import org.fourthline.cling.controlpoint.event.Search;
import org.fourthline.cling.model.message.header.MXHeader;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.registry.Registry;

@ApplicationScoped
public class ControlPointImpl
  implements ControlPoint
{
  private static Logger log = Logger.getLogger(ControlPointImpl.class.getName());
  protected UpnpServiceConfiguration configuration;
  protected ProtocolFactory protocolFactory;
  protected Registry registry;
  
  protected ControlPointImpl() {}
  
  @Inject
  public ControlPointImpl(UpnpServiceConfiguration configuration, ProtocolFactory protocolFactory, Registry registry)
  {
    log.fine("Creating ControlPoint: " + getClass().getName());
    
    this.configuration = configuration;
    this.protocolFactory = protocolFactory;
    this.registry = registry;
  }
  
  public UpnpServiceConfiguration getConfiguration()
  {
    return this.configuration;
  }
  
  public ProtocolFactory getProtocolFactory()
  {
    return this.protocolFactory;
  }
  
  public Registry getRegistry()
  {
    return this.registry;
  }
  
  public void search(@Observes Search search)
  {
    search(search.getSearchType(), search.getMxSeconds());
  }
  
  public void search()
  {
    search(new STAllHeader(), MXHeader.DEFAULT_VALUE.intValue());
  }
  
  public void search(UpnpHeader searchType)
  {
    search(searchType, MXHeader.DEFAULT_VALUE.intValue());
  }
  
  public void search(int mxSeconds)
  {
    search(new STAllHeader(), mxSeconds);
  }
  
  public void search(UpnpHeader searchType, int mxSeconds)
  {
    log.fine("Sending asynchronous search for: " + searchType.getString());
    getConfiguration().getAsyncProtocolExecutor().execute(
      getProtocolFactory().createSendingSearch(searchType, mxSeconds));
  }
  
  public void execute(ExecuteAction executeAction)
  {
    execute(executeAction.getCallback());
  }
  
  public Future execute(ActionCallback callback)
  {
    log.fine("Invoking action in background: " + callback);
    callback.setControlPoint(this);
    ExecutorService executor = getConfiguration().getSyncProtocolExecutorService();
    return executor.submit(callback);
  }
  
  public void execute(SubscriptionCallback callback)
  {
    log.fine("Invoking subscription in background: " + callback);
    callback.setControlPoint(this);
    getConfiguration().getSyncProtocolExecutorService().execute(callback);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\controlpoint\ControlPointImpl.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */