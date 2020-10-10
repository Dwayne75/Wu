package org.controlsfx.validation;

import javafx.scene.control.Control;

class SimpleValidationMessage
  implements ValidationMessage
{
  private final String text;
  private final Severity severity;
  private final Control target;
  
  public SimpleValidationMessage(Control target, String text, Severity severity)
  {
    this.text = text;
    this.severity = (severity == null ? Severity.ERROR : severity);
    this.target = target;
  }
  
  public Control getTarget()
  {
    return this.target;
  }
  
  public String getText()
  {
    return this.text;
  }
  
  public Severity getSeverity()
  {
    return this.severity;
  }
  
  public int hashCode()
  {
    int prime = 31;
    int result = 1;
    
    result = 31 * result + (this.severity == null ? 0 : this.severity.hashCode());
    result = 31 * result + (this.target == null ? 0 : this.target.hashCode());
    result = 31 * result + (this.text == null ? 0 : this.text.hashCode());
    return result;
  }
  
  public boolean equals(Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    SimpleValidationMessage other = (SimpleValidationMessage)obj;
    if (this.severity != other.severity) {
      return false;
    }
    if (this.target == null)
    {
      if (other.target != null) {
        return false;
      }
    }
    else if (!this.target.equals(other.target)) {
      return false;
    }
    if (this.text == null)
    {
      if (other.text != null) {
        return false;
      }
    }
    else if (!this.text.equals(other.text)) {
      return false;
    }
    return true;
  }
  
  public String toString()
  {
    return String.format("%s(%s)", new Object[] { this.severity, this.text });
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\validation\SimpleValidationMessage.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */