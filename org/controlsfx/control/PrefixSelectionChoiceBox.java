package org.controlsfx.control;

import impl.org.controlsfx.tools.PrefixSelectionCustomizer;
import javafx.scene.control.ChoiceBox;

public class PrefixSelectionChoiceBox<T>
  extends ChoiceBox<T>
{
  public PrefixSelectionChoiceBox()
  {
    PrefixSelectionCustomizer.customize(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\PrefixSelectionChoiceBox.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */