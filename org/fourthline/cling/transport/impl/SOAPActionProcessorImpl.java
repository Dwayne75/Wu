package org.fourthline.cling.transport.impl;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import org.fourthline.cling.model.UnsupportedDataException;
import org.fourthline.cling.model.XMLUtil;
import org.fourthline.cling.model.action.ActionArgumentValue;
import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.control.ActionMessage;
import org.fourthline.cling.model.message.control.ActionRequestMessage;
import org.fourthline.cling.model.message.control.ActionResponseMessage;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.ActionArgument;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.model.types.InvalidValueException;
import org.fourthline.cling.transport.spi.SOAPActionProcessor;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class SOAPActionProcessorImpl
  implements SOAPActionProcessor, ErrorHandler
{
  private static Logger log = Logger.getLogger(SOAPActionProcessor.class.getName());
  
  protected DocumentBuilderFactory createDocumentBuilderFactory()
    throws FactoryConfigurationError
  {
    return DocumentBuilderFactory.newInstance();
  }
  
  public void writeBody(ActionRequestMessage requestMessage, ActionInvocation actionInvocation)
    throws UnsupportedDataException
  {
    log.fine("Writing body of " + requestMessage + " for: " + actionInvocation);
    try
    {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      Document d = factory.newDocumentBuilder().newDocument();
      Element body = writeBodyElement(d);
      
      writeBodyRequest(d, body, requestMessage, actionInvocation);
      if (log.isLoggable(Level.FINER))
      {
        log.finer("===================================== SOAP BODY BEGIN ============================================");
        log.finer(requestMessage.getBodyString());
        log.finer("-===================================== SOAP BODY END ============================================");
      }
    }
    catch (Exception ex)
    {
      throw new UnsupportedDataException("Can't transform message payload: " + ex, ex);
    }
  }
  
  public void writeBody(ActionResponseMessage responseMessage, ActionInvocation actionInvocation)
    throws UnsupportedDataException
  {
    log.fine("Writing body of " + responseMessage + " for: " + actionInvocation);
    try
    {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      Document d = factory.newDocumentBuilder().newDocument();
      Element body = writeBodyElement(d);
      if (actionInvocation.getFailure() != null) {
        writeBodyFailure(d, body, responseMessage, actionInvocation);
      } else {
        writeBodyResponse(d, body, responseMessage, actionInvocation);
      }
      if (log.isLoggable(Level.FINER))
      {
        log.finer("===================================== SOAP BODY BEGIN ============================================");
        log.finer(responseMessage.getBodyString());
        log.finer("-===================================== SOAP BODY END ============================================");
      }
    }
    catch (Exception ex)
    {
      throw new UnsupportedDataException("Can't transform message payload: " + ex, ex);
    }
  }
  
  public void readBody(ActionRequestMessage requestMessage, ActionInvocation actionInvocation)
    throws UnsupportedDataException
  {
    log.fine("Reading body of " + requestMessage + " for: " + actionInvocation);
    if (log.isLoggable(Level.FINER))
    {
      log.finer("===================================== SOAP BODY BEGIN ============================================");
      log.finer(requestMessage.getBodyString());
      log.finer("-===================================== SOAP BODY END ============================================");
    }
    String body = getMessageBody(requestMessage);
    try
    {
      DocumentBuilderFactory factory = createDocumentBuilderFactory();
      factory.setNamespaceAware(true);
      DocumentBuilder documentBuilder = factory.newDocumentBuilder();
      documentBuilder.setErrorHandler(this);
      
      Document d = documentBuilder.parse(new InputSource(new StringReader(body)));
      
      Element bodyElement = readBodyElement(d);
      
      readBodyRequest(d, bodyElement, requestMessage, actionInvocation);
    }
    catch (Exception ex)
    {
      throw new UnsupportedDataException("Can't transform message payload: " + ex, ex, body);
    }
  }
  
  public void readBody(ActionResponseMessage responseMsg, ActionInvocation actionInvocation)
    throws UnsupportedDataException
  {
    log.fine("Reading body of " + responseMsg + " for: " + actionInvocation);
    if (log.isLoggable(Level.FINER))
    {
      log.finer("===================================== SOAP BODY BEGIN ============================================");
      log.finer(responseMsg.getBodyString());
      log.finer("-===================================== SOAP BODY END ============================================");
    }
    String body = getMessageBody(responseMsg);
    try
    {
      DocumentBuilderFactory factory = createDocumentBuilderFactory();
      factory.setNamespaceAware(true);
      DocumentBuilder documentBuilder = factory.newDocumentBuilder();
      documentBuilder.setErrorHandler(this);
      
      Document d = documentBuilder.parse(new InputSource(new StringReader(body)));
      
      Element bodyElement = readBodyElement(d);
      
      ActionException failure = readBodyFailure(d, bodyElement);
      if (failure == null) {
        readBodyResponse(d, bodyElement, responseMsg, actionInvocation);
      } else {
        actionInvocation.setFailure(failure);
      }
    }
    catch (Exception ex)
    {
      throw new UnsupportedDataException("Can't transform message payload: " + ex, ex, body);
    }
  }
  
  protected void writeBodyFailure(Document d, Element bodyElement, ActionResponseMessage message, ActionInvocation actionInvocation)
    throws Exception
  {
    writeFaultElement(d, bodyElement, actionInvocation);
    message.setBody(toString(d));
  }
  
  protected void writeBodyRequest(Document d, Element bodyElement, ActionRequestMessage message, ActionInvocation actionInvocation)
    throws Exception
  {
    Element actionRequestElement = writeActionRequestElement(d, bodyElement, message, actionInvocation);
    writeActionInputArguments(d, actionRequestElement, actionInvocation);
    message.setBody(toString(d));
  }
  
  protected void writeBodyResponse(Document d, Element bodyElement, ActionResponseMessage message, ActionInvocation actionInvocation)
    throws Exception
  {
    Element actionResponseElement = writeActionResponseElement(d, bodyElement, message, actionInvocation);
    writeActionOutputArguments(d, actionResponseElement, actionInvocation);
    message.setBody(toString(d));
  }
  
  protected ActionException readBodyFailure(Document d, Element bodyElement)
    throws Exception
  {
    return readFaultElement(bodyElement);
  }
  
  protected void readBodyRequest(Document d, Element bodyElement, ActionRequestMessage message, ActionInvocation actionInvocation)
    throws Exception
  {
    Element actionRequestElement = readActionRequestElement(bodyElement, message, actionInvocation);
    readActionInputArguments(actionRequestElement, actionInvocation);
  }
  
  protected void readBodyResponse(Document d, Element bodyElement, ActionResponseMessage message, ActionInvocation actionInvocation)
    throws Exception
  {
    Element actionResponse = readActionResponseElement(bodyElement, actionInvocation);
    readActionOutputArguments(actionResponse, actionInvocation);
  }
  
  protected Element writeBodyElement(Document d)
  {
    Element envelopeElement = d.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "s:Envelope");
    Attr encodingStyleAttr = d.createAttributeNS("http://schemas.xmlsoap.org/soap/envelope/", "s:encodingStyle");
    encodingStyleAttr.setValue("http://schemas.xmlsoap.org/soap/encoding/");
    envelopeElement.setAttributeNode(encodingStyleAttr);
    d.appendChild(envelopeElement);
    
    Element bodyElement = d.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "s:Body");
    envelopeElement.appendChild(bodyElement);
    
    return bodyElement;
  }
  
  protected Element readBodyElement(Document d)
  {
    Element envelopeElement = d.getDocumentElement();
    if ((envelopeElement == null) || (!getUnprefixedNodeName(envelopeElement).equals("Envelope"))) {
      throw new RuntimeException("Response root element was not 'Envelope'");
    }
    NodeList envelopeElementChildren = envelopeElement.getChildNodes();
    for (int i = 0; i < envelopeElementChildren.getLength(); i++)
    {
      Node envelopeChild = envelopeElementChildren.item(i);
      if (envelopeChild.getNodeType() == 1) {
        if (getUnprefixedNodeName(envelopeChild).equals("Body")) {
          return (Element)envelopeChild;
        }
      }
    }
    throw new RuntimeException("Response envelope did not contain 'Body' child element");
  }
  
  protected Element writeActionRequestElement(Document d, Element bodyElement, ActionRequestMessage message, ActionInvocation actionInvocation)
  {
    log.fine("Writing action request element: " + actionInvocation.getAction().getName());
    
    Element actionRequestElement = d.createElementNS(message
      .getActionNamespace(), "u:" + actionInvocation
      .getAction().getName());
    
    bodyElement.appendChild(actionRequestElement);
    
    return actionRequestElement;
  }
  
  protected Element readActionRequestElement(Element bodyElement, ActionRequestMessage message, ActionInvocation actionInvocation)
  {
    NodeList bodyChildren = bodyElement.getChildNodes();
    
    log.fine("Looking for action request element matching namespace:" + message.getActionNamespace());
    for (int i = 0; i < bodyChildren.getLength(); i++)
    {
      Node bodyChild = bodyChildren.item(i);
      if (bodyChild.getNodeType() == 1)
      {
        String unprefixedName = getUnprefixedNodeName(bodyChild);
        if (unprefixedName.equals(actionInvocation.getAction().getName()))
        {
          if ((bodyChild.getNamespaceURI() == null) || 
            (!bodyChild.getNamespaceURI().equals(message.getActionNamespace()))) {
            throw new UnsupportedDataException("Illegal or missing namespace on action request element: " + bodyChild);
          }
          log.fine("Reading action request element: " + unprefixedName);
          return (Element)bodyChild;
        }
      }
    }
    throw new UnsupportedDataException("Could not read action request element matching namespace: " + message.getActionNamespace());
  }
  
  protected Element writeActionResponseElement(Document d, Element bodyElement, ActionResponseMessage message, ActionInvocation actionInvocation)
  {
    log.fine("Writing action response element: " + actionInvocation.getAction().getName());
    Element actionResponseElement = d.createElementNS(message
      .getActionNamespace(), "u:" + actionInvocation
      .getAction().getName() + "Response");
    
    bodyElement.appendChild(actionResponseElement);
    
    return actionResponseElement;
  }
  
  protected Element readActionResponseElement(Element bodyElement, ActionInvocation actionInvocation)
  {
    NodeList bodyChildren = bodyElement.getChildNodes();
    for (int i = 0; i < bodyChildren.getLength(); i++)
    {
      Node bodyChild = bodyChildren.item(i);
      if (bodyChild.getNodeType() == 1) {
        if (getUnprefixedNodeName(bodyChild).equals(actionInvocation.getAction().getName() + "Response"))
        {
          log.fine("Reading action response element: " + getUnprefixedNodeName(bodyChild));
          return (Element)bodyChild;
        }
      }
    }
    log.fine("Could not read action response element");
    return null;
  }
  
  protected void writeActionInputArguments(Document d, Element actionRequestElement, ActionInvocation actionInvocation)
  {
    for (ActionArgument argument : actionInvocation.getAction().getInputArguments())
    {
      log.fine("Writing action input argument: " + argument.getName());
      String value = actionInvocation.getInput(argument) != null ? actionInvocation.getInput(argument).toString() : "";
      XMLUtil.appendNewElement(d, actionRequestElement, argument.getName(), value);
    }
  }
  
  public void readActionInputArguments(Element actionRequestElement, ActionInvocation actionInvocation)
    throws ActionException
  {
    actionInvocation.setInput(
      readArgumentValues(actionRequestElement
      .getChildNodes(), actionInvocation
      .getAction().getInputArguments()));
  }
  
  protected void writeActionOutputArguments(Document d, Element actionResponseElement, ActionInvocation actionInvocation)
  {
    for (ActionArgument argument : actionInvocation.getAction().getOutputArguments())
    {
      log.fine("Writing action output argument: " + argument.getName());
      String value = actionInvocation.getOutput(argument) != null ? actionInvocation.getOutput(argument).toString() : "";
      XMLUtil.appendNewElement(d, actionResponseElement, argument.getName(), value);
    }
  }
  
  protected void readActionOutputArguments(Element actionResponseElement, ActionInvocation actionInvocation)
    throws ActionException
  {
    actionInvocation.setOutput(
      readArgumentValues(actionResponseElement
      .getChildNodes(), actionInvocation
      .getAction().getOutputArguments()));
  }
  
  protected void writeFaultElement(Document d, Element bodyElement, ActionInvocation actionInvocation)
  {
    Element faultElement = d.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "s:Fault");
    bodyElement.appendChild(faultElement);
    
    XMLUtil.appendNewElement(d, faultElement, "faultcode", "s:Client");
    XMLUtil.appendNewElement(d, faultElement, "faultstring", "UPnPError");
    
    Element detailElement = d.createElement("detail");
    faultElement.appendChild(detailElement);
    
    Element upnpErrorElement = d.createElementNS("urn:schemas-upnp-org:control-1-0", "UPnPError");
    detailElement.appendChild(upnpErrorElement);
    
    int errorCode = actionInvocation.getFailure().getErrorCode();
    String errorDescription = actionInvocation.getFailure().getMessage();
    
    log.fine("Writing fault element: " + errorCode + " - " + errorDescription);
    
    XMLUtil.appendNewElement(d, upnpErrorElement, "errorCode", Integer.toString(errorCode));
    XMLUtil.appendNewElement(d, upnpErrorElement, "errorDescription", errorDescription);
  }
  
  protected ActionException readFaultElement(Element bodyElement)
  {
    boolean receivedFaultElement = false;
    String errorCode = null;
    String errorDescription = null;
    
    NodeList bodyChildren = bodyElement.getChildNodes();
    for (int i = 0; i < bodyChildren.getLength(); i++)
    {
      Node bodyChild = bodyChildren.item(i);
      if (bodyChild.getNodeType() == 1) {
        if (getUnprefixedNodeName(bodyChild).equals("Fault"))
        {
          receivedFaultElement = true;
          
          NodeList faultChildren = bodyChild.getChildNodes();
          for (int j = 0; j < faultChildren.getLength(); j++)
          {
            Node faultChild = faultChildren.item(j);
            if (faultChild.getNodeType() == 1) {
              if (getUnprefixedNodeName(faultChild).equals("detail"))
              {
                NodeList detailChildren = faultChild.getChildNodes();
                for (int x = 0; x < detailChildren.getLength(); x++)
                {
                  Node detailChild = detailChildren.item(x);
                  if (detailChild.getNodeType() == 1) {
                    if (getUnprefixedNodeName(detailChild).equals("UPnPError"))
                    {
                      NodeList errorChildren = detailChild.getChildNodes();
                      for (int y = 0; y < errorChildren.getLength(); y++)
                      {
                        Node errorChild = errorChildren.item(y);
                        if (errorChild.getNodeType() == 1)
                        {
                          if (getUnprefixedNodeName(errorChild).equals("errorCode")) {
                            errorCode = XMLUtil.getTextContent(errorChild);
                          }
                          if (getUnprefixedNodeName(errorChild).equals("errorDescription")) {
                            errorDescription = XMLUtil.getTextContent(errorChild);
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
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
    if (receivedFaultElement) {
      throw new RuntimeException("Received fault element but no error code");
    }
    return null;
  }
  
  protected String getMessageBody(ActionMessage message)
    throws UnsupportedDataException
  {
    if (!message.isBodyNonEmptyString()) {
      throw new UnsupportedDataException("Can't transform null or non-string/zero-length body of: " + message);
    }
    return message.getBodyString().trim();
  }
  
  protected String toString(Document d)
    throws Exception
  {
    String output = XMLUtil.documentToString(d);
    while ((output.endsWith("\n")) || (output.endsWith("\r"))) {
      output = output.substring(0, output.length() - 1);
    }
    return output;
  }
  
  protected String getUnprefixedNodeName(Node node)
  {
    return node.getPrefix() != null ? node.getNodeName().substring(node.getPrefix().length() + 1) : node.getNodeName();
  }
  
  protected ActionArgumentValue[] readArgumentValues(NodeList nodeList, ActionArgument[] args)
    throws ActionException
  {
    List<Node> nodes = getMatchingNodes(nodeList, args);
    
    ActionArgumentValue[] values = new ActionArgumentValue[args.length];
    for (int i = 0; i < args.length; i++)
    {
      ActionArgument arg = args[i];
      Node node = findActionArgumentNode(nodes, arg);
      if (node == null) {
        throw new ActionException(ErrorCode.ARGUMENT_VALUE_INVALID, "Could not find argument '" + arg.getName() + "' node");
      }
      log.fine("Reading action argument: " + arg.getName());
      String value = XMLUtil.getTextContent(node);
      values[i] = createValue(arg, value);
    }
    return values;
  }
  
  protected List<Node> getMatchingNodes(NodeList nodeList, ActionArgument[] args)
    throws ActionException
  {
    List<String> names = new ArrayList();
    for (ActionArgument argument : args)
    {
      names.add(argument.getName());
      names.addAll(Arrays.asList(argument.getAliases()));
    }
    Object matches = new ArrayList();
    for (int i = 0; i < nodeList.getLength(); i++)
    {
      Node child = nodeList.item(i);
      if (child.getNodeType() == 1) {
        if (names.contains(getUnprefixedNodeName(child))) {
          ((List)matches).add(child);
        }
      }
    }
    if (((List)matches).size() < args.length) {
      throw new ActionException(ErrorCode.ARGUMENT_VALUE_INVALID, "Invalid number of input or output arguments in XML message, expected " + args.length + " but found " + ((List)matches).size());
    }
    return (List<Node>)matches;
  }
  
  protected ActionArgumentValue createValue(ActionArgument arg, String value)
    throws ActionException
  {
    try
    {
      return new ActionArgumentValue(arg, value);
    }
    catch (InvalidValueException ex)
    {
      throw new ActionException(ErrorCode.ARGUMENT_VALUE_INVALID, "Wrong type or invalid value for '" + arg.getName() + "': " + ex.getMessage(), ex);
    }
  }
  
  protected Node findActionArgumentNode(List<Node> nodes, ActionArgument arg)
  {
    for (Node node : nodes) {
      if (arg.isNameOrAlias(getUnprefixedNodeName(node))) {
        return node;
      }
    }
    return null;
  }
  
  public void warning(SAXParseException e)
    throws SAXException
  {
    log.warning(e.toString());
  }
  
  public void error(SAXParseException e)
    throws SAXException
  {
    throw e;
  }
  
  public void fatalError(SAXParseException e)
    throws SAXException
  {
    throw e;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\impl\SOAPActionProcessorImpl.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */