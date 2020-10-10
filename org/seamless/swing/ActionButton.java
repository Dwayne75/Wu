package org.seamless.swing;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;

public class ActionButton
  extends JButton
{
  public ActionButton(String actionCommand)
  {
    setActionCommand(actionCommand);
  }
  
  public ActionButton(Icon icon, String actionCommand)
  {
    super(icon);
    setActionCommand(actionCommand);
  }
  
  public ActionButton(String s, String actionCommand)
  {
    super(s);
    setActionCommand(actionCommand);
  }
  
  public ActionButton(Action action, String actionCommand)
  {
    super(action);
    setActionCommand(actionCommand);
  }
  
  public ActionButton(String s, Icon icon, String actionCommand)
  {
    super(s, icon);
    setActionCommand(actionCommand);
  }
  
  public ActionButton enableDefaultEvents(final Controller controller)
  {
    controller.registerAction(this, new DefaultAction()
    {
      public void actionPerformed(ActionEvent actionEvent)
      {
        Event e;
        if ((e = ActionButton.this.createDefaultEvent()) != null) {
          controller.fireEvent(e);
        }
        if ((e = ActionButton.this.createDefaultGlobalEvent()) != null) {
          controller.fireEventGlobal(e);
        }
      }
    });
    return this;
  }
  
  public Event createDefaultEvent()
  {
    return null;
  }
  
  public Event createDefaultGlobalEvent()
  {
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\swing\ActionButton.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */