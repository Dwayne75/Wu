package org.fourthline.cling.transport.impl;

import java.util.logging.Logger;
import javax.enterprise.inject.Alternative;
import org.fourthline.cling.model.UnsupportedDataException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.control.ActionRequestMessage;
import org.fourthline.cling.model.message.control.ActionResponseMessage;
import org.fourthline.cling.transport.spi.SOAPActionProcessor;
import org.seamless.xml.XmlPullParserUtils;

@Alternative
public class RecoveringSOAPActionProcessorImpl
  extends PullSOAPActionProcessorImpl
{
  private static Logger log = Logger.getLogger(SOAPActionProcessor.class.getName());
  
  public void readBody(ActionRequestMessage requestMessage, ActionInvocation actionInvocation)
    throws UnsupportedDataException
  {
    try
    {
      super.readBody(requestMessage, actionInvocation);
    }
    catch (UnsupportedDataException ex)
    {
      if (!requestMessage.isBodyNonEmptyString()) {
        throw ex;
      }
      log.warning("Trying to recover from invalid SOAP XML request: " + ex);
      String body = getMessageBody(requestMessage);
      
      String fixedBody = XmlPullParserUtils.fixXMLEntities(body);
      try
      {
        requestMessage.setBody(fixedBody);
        super.readBody(requestMessage, actionInvocation);
      }
      catch (UnsupportedDataException ex2)
      {
        handleInvalidMessage(actionInvocation, ex, ex2);
      }
    }
  }
  
  public void readBody(ActionResponseMessage responseMsg, ActionInvocation actionInvocation)
    throws UnsupportedDataException
  {
    try
    {
      super.readBody(responseMsg, actionInvocation);
    }
    catch (UnsupportedDataException ex)
    {
      if (!responseMsg.isBodyNonEmptyString()) {
        throw ex;
      }
      log.warning("Trying to recover from invalid SOAP XML response: " + ex);
      String body = getMessageBody(responseMsg);
      
      String fixedBody = XmlPullParserUtils.fixXMLEntities(body);
      if (fixedBody.endsWith("</s:Envelop")) {
        fixedBody = fixedBody + "e>";
      }
      try
      {
        responseMsg.setBody(fixedBody);
        super.readBody(responseMsg, actionInvocation);
      }
      catch (UnsupportedDataException ex2)
      {
        handleInvalidMessage(actionInvocation, ex, ex2);
      }
    }
  }
  
  protected void handleInvalidMessage(ActionInvocation actionInvocation, UnsupportedDataException originalException, UnsupportedDataException recoveringException)
    throws UnsupportedDataException
  {
    throw originalException;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\impl\RecoveringSOAPActionProcessorImpl.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */