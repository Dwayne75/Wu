package com.sun.xml.bind.v2.runtime;

import com.sun.xml.bind.util.ValidationEventLocatorExImpl;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.helpers.PrintConversionEventImpl;
import javax.xml.bind.helpers.ValidationEventImpl;
import javax.xml.bind.helpers.ValidationEventLocatorImpl;
import org.xml.sax.SAXException;

public class RuntimeUtil
{
  public static final Map<Class, Class> boxToPrimitive;
  public static final Map<Class, Class> primitiveToBox;
  
  public static final class ToStringAdapter
    extends XmlAdapter<String, Object>
  {
    public Object unmarshal(String s)
    {
      throw new UnsupportedOperationException();
    }
    
    public String marshal(Object o)
    {
      if (o == null) {
        return null;
      }
      return o.toString();
    }
  }
  
  static
  {
    Map<Class, Class> b = new HashMap();
    b.put(Byte.TYPE, Byte.class);
    b.put(Short.TYPE, Short.class);
    b.put(Integer.TYPE, Integer.class);
    b.put(Long.TYPE, Long.class);
    b.put(Character.TYPE, Character.class);
    b.put(Boolean.TYPE, Boolean.class);
    b.put(Float.TYPE, Float.class);
    b.put(Double.TYPE, Double.class);
    b.put(Void.TYPE, Void.class);
    
    primitiveToBox = Collections.unmodifiableMap(b);
    
    Map<Class, Class> p = new HashMap();
    for (Map.Entry<Class, Class> e : b.entrySet()) {
      p.put(e.getValue(), e.getKey());
    }
    boxToPrimitive = Collections.unmodifiableMap(p);
  }
  
  public static void handlePrintConversionException(Object caller, Exception e, XMLSerializer serializer)
    throws SAXException
  {
    if ((e instanceof SAXException)) {
      throw ((SAXException)e);
    }
    ValidationEvent ve = new PrintConversionEventImpl(1, e.getMessage(), new ValidationEventLocatorImpl(caller), e);
    
    serializer.reportError(ve);
  }
  
  public static void handleTypeMismatchError(XMLSerializer serializer, Object parentObject, String fieldName, Object childObject)
    throws SAXException
  {
    ValidationEvent ve = new ValidationEventImpl(1, Messages.TYPE_MISMATCH.format(new Object[] { getTypeName(parentObject), fieldName, getTypeName(childObject) }), new ValidationEventLocatorExImpl(parentObject, fieldName));
    
    serializer.reportError(ve);
  }
  
  private static String getTypeName(Object o)
  {
    return o.getClass().getName();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\RuntimeUtil.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */