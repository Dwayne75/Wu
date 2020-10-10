package org.controlsfx.validation;

import java.util.Comparator;
import javafx.scene.control.Control;

public abstract interface ValidationMessage
  extends Comparable<ValidationMessage>
{
  public static final Comparator<ValidationMessage> COMPARATOR = new Comparator()
  {
    public int compare(ValidationMessage vm1, ValidationMessage vm2)
    {
      if (vm1 == vm2) {
        return 0;
      }
      if (vm1 == null) {
        return 1;
      }
      if (vm2 == null) {
        return -1;
      }
      return vm1.compareTo(vm2);
    }
  };
  
  public abstract String getText();
  
  public abstract Severity getSeverity();
  
  public abstract Control getTarget();
  
  public static ValidationMessage error(Control target, String text)
  {
    return new SimpleValidationMessage(target, text, Severity.ERROR);
  }
  
  public static ValidationMessage warning(Control target, String text)
  {
    return new SimpleValidationMessage(target, text, Severity.WARNING);
  }
  
  public int compareTo(ValidationMessage msg)
  {
    return (msg == null) || (getTarget() != msg.getTarget()) ? -1 : getSeverity().compareTo(msg.getSeverity());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\validation\ValidationMessage.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */