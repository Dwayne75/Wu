package org.fourthline.cling.transport.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import javax.enterprise.inject.Alternative;
import org.fourthline.cling.model.UnsupportedDataException;
import org.fourthline.cling.model.action.ActionArgumentValue;
import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.control.ActionRequestMessage;
import org.fourthline.cling.model.message.control.ActionResponseMessage;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.ActionArgument;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.transport.spi.SOAPActionProcessor;
import org.seamless.xml.XmlPullParserUtils;
import org.xmlpull.v1.XmlPullParser;

@Alternative
public class PullSOAPActionProcessorImpl
  extends SOAPActionProcessorImpl
{
  protected static Logger log = Logger.getLogger(SOAPActionProcessor.class.getName());
  
  public void readBody(ActionRequestMessage requestMessage, ActionInvocation actionInvocation)
    throws UnsupportedDataException
  {
    String body = getMessageBody(requestMessage);
    try
    {
      XmlPullParser xpp = XmlPullParserUtils.createParser(body);
      readBodyRequest(xpp, requestMessage, actionInvocation);
    }
    catch (Exception ex)
    {
      throw new UnsupportedDataException("Can't transform message payload: " + ex, ex, body);
    }
  }
  
  public void readBody(ActionResponseMessage responseMsg, ActionInvocation actionInvocation)
    throws UnsupportedDataException
  {
    String body = getMessageBody(responseMsg);
    try
    {
      XmlPullParser xpp = XmlPullParserUtils.createParser(body);
      readBodyElement(xpp);
      readBodyResponse(xpp, actionInvocation);
    }
    catch (Exception ex)
    {
      throw new UnsupportedDataException("Can't transform message payload: " + ex, ex, body);
    }
  }
  
  protected void readBodyElement(XmlPullParser xpp)
    throws Exception
  {
    XmlPullParserUtils.searchTag(xpp, "Body");
  }
  
  protected void readBodyRequest(XmlPullParser xpp, ActionRequestMessage requestMessage, ActionInvocation actionInvocation)
    throws Exception
  {
    XmlPullParserUtils.searchTag(xpp, actionInvocation.getAction().getName());
    readActionInputArguments(xpp, actionInvocation);
  }
  
  protected void readBodyResponse(XmlPullParser xpp, ActionInvocation actionInvocation)
    throws Exception
  {
    int event;
    do
    {
      event = xpp.next();
      if (event == 2)
      {
        if (xpp.getName().equals("Fault"))
        {
          ActionException e = readFaultElement(xpp);
          actionInvocation.setFailure(e);
          return;
        }
        if (xpp.getName().equals(actionInvocation.getAction().getName() + "Response"))
        {
          readActionOutputArguments(xpp, actionInvocation);
          return;
        }
      }
    } while ((event != 1) && ((event != 3) || (!xpp.getName().equals("Body"))));
    throw new ActionException(ErrorCode.ACTION_FAILED, String.format("Action SOAP response do not contain %s element", new Object[] {actionInvocation
      .getAction().getName() + "Response" }));
  }
  
  protected void readActionInputArguments(XmlPullParser xpp, ActionInvocation actionInvocation)
    throws Exception
  {
    actionInvocation.setInput(readArgumentValues(xpp, actionInvocation.getAction().getInputArguments()));
  }
  
  protected void readActionOutputArguments(XmlPullParser xpp, ActionInvocation actionInvocation)
    throws Exception
  {
    actionInvocation.setOutput(readArgumentValues(xpp, actionInvocation.getAction().getOutputArguments()));
  }
  
  protected Map<String, String> getMatchingNodes(XmlPullParser xpp, ActionArgument[] args)
    throws Exception
  {
    List<String> names = new ArrayList();
    for (ActionArgument argument : args)
    {
      names.add(argument.getName().toUpperCase(Locale.ROOT));
      for (String alias : Arrays.asList(argument.getAliases())) {
        names.add(alias.toUpperCase(Locale.ROOT));
      }
    }
    Object matches = new HashMap();
    
    String enclosingTag = xpp.getName();
    int event;
    do
    {
      event = xpp.next();
      if ((event == 2) && (names.contains(xpp.getName().toUpperCase(Locale.ROOT)))) {
        ((Map)matches).put(xpp.getName(), xpp.nextText());
      }
    } while ((event != 1) && ((event != 3) || (!xpp.getName().equals(enclosingTag))));
    if (((Map)matches).size() < args.length) {
      throw new ActionException(ErrorCode.ARGUMENT_VALUE_INVALID, "Invalid number of input or output arguments in XML message, expected " + args.length + " but found " + ((Map)matches).size());
    }
    return (Map<String, String>)matches;
  }
  
  protected ActionArgumentValue[] readArgumentValues(XmlPullParser xpp, ActionArgument[] args)
    throws Exception
  {
    Map<String, String> matches = getMatchingNodes(xpp, args);
    
    ActionArgumentValue[] values = new ActionArgumentValue[args.length];
    for (int i = 0; i < args.length; i++)
    {
      ActionArgument arg = args[i];
      String value = findActionArgumentValue(matches, arg);
      if (value == null) {
        throw new ActionException(ErrorCode.ARGUMENT_VALUE_INVALID, "Could not find argument '" + arg.getName() + "' node");
      }
      log.fine("Reading action argument: " + arg.getName());
      values[i] = createValue(arg, value);
    }
    return values;
  }
  
  protected String findActionArgumentValue(Map<String, String> entries, ActionArgument arg)
  {
    for (Map.Entry<String, String> entry : entries.entrySet()) {
      if (arg.isNameOrAlias((String)entry.getKey())) {
        return (String)entry.getValue();
      }
    }
    return null;
  }
  
  protected ActionException readFaultElement(XmlPullParser xpp)
    throws Exception
  {
    String errorCode = null;
    String errorDescription = null;
    
    XmlPullParserUtils.searchTag(xpp, "UPnPError");
    int event;
    do
    {
      event = xpp.next();
      if (event == 2)
      {
        String tag = xpp.getName();
        if (tag.equals("errorCode")) {
          errorCode = xpp.nextText();
        } else if (tag.equals("errorDescription")) {
          errorDescription = xpp.nextText();
        }
      }
    } while ((event != 1) && ((event != 3) || (!xpp.getName().equals("UPnPError"))));
    if (errorCode != null) {
      try
      {
        int numericCode = Integer.valueOf(errorCode).intValue();
        ErrorCode standardErrorCode = ErrorCode.getByCode(numericCode);
        if (standardErrorCode != null)
        {
          log.fine("Reading fault element: " + standardErrorCode.getCode() + " - " + errorDescription);
          return new ActionException(standardErrorCode, errorDescription, false);
        }
        log.fine("Reading fault element: " + numericCode + " - " + errorDescription);
        return new ActionException(numericCode, errorDescription);
      }
      catch (NumberFormatException ex)
      {
        throw new RuntimeException("Error code was not a number");
      }
    }
    throw new RuntimeException("Received fault element but no error code");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\impl\PullSOAPActionProcessorImpl.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */