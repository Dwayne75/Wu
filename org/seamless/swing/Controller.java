package org.seamless.swing;

import java.awt.Container;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.util.List;
import javax.swing.AbstractButton;

public abstract interface Controller<V extends Container>
  extends ActionListener, WindowListener
{
  public abstract V getView();
  
  public abstract Controller getParentController();
  
  public abstract List<Controller> getSubControllers();
  
  public abstract void dispose();
  
  public abstract void registerEventListener(Class paramClass, EventListener paramEventListener);
  
  public abstract void fireEvent(Event paramEvent);
  
  public abstract void fireEventGlobal(Event paramEvent);
  
  public abstract void fireEvent(Event paramEvent, boolean paramBoolean);
  
  public abstract void registerAction(AbstractButton paramAbstractButton, DefaultAction paramDefaultAction);
  
  public abstract void registerAction(AbstractButton paramAbstractButton, String paramString, DefaultAction paramDefaultAction);
  
  public abstract void preActionExecute();
  
  public abstract void postActionExecute();
  
  public abstract void failedActionExecute();
  
  public abstract void finalActionExecute();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\swing\Controller.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */