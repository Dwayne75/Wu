package org.seamless.swing;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public abstract class DefaultAction
  extends AbstractAction
{
  public void executeInController(Controller controller, ActionEvent event)
  {
    actionPerformed(event);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\swing\DefaultAction.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */