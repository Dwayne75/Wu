package org.fourthline.cling.support.lastchange;

import java.util.ArrayList;
import java.util.Collection;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.StateVariable;
import org.fourthline.cling.model.state.StateVariableValue;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;

public class LastChangeAwareServiceManager<T extends LastChangeDelegator>
  extends DefaultServiceManager<T>
{
  protected final LastChangeParser lastChangeParser;
  
  public LastChangeAwareServiceManager(LocalService<T> localService, LastChangeParser lastChangeParser)
  {
    this(localService, null, lastChangeParser);
  }
  
  public LastChangeAwareServiceManager(LocalService<T> localService, Class<T> serviceClass, LastChangeParser lastChangeParser)
  {
    super(localService, serviceClass);
    this.lastChangeParser = lastChangeParser;
  }
  
  protected LastChangeParser getLastChangeParser()
  {
    return this.lastChangeParser;
  }
  
  /* Error */
  public void fireLastChange()
  {
    // Byte code:
    //   0: aload_0
    //   1: invokevirtual 4	org/fourthline/cling/support/lastchange/LastChangeAwareServiceManager:lock	()V
    //   4: aload_0
    //   5: invokevirtual 5	org/fourthline/cling/support/lastchange/LastChangeAwareServiceManager:getImplementation	()Ljava/lang/Object;
    //   8: checkcast 6	org/fourthline/cling/support/lastchange/LastChangeDelegator
    //   11: invokeinterface 7 1 0
    //   16: aload_0
    //   17: invokevirtual 8	org/fourthline/cling/support/lastchange/LastChangeAwareServiceManager:getPropertyChangeSupport	()Ljava/beans/PropertyChangeSupport;
    //   20: invokevirtual 9	org/fourthline/cling/support/lastchange/LastChange:fire	(Ljava/beans/PropertyChangeSupport;)V
    //   23: aload_0
    //   24: invokevirtual 10	org/fourthline/cling/support/lastchange/LastChangeAwareServiceManager:unlock	()V
    //   27: goto +10 -> 37
    //   30: astore_1
    //   31: aload_0
    //   32: invokevirtual 10	org/fourthline/cling/support/lastchange/LastChangeAwareServiceManager:unlock	()V
    //   35: aload_1
    //   36: athrow
    //   37: return
    // Line number table:
    //   Java source line #72	-> byte code offset #0
    //   Java source line #74	-> byte code offset #4
    //   Java source line #76	-> byte code offset #23
    //   Java source line #77	-> byte code offset #27
    //   Java source line #76	-> byte code offset #30
    //   Java source line #78	-> byte code offset #37
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	38	0	this	LastChangeAwareServiceManager<T>
    //   30	6	1	localObject	Object
    // Exception table:
    //   from	to	target	type
    //   4	23	30	finally
  }
  
  protected Collection<StateVariableValue> readInitialEventedStateVariableValues()
    throws Exception
  {
    LastChange lc = new LastChange(getLastChangeParser());
    
    UnsignedIntegerFourBytes[] ids = ((LastChangeDelegator)getImplementation()).getCurrentInstanceIds();
    if (ids.length > 0) {
      for (UnsignedIntegerFourBytes instanceId : ids) {
        ((LastChangeDelegator)getImplementation()).appendCurrentState(lc, instanceId);
      }
    } else {
      ((LastChangeDelegator)getImplementation()).appendCurrentState(lc, new UnsignedIntegerFourBytes(0L));
    }
    StateVariable variable = getService().getStateVariable("LastChange");
    Object values = new ArrayList();
    ((Collection)values).add(new StateVariableValue(variable, lc.toString()));
    return (Collection<StateVariableValue>)values;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\lastchange\LastChangeAwareServiceManager.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */