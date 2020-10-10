package org.controlsfx.control;

import impl.org.controlsfx.version.VersionChecker;
import java.net.URL;
import javafx.scene.control.Control;

abstract class ControlsFXControl
  extends Control
{
  private String stylesheet;
  
  public ControlsFXControl()
  {
    VersionChecker.doVersionCheck();
  }
  
  protected final String getUserAgentStylesheet(Class<?> clazz, String fileName)
  {
    if (this.stylesheet == null) {
      this.stylesheet = clazz.getResource(fileName).toExternalForm();
    }
    return this.stylesheet;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\ControlsFXControl.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */