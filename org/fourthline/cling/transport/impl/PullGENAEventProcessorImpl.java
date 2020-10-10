package org.fourthline.cling.transport.impl;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.inject.Alternative;
import org.fourthline.cling.model.UnsupportedDataException;
import org.fourthline.cling.model.message.gena.IncomingEventRequestMessage;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.meta.StateVariable;
import org.fourthline.cling.model.state.StateVariableValue;
import org.fourthline.cling.transport.spi.GENAEventProcessor;
import org.seamless.xml.XmlPullParserUtils;
import org.xmlpull.v1.XmlPullParser;

@Alternative
public class PullGENAEventProcessorImpl
  extends GENAEventProcessorImpl
{
  private static Logger log = Logger.getLogger(GENAEventProcessor.class.getName());
  
  public void readBody(IncomingEventRequestMessage requestMessage)
    throws UnsupportedDataException
  {
    log.fine("Reading body of: " + requestMessage);
    if (log.isLoggable(Level.FINER))
    {
      log.finer("===================================== GENA BODY BEGIN ============================================");
      log.finer(requestMessage.getBody() != null ? requestMessage.getBody().toString() : null);
      log.finer("-===================================== GENA BODY END ============================================");
    }
    String body = getMessageBody(requestMessage);
    try
    {
      XmlPullParser xpp = XmlPullParserUtils.createParser(body);
      readProperties(xpp, requestMessage);
    }
    catch (Exception ex)
    {
      throw new UnsupportedDataException("Can't transform message payload: " + ex.getMessage(), ex, body);
    }
  }
  
  protected void readProperties(XmlPullParser xpp, IncomingEventRequestMessage message)
    throws Exception
  {
    StateVariable[] stateVariables = message.getService().getStateVariables();
    int event;
    while ((event = xpp.next()) != 1) {
      if ((event == 2) && 
        (xpp.getName().equals("property"))) {
        readProperty(xpp, message, stateVariables);
      }
    }
  }
  
  protected void readProperty(XmlPullParser xpp, IncomingEventRequestMessage message, StateVariable[] stateVariables)
    throws Exception
  {
    int event;
    do
    {
      event = xpp.next();
      if (event == 2)
      {
        String stateVariableName = xpp.getName();
        for (StateVariable stateVariable : stateVariables) {
          if (stateVariable.getName().equals(stateVariableName))
          {
            log.fine("Reading state variable value: " + stateVariableName);
            String value = xpp.nextText();
            message.getStateVariableValues().add(new StateVariableValue(stateVariable, value));
            break;
          }
        }
      }
    } while ((event != 1) && ((event != 3) || (!xpp.getName().equals("property"))));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\impl\PullGENAEventProcessorImpl.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */