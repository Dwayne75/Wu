package org.fourthline.cling.binding.xml;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import org.fourthline.cling.binding.staging.MutableAction;
import org.fourthline.cling.binding.staging.MutableActionArgument;
import org.fourthline.cling.binding.staging.MutableAllowedValueRange;
import org.fourthline.cling.binding.staging.MutableService;
import org.fourthline.cling.binding.staging.MutableStateVariable;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.ActionArgument.Direction;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.meta.StateVariableEventDetails;
import org.fourthline.cling.model.types.CustomDatatype;
import org.fourthline.cling.model.types.Datatype.Builtin;
import org.seamless.xml.SAXParser;
import org.seamless.xml.SAXParser.Handler;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class UDA10ServiceDescriptorBinderSAXImpl
  extends UDA10ServiceDescriptorBinderImpl
{
  private static Logger log = Logger.getLogger(ServiceDescriptorBinder.class.getName());
  
  public <S extends Service> S describe(S undescribedService, String descriptorXml)
    throws DescriptorBindingException, ValidationException
  {
    if ((descriptorXml == null) || (descriptorXml.length() == 0)) {
      throw new DescriptorBindingException("Null or empty descriptor");
    }
    try
    {
      log.fine("Reading service from XML descriptor");
      
      SAXParser parser = new SAXParser();
      
      MutableService descriptor = new MutableService();
      
      hydrateBasic(descriptor, undescribedService);
      
      new RootHandler(descriptor, parser);
      
      parser.parse(new InputSource(new StringReader(descriptorXml
      
        .trim())));
      
      return descriptor.build(undescribedService.getDevice());
    }
    catch (ValidationException ex)
    {
      throw ex;
    }
    catch (Exception ex)
    {
      throw new DescriptorBindingException("Could not parse service descriptor: " + ex.toString(), ex);
    }
  }
  
  protected static class RootHandler
    extends UDA10ServiceDescriptorBinderSAXImpl.ServiceDescriptorHandler<MutableService>
  {
    public RootHandler(MutableService instance, SAXParser parser)
    {
      super(parser);
    }
    
    public void startElement(Descriptor.Service.ELEMENT element, Attributes attributes)
      throws SAXException
    {
      if (element.equals(UDA10ServiceDescriptorBinderSAXImpl.ActionListHandler.EL))
      {
        List<MutableAction> actions = new ArrayList();
        ((MutableService)getInstance()).actions = actions;
        new UDA10ServiceDescriptorBinderSAXImpl.ActionListHandler(actions, this);
      }
      if (element.equals(UDA10ServiceDescriptorBinderSAXImpl.StateVariableListHandler.EL))
      {
        List<MutableStateVariable> stateVariables = new ArrayList();
        ((MutableService)getInstance()).stateVariables = stateVariables;
        new UDA10ServiceDescriptorBinderSAXImpl.StateVariableListHandler(stateVariables, this);
      }
    }
  }
  
  protected static class ActionListHandler
    extends UDA10ServiceDescriptorBinderSAXImpl.ServiceDescriptorHandler<List<MutableAction>>
  {
    public static final Descriptor.Service.ELEMENT EL = Descriptor.Service.ELEMENT.actionList;
    
    public ActionListHandler(List<MutableAction> instance, UDA10ServiceDescriptorBinderSAXImpl.ServiceDescriptorHandler parent)
    {
      super(parent);
    }
    
    public void startElement(Descriptor.Service.ELEMENT element, Attributes attributes)
      throws SAXException
    {
      if (element.equals(UDA10ServiceDescriptorBinderSAXImpl.ActionHandler.EL))
      {
        MutableAction action = new MutableAction();
        ((List)getInstance()).add(action);
        new UDA10ServiceDescriptorBinderSAXImpl.ActionHandler(action, this);
      }
    }
    
    public boolean isLastElement(Descriptor.Service.ELEMENT element)
    {
      return element.equals(EL);
    }
  }
  
  protected static class ActionHandler
    extends UDA10ServiceDescriptorBinderSAXImpl.ServiceDescriptorHandler<MutableAction>
  {
    public static final Descriptor.Service.ELEMENT EL = Descriptor.Service.ELEMENT.action;
    
    public ActionHandler(MutableAction instance, UDA10ServiceDescriptorBinderSAXImpl.ServiceDescriptorHandler parent)
    {
      super(parent);
    }
    
    public void startElement(Descriptor.Service.ELEMENT element, Attributes attributes)
      throws SAXException
    {
      if (element.equals(UDA10ServiceDescriptorBinderSAXImpl.ActionArgumentListHandler.EL))
      {
        List<MutableActionArgument> arguments = new ArrayList();
        ((MutableAction)getInstance()).arguments = arguments;
        new UDA10ServiceDescriptorBinderSAXImpl.ActionArgumentListHandler(arguments, this);
      }
    }
    
    public void endElement(Descriptor.Service.ELEMENT element)
      throws SAXException
    {
      switch (UDA10ServiceDescriptorBinderSAXImpl.1.$SwitchMap$org$fourthline$cling$binding$xml$Descriptor$Service$ELEMENT[element.ordinal()])
      {
      case 1: 
        ((MutableAction)getInstance()).name = getCharacters();
      }
    }
    
    public boolean isLastElement(Descriptor.Service.ELEMENT element)
    {
      return element.equals(EL);
    }
  }
  
  protected static class ActionArgumentListHandler
    extends UDA10ServiceDescriptorBinderSAXImpl.ServiceDescriptorHandler<List<MutableActionArgument>>
  {
    public static final Descriptor.Service.ELEMENT EL = Descriptor.Service.ELEMENT.argumentList;
    
    public ActionArgumentListHandler(List<MutableActionArgument> instance, UDA10ServiceDescriptorBinderSAXImpl.ServiceDescriptorHandler parent)
    {
      super(parent);
    }
    
    public void startElement(Descriptor.Service.ELEMENT element, Attributes attributes)
      throws SAXException
    {
      if (element.equals(UDA10ServiceDescriptorBinderSAXImpl.ActionArgumentHandler.EL))
      {
        MutableActionArgument argument = new MutableActionArgument();
        ((List)getInstance()).add(argument);
        new UDA10ServiceDescriptorBinderSAXImpl.ActionArgumentHandler(argument, this);
      }
    }
    
    public boolean isLastElement(Descriptor.Service.ELEMENT element)
    {
      return element.equals(EL);
    }
  }
  
  protected static class ActionArgumentHandler
    extends UDA10ServiceDescriptorBinderSAXImpl.ServiceDescriptorHandler<MutableActionArgument>
  {
    public static final Descriptor.Service.ELEMENT EL = Descriptor.Service.ELEMENT.argument;
    
    public ActionArgumentHandler(MutableActionArgument instance, UDA10ServiceDescriptorBinderSAXImpl.ServiceDescriptorHandler parent)
    {
      super(parent);
    }
    
    public void endElement(Descriptor.Service.ELEMENT element)
      throws SAXException
    {
      switch (UDA10ServiceDescriptorBinderSAXImpl.1.$SwitchMap$org$fourthline$cling$binding$xml$Descriptor$Service$ELEMENT[element.ordinal()])
      {
      case 1: 
        ((MutableActionArgument)getInstance()).name = getCharacters();
        break;
      case 2: 
        String directionString = getCharacters();
        try
        {
          ((MutableActionArgument)getInstance()).direction = ActionArgument.Direction.valueOf(directionString.toUpperCase(Locale.ROOT));
        }
        catch (IllegalArgumentException ex)
        {
          UDA10ServiceDescriptorBinderSAXImpl.log.warning("UPnP specification violation: Invalid action argument direction, assuming 'IN': " + directionString);
          ((MutableActionArgument)getInstance()).direction = ActionArgument.Direction.IN;
        }
      case 3: 
        ((MutableActionArgument)getInstance()).relatedStateVariable = getCharacters();
        break;
      case 4: 
        ((MutableActionArgument)getInstance()).retval = true;
      }
    }
    
    public boolean isLastElement(Descriptor.Service.ELEMENT element)
    {
      return element.equals(EL);
    }
  }
  
  protected static class StateVariableListHandler
    extends UDA10ServiceDescriptorBinderSAXImpl.ServiceDescriptorHandler<List<MutableStateVariable>>
  {
    public static final Descriptor.Service.ELEMENT EL = Descriptor.Service.ELEMENT.serviceStateTable;
    
    public StateVariableListHandler(List<MutableStateVariable> instance, UDA10ServiceDescriptorBinderSAXImpl.ServiceDescriptorHandler parent)
    {
      super(parent);
    }
    
    public void startElement(Descriptor.Service.ELEMENT element, Attributes attributes)
      throws SAXException
    {
      if (element.equals(UDA10ServiceDescriptorBinderSAXImpl.StateVariableHandler.EL))
      {
        MutableStateVariable stateVariable = new MutableStateVariable();
        
        String sendEventsAttributeValue = attributes.getValue(Descriptor.Service.ATTRIBUTE.sendEvents.toString());
        
        stateVariable.eventDetails = new StateVariableEventDetails((sendEventsAttributeValue != null) && (sendEventsAttributeValue.toUpperCase(Locale.ROOT).equals("YES")));
        
        ((List)getInstance()).add(stateVariable);
        new UDA10ServiceDescriptorBinderSAXImpl.StateVariableHandler(stateVariable, this);
      }
    }
    
    public boolean isLastElement(Descriptor.Service.ELEMENT element)
    {
      return element.equals(EL);
    }
  }
  
  protected static class StateVariableHandler
    extends UDA10ServiceDescriptorBinderSAXImpl.ServiceDescriptorHandler<MutableStateVariable>
  {
    public static final Descriptor.Service.ELEMENT EL = Descriptor.Service.ELEMENT.stateVariable;
    
    public StateVariableHandler(MutableStateVariable instance, UDA10ServiceDescriptorBinderSAXImpl.ServiceDescriptorHandler parent)
    {
      super(parent);
    }
    
    public void startElement(Descriptor.Service.ELEMENT element, Attributes attributes)
      throws SAXException
    {
      if (element.equals(UDA10ServiceDescriptorBinderSAXImpl.AllowedValueListHandler.EL))
      {
        List<String> allowedValues = new ArrayList();
        ((MutableStateVariable)getInstance()).allowedValues = allowedValues;
        new UDA10ServiceDescriptorBinderSAXImpl.AllowedValueListHandler(allowedValues, this);
      }
      if (element.equals(UDA10ServiceDescriptorBinderSAXImpl.AllowedValueRangeHandler.EL))
      {
        MutableAllowedValueRange allowedValueRange = new MutableAllowedValueRange();
        ((MutableStateVariable)getInstance()).allowedValueRange = allowedValueRange;
        new UDA10ServiceDescriptorBinderSAXImpl.AllowedValueRangeHandler(allowedValueRange, this);
      }
    }
    
    public void endElement(Descriptor.Service.ELEMENT element)
      throws SAXException
    {
      switch (UDA10ServiceDescriptorBinderSAXImpl.1.$SwitchMap$org$fourthline$cling$binding$xml$Descriptor$Service$ELEMENT[element.ordinal()])
      {
      case 1: 
        ((MutableStateVariable)getInstance()).name = getCharacters();
        break;
      case 5: 
        String dtName = getCharacters();
        Datatype.Builtin builtin = Datatype.Builtin.getByDescriptorName(dtName);
        ((MutableStateVariable)getInstance()).dataType = (builtin != null ? builtin.getDatatype() : new CustomDatatype(dtName));
        break;
      case 6: 
        ((MutableStateVariable)getInstance()).defaultValue = getCharacters();
      }
    }
    
    public boolean isLastElement(Descriptor.Service.ELEMENT element)
    {
      return element.equals(EL);
    }
  }
  
  protected static class AllowedValueListHandler
    extends UDA10ServiceDescriptorBinderSAXImpl.ServiceDescriptorHandler<List<String>>
  {
    public static final Descriptor.Service.ELEMENT EL = Descriptor.Service.ELEMENT.allowedValueList;
    
    public AllowedValueListHandler(List<String> instance, UDA10ServiceDescriptorBinderSAXImpl.ServiceDescriptorHandler parent)
    {
      super(parent);
    }
    
    public void endElement(Descriptor.Service.ELEMENT element)
      throws SAXException
    {
      switch (UDA10ServiceDescriptorBinderSAXImpl.1.$SwitchMap$org$fourthline$cling$binding$xml$Descriptor$Service$ELEMENT[element.ordinal()])
      {
      case 7: 
        ((List)getInstance()).add(getCharacters());
      }
    }
    
    public boolean isLastElement(Descriptor.Service.ELEMENT element)
    {
      return element.equals(EL);
    }
  }
  
  protected static class AllowedValueRangeHandler
    extends UDA10ServiceDescriptorBinderSAXImpl.ServiceDescriptorHandler<MutableAllowedValueRange>
  {
    public static final Descriptor.Service.ELEMENT EL = Descriptor.Service.ELEMENT.allowedValueRange;
    
    public AllowedValueRangeHandler(MutableAllowedValueRange instance, UDA10ServiceDescriptorBinderSAXImpl.ServiceDescriptorHandler parent)
    {
      super(parent);
    }
    
    public void endElement(Descriptor.Service.ELEMENT element)
      throws SAXException
    {
      try
      {
        switch (UDA10ServiceDescriptorBinderSAXImpl.1.$SwitchMap$org$fourthline$cling$binding$xml$Descriptor$Service$ELEMENT[element.ordinal()])
        {
        case 8: 
          ((MutableAllowedValueRange)getInstance()).minimum = Long.valueOf(getCharacters());
          break;
        case 9: 
          ((MutableAllowedValueRange)getInstance()).maximum = Long.valueOf(getCharacters());
          break;
        case 10: 
          ((MutableAllowedValueRange)getInstance()).step = Long.valueOf(getCharacters());
        }
      }
      catch (Exception localException) {}
    }
    
    public boolean isLastElement(Descriptor.Service.ELEMENT element)
    {
      return element.equals(EL);
    }
  }
  
  protected static class ServiceDescriptorHandler<I>
    extends SAXParser.Handler<I>
  {
    public ServiceDescriptorHandler(I instance)
    {
      super();
    }
    
    public ServiceDescriptorHandler(I instance, SAXParser parser)
    {
      super(parser);
    }
    
    public ServiceDescriptorHandler(I instance, ServiceDescriptorHandler parent)
    {
      super(parent);
    }
    
    public ServiceDescriptorHandler(I instance, SAXParser parser, ServiceDescriptorHandler parent)
    {
      super(parser, parent);
    }
    
    public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws SAXException
    {
      super.startElement(uri, localName, qName, attributes);
      Descriptor.Service.ELEMENT el = Descriptor.Service.ELEMENT.valueOrNullOf(localName);
      if (el == null) {
        return;
      }
      startElement(el, attributes);
    }
    
    public void endElement(String uri, String localName, String qName)
      throws SAXException
    {
      super.endElement(uri, localName, qName);
      Descriptor.Service.ELEMENT el = Descriptor.Service.ELEMENT.valueOrNullOf(localName);
      if (el == null) {
        return;
      }
      endElement(el);
    }
    
    protected boolean isLastElement(String uri, String localName, String qName)
    {
      Descriptor.Service.ELEMENT el = Descriptor.Service.ELEMENT.valueOrNullOf(localName);
      return (el != null) && (isLastElement(el));
    }
    
    public void startElement(Descriptor.Service.ELEMENT element, Attributes attributes)
      throws SAXException
    {}
    
    public void endElement(Descriptor.Service.ELEMENT element)
      throws SAXException
    {}
    
    public boolean isLastElement(Descriptor.Service.ELEMENT element)
    {
      return false;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\binding\xml\UDA10ServiceDescriptorBinderSAXImpl.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */