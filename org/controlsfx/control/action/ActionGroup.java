package org.controlsfx.control.action;

import java.util.Arrays;
import java.util.Collection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;

public class ActionGroup
  extends Action
{
  public ActionGroup(String text, Action... actions)
  {
    this(text, Arrays.asList(actions));
  }
  
  public ActionGroup(String text, Collection<Action> actions)
  {
    super(text);
    getActions().addAll(actions);
  }
  
  public ActionGroup(String text, Node icon, Action... actions)
  {
    this(text, icon, Arrays.asList(actions));
  }
  
  public ActionGroup(String text, Node icon, Collection<Action> actions)
  {
    super(text);
    setGraphic(icon);
    getActions().addAll(actions);
  }
  
  private final ObservableList<Action> actions = FXCollections.observableArrayList();
  
  public final ObservableList<Action> getActions()
  {
    return this.actions;
  }
  
  public String toString()
  {
    return getText();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\action\ActionGroup.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */