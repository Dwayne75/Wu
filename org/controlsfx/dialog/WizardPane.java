package org.controlsfx.dialog;

import java.net.URL;
import javafx.collections.ObservableList;
import javafx.scene.control.DialogPane;

public class WizardPane
  extends DialogPane
{
  public WizardPane()
  {
    getStylesheets().add(Wizard.class.getResource("wizard.css").toExternalForm());
    getStyleClass().add("wizard-pane");
  }
  
  public void onEnteringPage(Wizard wizard) {}
  
  public void onExitingPage(Wizard wizard) {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\dialog\WizardPane.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */