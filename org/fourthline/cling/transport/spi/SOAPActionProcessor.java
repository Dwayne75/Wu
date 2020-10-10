package org.fourthline.cling.transport.spi;

import org.fourthline.cling.model.UnsupportedDataException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.control.ActionRequestMessage;
import org.fourthline.cling.model.message.control.ActionResponseMessage;

public abstract interface SOAPActionProcessor
{
  public abstract void writeBody(ActionRequestMessage paramActionRequestMessage, ActionInvocation paramActionInvocation)
    throws UnsupportedDataException;
  
  public abstract void writeBody(ActionResponseMessage paramActionResponseMessage, ActionInvocation paramActionInvocation)
    throws UnsupportedDataException;
  
  public abstract void readBody(ActionRequestMessage paramActionRequestMessage, ActionInvocation paramActionInvocation)
    throws UnsupportedDataException;
  
  public abstract void readBody(ActionResponseMessage paramActionResponseMessage, ActionInvocation paramActionInvocation)
    throws UnsupportedDataException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\spi\SOAPActionProcessor.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */