package org.seamless.swing;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.JFrame;

public class AbstractController<V extends Container>
  implements Controller<V>
{
  private static Logger log = Logger.getLogger(AbstractController.class.getName());
  private V view;
  private Controller parentController;
  private List<Controller> subControllers = new ArrayList();
  private Map<String, DefaultAction> actions = new HashMap();
  private Map<Class, List<EventListener>> eventListeners = new HashMap();
  
  public AbstractController(V view)
  {
    this.view = view;
  }
  
  public AbstractController() {}
  
  public AbstractController(Controller parentController)
  {
    this(null, parentController);
  }
  
  public AbstractController(V view, Controller parentController)
  {
    this.view = view;
    if (parentController != null)
    {
      this.parentController = parentController;
      parentController.getSubControllers().add(this);
    }
  }
  
  public V getView()
  {
    return this.view;
  }
  
  public Controller getParentController()
  {
    return this.parentController;
  }
  
  public List<Controller> getSubControllers()
  {
    return this.subControllers;
  }
  
  public void dispose()
  {
    log.fine("Disposing controller");
    Iterator<Controller> it = this.subControllers.iterator();
    while (it.hasNext())
    {
      Controller subcontroller = (Controller)it.next();
      subcontroller.dispose();
      it.remove();
    }
  }
  
  public void registerAction(AbstractButton source, DefaultAction action)
  {
    source.removeActionListener(this);
    source.addActionListener(this);
    this.actions.put(source.getActionCommand(), action);
  }
  
  public void registerAction(AbstractButton source, String actionCommand, DefaultAction action)
  {
    source.setActionCommand(actionCommand);
    registerAction(source, action);
  }
  
  public void deregisterAction(String actionCommand)
  {
    this.actions.remove(actionCommand);
  }
  
  public void registerEventListener(Class eventClass, EventListener eventListener)
  {
    log.fine("Registering listener: " + eventListener + " for event type: " + eventClass.getName());
    List<EventListener> listenersForEvent = (List)this.eventListeners.get(eventClass);
    if (listenersForEvent == null) {
      listenersForEvent = new ArrayList();
    }
    listenersForEvent.add(eventListener);
    this.eventListeners.put(eventClass, listenersForEvent);
  }
  
  public void fireEvent(Event event)
  {
    fireEvent(event, false);
  }
  
  public void fireEventGlobal(Event event)
  {
    fireEvent(event, true);
  }
  
  public void fireEvent(Event event, boolean global)
  {
    if (!event.alreadyFired(this))
    {
      log.finest("Event has not been fired already");
      if (this.eventListeners.get(event.getClass()) != null)
      {
        log.finest("Have listeners for this type of event: " + this.eventListeners.get(event.getClass()));
        for (EventListener eventListener : (List)this.eventListeners.get(event.getClass()))
        {
          log.fine("Processing event: " + event.getClass().getName() + " with listener: " + eventListener.getClass().getName());
          eventListener.handleEvent(event);
        }
      }
      event.addFiredInController(this);
      log.fine("Passing event: " + event.getClass().getName() + " DOWN in the controller hierarchy");
      Controller subController;
      for (Iterator i$ = this.subControllers.iterator(); i$.hasNext(); subController.fireEvent(event, global)) {
        subController = (Controller)i$.next();
      }
    }
    else
    {
      log.finest("Event already fired here, ignoring...");
    }
    if ((getParentController() != null) && (!event.alreadyFired(getParentController())) && (global))
    {
      log.fine("Passing event: " + event.getClass().getName() + " UP in the controller hierarchy");
      getParentController().fireEvent(event, global);
    }
    else
    {
      log.finest("Event does not propagate up the tree from here");
    }
  }
  
  public void actionPerformed(ActionEvent actionEvent)
  {
    try
    {
      AbstractButton button = (AbstractButton)actionEvent.getSource();
      String actionCommand = button.getActionCommand();
      DefaultAction action = (DefaultAction)this.actions.get(actionCommand);
      if (action != null)
      {
        log.fine("Handling command: " + actionCommand + " with action: " + action.getClass());
        try
        {
          preActionExecute();
          log.fine("Dispatching to action for execution");
          action.executeInController(this, actionEvent);
          postActionExecute();
        }
        catch (RuntimeException ex)
        {
          throw ex;
        }
        catch (Exception ex)
        {
          throw new RuntimeException(ex);
        }
        finally
        {
          finalActionExecute();
        }
      }
      else if (getParentController() != null)
      {
        log.fine("Passing action on to parent controller");
        this.parentController.actionPerformed(actionEvent);
      }
      else
      {
        throw new RuntimeException("Nobody is responsible for action command: " + actionCommand);
      }
    }
    catch (ClassCastException e)
    {
      throw new IllegalArgumentException("Action source is not an Abstractbutton: " + actionEvent);
    }
  }
  
  public void preActionExecute() {}
  
  public void postActionExecute() {}
  
  public void failedActionExecute() {}
  
  public void finalActionExecute() {}
  
  public void windowClosing(WindowEvent windowEvent)
  {
    dispose();
    ((JFrame)getView()).dispose();
  }
  
  public void windowOpened(WindowEvent windowEvent) {}
  
  public void windowClosed(WindowEvent windowEvent) {}
  
  public void windowIconified(WindowEvent windowEvent) {}
  
  public void windowDeiconified(WindowEvent windowEvent) {}
  
  public void windowActivated(WindowEvent windowEvent) {}
  
  public void windowDeactivated(WindowEvent windowEvent) {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\swing\AbstractController.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */