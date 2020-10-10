package org.fourthline.cling.binding.xml;

import java.util.Locale;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.Device;
import org.seamless.util.Exceptions;
import org.seamless.xml.ParserException;
import org.seamless.xml.XmlPullParserUtils;
import org.xml.sax.SAXParseException;

public class RecoveringUDA10DeviceDescriptorBinderImpl
  extends UDA10DeviceDescriptorBinderImpl
{
  private static Logger log = Logger.getLogger(RecoveringUDA10DeviceDescriptorBinderImpl.class.getName());
  
  public <D extends Device> D describe(D undescribedDevice, String descriptorXml)
    throws DescriptorBindingException, ValidationException
  {
    D device = null;
    try
    {
      if (descriptorXml != null) {
        descriptorXml = descriptorXml.trim();
      }
      return super.describe(undescribedDevice, descriptorXml);
    }
    catch (DescriptorBindingException ex)
    {
      log.warning("Regular parsing failed: " + Exceptions.unwrap(ex).getMessage());
      DescriptorBindingException originalException = ex;
      
      String fixedXml = fixGarbageLeadingChars(descriptorXml);
      if (fixedXml != null) {
        try
        {
          return super.describe(undescribedDevice, fixedXml);
        }
        catch (DescriptorBindingException ex)
        {
          log.warning("Removing leading garbage didn't work: " + Exceptions.unwrap(ex).getMessage());
        }
      }
      fixedXml = fixGarbageTrailingChars(descriptorXml, originalException);
      if (fixedXml != null) {
        try
        {
          return super.describe(undescribedDevice, fixedXml);
        }
        catch (DescriptorBindingException ex)
        {
          log.warning("Removing trailing garbage didn't work: " + Exceptions.unwrap(ex).getMessage());
        }
      }
      DescriptorBindingException lastException = originalException;
      fixedXml = descriptorXml;
      for (int retryCount = 0; retryCount < 5; retryCount++)
      {
        fixedXml = fixMissingNamespaces(fixedXml, lastException);
        if (fixedXml == null) {
          break;
        }
        try
        {
          return super.describe(undescribedDevice, fixedXml);
        }
        catch (DescriptorBindingException ex)
        {
          log.warning("Fixing namespace prefix didn't work: " + Exceptions.unwrap(ex).getMessage());
          lastException = ex;
        }
      }
      fixedXml = XmlPullParserUtils.fixXMLEntities(descriptorXml);
      if (!fixedXml.equals(descriptorXml)) {
        try
        {
          return super.describe(undescribedDevice, fixedXml);
        }
        catch (DescriptorBindingException ex)
        {
          log.warning("Fixing XML entities didn't work: " + Exceptions.unwrap(ex).getMessage());
        }
      }
      handleInvalidDescriptor(descriptorXml, originalException);
    }
    catch (ValidationException ex)
    {
      device = handleInvalidDevice(descriptorXml, device, ex);
      if (device != null) {
        return device;
      }
    }
    throw new IllegalStateException("No device produced, did you swallow exceptions in your subclass?");
  }
  
  private String fixGarbageLeadingChars(String descriptorXml)
  {
    int index = descriptorXml.indexOf("<?xml");
    if (index == -1) {
      return descriptorXml;
    }
    return descriptorXml.substring(index);
  }
  
  protected String fixGarbageTrailingChars(String descriptorXml, DescriptorBindingException ex)
  {
    int index = descriptorXml.indexOf("</root>");
    if (index == -1)
    {
      log.warning("No closing </root> element in descriptor");
      return null;
    }
    if (descriptorXml.length() != index + "</root>".length())
    {
      log.warning("Detected garbage characters after <root> node, removing");
      return descriptorXml.substring(0, index) + "</root>";
    }
    return null;
  }
  
  protected String fixMissingNamespaces(String descriptorXml, DescriptorBindingException ex)
  {
    Throwable cause = ex.getCause();
    if ((!(cause instanceof SAXParseException)) && (!(cause instanceof ParserException))) {
      return null;
    }
    String message = cause.getMessage();
    if (message == null) {
      return null;
    }
    Pattern pattern = Pattern.compile("The prefix \"(.*)\" for element");
    Matcher matcher = pattern.matcher(message);
    if ((!matcher.find()) || (matcher.groupCount() != 1))
    {
      pattern = Pattern.compile("undefined prefix: ([^ ]*)");
      matcher = pattern.matcher(message);
      if ((!matcher.find()) || (matcher.groupCount() != 1)) {
        return null;
      }
    }
    String missingNS = matcher.group(1);
    log.warning("Fixing missing namespace declaration for: " + missingNS);
    
    pattern = Pattern.compile("<root([^>]*)");
    matcher = pattern.matcher(descriptorXml);
    if ((!matcher.find()) || (matcher.groupCount() != 1))
    {
      log.fine("Could not find <root> element attributes");
      return null;
    }
    String rootAttributes = matcher.group(1);
    log.fine("Preserving existing <root> element attributes/namespace declarations: " + matcher.group(0));
    
    pattern = Pattern.compile("<root[^>]*>(.*)</root>", 32);
    matcher = pattern.matcher(descriptorXml);
    if ((!matcher.find()) || (matcher.groupCount() != 1))
    {
      log.fine("Could not extract body of <root> element");
      return null;
    }
    String rootBody = matcher.group(1);
    
    return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root " + String.format(Locale.ROOT, "xmlns:%s=\"urn:schemas-dlna-org:device-1-0\"", new Object[] { missingNS }) + rootAttributes + ">" + rootBody + "</root>";
  }
  
  protected void handleInvalidDescriptor(String xml, DescriptorBindingException exception)
    throws DescriptorBindingException
  {
    throw exception;
  }
  
  protected <D extends Device> D handleInvalidDevice(String xml, D device, ValidationException exception)
    throws ValidationException
  {
    throw exception;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\binding\xml\RecoveringUDA10DeviceDescriptorBinderImpl.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */