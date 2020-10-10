package org.fourthline.cling.support.model.dlna;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.seamless.util.Exceptions;

public abstract class DLNAAttribute<T>
{
  private static final Logger log = Logger.getLogger(DLNAAttribute.class.getName());
  private T value;
  
  public static enum Type
  {
    DLNA_ORG_PN("DLNA.ORG_PN", new Class[] { DLNAProfileAttribute.class }),  DLNA_ORG_OP("DLNA.ORG_OP", new Class[] { DLNAOperationsAttribute.class }),  DLNA_ORG_PS("DLNA.ORG_PS", new Class[] { DLNAPlaySpeedAttribute.class }),  DLNA_ORG_CI("DLNA.ORG_CI", new Class[] { DLNAConversionIndicatorAttribute.class }),  DLNA_ORG_FLAGS("DLNA.ORG_FLAGS", new Class[] { DLNAFlagsAttribute.class });
    
    private static Map<String, Type> byName = new HashMap() {};
    private String attributeName;
    private Class<? extends DLNAAttribute>[] attributeTypes;
    
    @SafeVarargs
    private Type(String attributeName, Class<? extends DLNAAttribute>... attributeClass)
    {
      this.attributeName = attributeName;
      this.attributeTypes = attributeClass;
    }
    
    public String getAttributeName()
    {
      return this.attributeName;
    }
    
    public Class<? extends DLNAAttribute>[] getAttributeTypes()
    {
      return this.attributeTypes;
    }
    
    public static Type valueOfAttributeName(String attributeName)
    {
      if (attributeName == null) {
        return null;
      }
      return (Type)byName.get(attributeName.toUpperCase(Locale.ROOT));
    }
  }
  
  public void setValue(T value)
  {
    this.value = value;
  }
  
  public T getValue()
  {
    return (T)this.value;
  }
  
  public abstract void setString(String paramString1, String paramString2)
    throws InvalidDLNAProtocolAttributeException;
  
  public abstract String getString();
  
  public static DLNAAttribute newInstance(Type type, String attributeValue, String contentFormat)
  {
    DLNAAttribute attr = null;
    for (int i = 0; (i < type.getAttributeTypes().length) && (attr == null); i++)
    {
      Class<? extends DLNAAttribute> attributeClass = type.getAttributeTypes()[i];
      try
      {
        log.finest("Trying to parse DLNA '" + type + "' with class: " + attributeClass.getSimpleName());
        attr = (DLNAAttribute)attributeClass.newInstance();
        if (attributeValue != null) {
          attr.setString(attributeValue, contentFormat);
        }
      }
      catch (InvalidDLNAProtocolAttributeException ex)
      {
        log.finest("Invalid DLNA attribute value for tested type: " + attributeClass.getSimpleName() + " - " + ex.getMessage());
        attr = null;
      }
      catch (Exception ex)
      {
        log.severe("Error instantiating DLNA attribute of type '" + type + "' with value: " + attributeValue);
        log.log(Level.SEVERE, "Exception root cause: ", Exceptions.unwrap(ex));
      }
    }
    return attr;
  }
  
  public String toString()
  {
    return "(" + getClass().getSimpleName() + ") '" + getValue() + "'";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\dlna\DLNAAttribute.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */