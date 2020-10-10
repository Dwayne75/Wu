package org.fourthline.cling.support.avtransport.impl;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.AVTransportErrorCode;
import org.fourthline.cling.support.avtransport.AVTransportException;
import org.fourthline.cling.support.avtransport.AbstractAVTransportService;
import org.fourthline.cling.support.avtransport.impl.state.AbstractState;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.AVTransport;
import org.fourthline.cling.support.model.DeviceCapabilities;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PlayMode;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.RecordQualityMode;
import org.fourthline.cling.support.model.SeekMode;
import org.fourthline.cling.support.model.StorageMedium;
import org.fourthline.cling.support.model.TransportAction;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.TransportSettings;
import org.seamless.statemachine.StateMachineBuilder;
import org.seamless.statemachine.TransitionException;

public class AVTransportService<T extends AVTransport>
  extends AbstractAVTransportService
{
  private static final Logger log = Logger.getLogger(AVTransportService.class.getName());
  private final Map<Long, AVTransportStateMachine> stateMachines = new ConcurrentHashMap();
  final Class<? extends AVTransportStateMachine> stateMachineDefinition;
  final Class<? extends AbstractState> initialState;
  final Class<? extends AVTransport> transportClass;
  
  public AVTransportService(Class<? extends AVTransportStateMachine> stateMachineDefinition, Class<? extends AbstractState> initialState)
  {
    this(stateMachineDefinition, initialState, AVTransport.class);
  }
  
  public AVTransportService(Class<? extends AVTransportStateMachine> stateMachineDefinition, Class<? extends AbstractState> initialState, Class<T> transportClass)
  {
    this.stateMachineDefinition = stateMachineDefinition;
    this.initialState = initialState;
    this.transportClass = transportClass;
  }
  
  public void setAVTransportURI(UnsignedIntegerFourBytes instanceId, String currentURI, String currentURIMetaData)
    throws AVTransportException
  {
    try
    {
      uri = new URI(currentURI);
    }
    catch (Exception ex)
    {
      URI uri;
      throw new AVTransportException(ErrorCode.INVALID_ARGS, "CurrentURI can not be null or malformed");
    }
    try
    {
      URI uri;
      AVTransportStateMachine transportStateMachine = findStateMachine(instanceId, true);
      transportStateMachine.setTransportURI(uri, currentURIMetaData);
    }
    catch (TransitionException ex)
    {
      throw new AVTransportException(AVTransportErrorCode.TRANSITION_NOT_AVAILABLE, ex.getMessage());
    }
  }
  
  public void setNextAVTransportURI(UnsignedIntegerFourBytes instanceId, String nextURI, String nextURIMetaData)
    throws AVTransportException
  {
    try
    {
      uri = new URI(nextURI);
    }
    catch (Exception ex)
    {
      URI uri;
      throw new AVTransportException(ErrorCode.INVALID_ARGS, "NextURI can not be null or malformed");
    }
    try
    {
      URI uri;
      AVTransportStateMachine transportStateMachine = findStateMachine(instanceId, true);
      transportStateMachine.setNextTransportURI(uri, nextURIMetaData);
    }
    catch (TransitionException ex)
    {
      throw new AVTransportException(AVTransportErrorCode.TRANSITION_NOT_AVAILABLE, ex.getMessage());
    }
  }
  
  public void setPlayMode(UnsignedIntegerFourBytes instanceId, String newPlayMode)
    throws AVTransportException
  {
    AVTransport transport = ((AbstractState)findStateMachine(instanceId).getCurrentState()).getTransport();
    try
    {
      transport.setTransportSettings(new TransportSettings(
      
        PlayMode.valueOf(newPlayMode), transport
        .getTransportSettings().getRecQualityMode()));
    }
    catch (IllegalArgumentException ex)
    {
      throw new AVTransportException(AVTransportErrorCode.PLAYMODE_NOT_SUPPORTED, "Unsupported play mode: " + newPlayMode);
    }
  }
  
  public void setRecordQualityMode(UnsignedIntegerFourBytes instanceId, String newRecordQualityMode)
    throws AVTransportException
  {
    AVTransport transport = ((AbstractState)findStateMachine(instanceId).getCurrentState()).getTransport();
    try
    {
      transport.setTransportSettings(new TransportSettings(transport
      
        .getTransportSettings().getPlayMode(), 
        RecordQualityMode.valueOrExceptionOf(newRecordQualityMode)));
    }
    catch (IllegalArgumentException ex)
    {
      throw new AVTransportException(AVTransportErrorCode.RECORDQUALITYMODE_NOT_SUPPORTED, "Unsupported record quality mode: " + newRecordQualityMode);
    }
  }
  
  public MediaInfo getMediaInfo(UnsignedIntegerFourBytes instanceId)
    throws AVTransportException
  {
    return ((AbstractState)findStateMachine(instanceId).getCurrentState()).getTransport().getMediaInfo();
  }
  
  public TransportInfo getTransportInfo(UnsignedIntegerFourBytes instanceId)
    throws AVTransportException
  {
    return ((AbstractState)findStateMachine(instanceId).getCurrentState()).getTransport().getTransportInfo();
  }
  
  public PositionInfo getPositionInfo(UnsignedIntegerFourBytes instanceId)
    throws AVTransportException
  {
    return ((AbstractState)findStateMachine(instanceId).getCurrentState()).getTransport().getPositionInfo();
  }
  
  public DeviceCapabilities getDeviceCapabilities(UnsignedIntegerFourBytes instanceId)
    throws AVTransportException
  {
    return ((AbstractState)findStateMachine(instanceId).getCurrentState()).getTransport().getDeviceCapabilities();
  }
  
  public TransportSettings getTransportSettings(UnsignedIntegerFourBytes instanceId)
    throws AVTransportException
  {
    return ((AbstractState)findStateMachine(instanceId).getCurrentState()).getTransport().getTransportSettings();
  }
  
  public void stop(UnsignedIntegerFourBytes instanceId)
    throws AVTransportException
  {
    try
    {
      findStateMachine(instanceId).stop();
    }
    catch (TransitionException ex)
    {
      throw new AVTransportException(AVTransportErrorCode.TRANSITION_NOT_AVAILABLE, ex.getMessage());
    }
  }
  
  public void play(UnsignedIntegerFourBytes instanceId, String speed)
    throws AVTransportException
  {
    try
    {
      findStateMachine(instanceId).play(speed);
    }
    catch (TransitionException ex)
    {
      throw new AVTransportException(AVTransportErrorCode.TRANSITION_NOT_AVAILABLE, ex.getMessage());
    }
  }
  
  public void pause(UnsignedIntegerFourBytes instanceId)
    throws AVTransportException
  {
    try
    {
      findStateMachine(instanceId).pause();
    }
    catch (TransitionException ex)
    {
      throw new AVTransportException(AVTransportErrorCode.TRANSITION_NOT_AVAILABLE, ex.getMessage());
    }
  }
  
  public void record(UnsignedIntegerFourBytes instanceId)
    throws AVTransportException
  {
    try
    {
      findStateMachine(instanceId).record();
    }
    catch (TransitionException ex)
    {
      throw new AVTransportException(AVTransportErrorCode.TRANSITION_NOT_AVAILABLE, ex.getMessage());
    }
  }
  
  public void seek(UnsignedIntegerFourBytes instanceId, String unit, String target)
    throws AVTransportException
  {
    try
    {
      seekMode = SeekMode.valueOrExceptionOf(unit);
    }
    catch (IllegalArgumentException ex)
    {
      SeekMode seekMode;
      throw new AVTransportException(AVTransportErrorCode.SEEKMODE_NOT_SUPPORTED, "Unsupported seek mode: " + unit);
    }
    try
    {
      SeekMode seekMode;
      findStateMachine(instanceId).seek(seekMode, target);
    }
    catch (TransitionException ex)
    {
      throw new AVTransportException(AVTransportErrorCode.TRANSITION_NOT_AVAILABLE, ex.getMessage());
    }
  }
  
  public void next(UnsignedIntegerFourBytes instanceId)
    throws AVTransportException
  {
    try
    {
      findStateMachine(instanceId).next();
    }
    catch (TransitionException ex)
    {
      throw new AVTransportException(AVTransportErrorCode.TRANSITION_NOT_AVAILABLE, ex.getMessage());
    }
  }
  
  public void previous(UnsignedIntegerFourBytes instanceId)
    throws AVTransportException
  {
    try
    {
      findStateMachine(instanceId).previous();
    }
    catch (TransitionException ex)
    {
      throw new AVTransportException(AVTransportErrorCode.TRANSITION_NOT_AVAILABLE, ex.getMessage());
    }
  }
  
  protected TransportAction[] getCurrentTransportActions(UnsignedIntegerFourBytes instanceId)
    throws Exception
  {
    AVTransportStateMachine stateMachine = findStateMachine(instanceId);
    try
    {
      return ((AbstractState)stateMachine.getCurrentState()).getCurrentTransportActions();
    }
    catch (TransitionException ex) {}
    return new TransportAction[0];
  }
  
  public UnsignedIntegerFourBytes[] getCurrentInstanceIds()
  {
    synchronized (this.stateMachines)
    {
      UnsignedIntegerFourBytes[] ids = new UnsignedIntegerFourBytes[this.stateMachines.size()];
      int i = 0;
      for (Long id : this.stateMachines.keySet())
      {
        ids[i] = new UnsignedIntegerFourBytes(id.longValue());
        i++;
      }
      return ids;
    }
  }
  
  protected AVTransportStateMachine findStateMachine(UnsignedIntegerFourBytes instanceId)
    throws AVTransportException
  {
    return findStateMachine(instanceId, true);
  }
  
  protected AVTransportStateMachine findStateMachine(UnsignedIntegerFourBytes instanceId, boolean createDefaultTransport)
    throws AVTransportException
  {
    synchronized (this.stateMachines)
    {
      long id = instanceId.getValue().longValue();
      AVTransportStateMachine stateMachine = (AVTransportStateMachine)this.stateMachines.get(Long.valueOf(id));
      if ((stateMachine == null) && (id == 0L) && (createDefaultTransport))
      {
        log.fine("Creating default transport instance with ID '0'");
        stateMachine = createStateMachine(instanceId);
        this.stateMachines.put(Long.valueOf(id), stateMachine);
      }
      else if (stateMachine == null)
      {
        throw new AVTransportException(AVTransportErrorCode.INVALID_INSTANCE_ID);
      }
      log.fine("Found transport control with ID '" + id + "'");
      return stateMachine;
    }
  }
  
  protected AVTransportStateMachine createStateMachine(UnsignedIntegerFourBytes instanceId)
  {
    return (AVTransportStateMachine)StateMachineBuilder.build(this.stateMachineDefinition, this.initialState, new Class[] { this.transportClass }, new Object[] {
    
      createTransport(instanceId, getLastChange()) });
  }
  
  protected AVTransport createTransport(UnsignedIntegerFourBytes instanceId, LastChange lastChange)
  {
    return new AVTransport(instanceId, lastChange, StorageMedium.NETWORK);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\avtransport\impl\AVTransportService.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */