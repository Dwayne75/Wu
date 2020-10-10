package javax.xml.bind.helpers;

import java.io.PrintStream;
import java.net.URL;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;
import org.w3c.dom.Node;

public class DefaultValidationEventHandler
  implements ValidationEventHandler
{
  public boolean handleEvent(ValidationEvent event)
  {
    if (event == null) {
      throw new IllegalArgumentException();
    }
    String severity = null;
    boolean retVal = false;
    switch (event.getSeverity())
    {
    case 0: 
      severity = Messages.format("DefaultValidationEventHandler.Warning");
      retVal = true;
      break;
    case 1: 
      severity = Messages.format("DefaultValidationEventHandler.Error");
      retVal = false;
      break;
    case 2: 
      severity = Messages.format("DefaultValidationEventHandler.FatalError");
      retVal = false;
      break;
    default: 
      if (!$assertionsDisabled) {
        throw new AssertionError(Messages.format("DefaultValidationEventHandler.UnrecognizedSeverity", Integer.valueOf(event.getSeverity())));
      }
      break;
    }
    String location = getLocation(event);
    
    System.out.println(Messages.format("DefaultValidationEventHandler.SeverityMessage", severity, event.getMessage(), location));
    
    return retVal;
  }
  
  private String getLocation(ValidationEvent event)
  {
    StringBuffer msg = new StringBuffer();
    
    ValidationEventLocator locator = event.getLocator();
    if (locator != null)
    {
      URL url = locator.getURL();
      Object obj = locator.getObject();
      Node node = locator.getNode();
      int line = locator.getLineNumber();
      if ((url != null) || (line != -1))
      {
        msg.append("line " + line);
        if (url != null) {
          msg.append(" of " + url);
        }
      }
      else if (obj != null)
      {
        msg.append(" obj: " + obj.toString());
      }
      else if (node != null)
      {
        msg.append(" node: " + node.toString());
      }
    }
    else
    {
      msg.append(Messages.format("DefaultValidationEventHandler.LocationUnavailable"));
    }
    return msg.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\xml\bind\helpers\DefaultValidationEventHandler.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */