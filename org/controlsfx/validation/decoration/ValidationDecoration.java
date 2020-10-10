package org.controlsfx.validation.decoration;

import javafx.scene.control.Control;
import org.controlsfx.validation.ValidationMessage;

public abstract interface ValidationDecoration
{
  public abstract void removeDecorations(Control paramControl);
  
  public abstract void applyValidationDecoration(ValidationMessage paramValidationMessage);
  
  public abstract void applyRequiredDecoration(Control paramControl);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\validation\decoration\ValidationDecoration.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */