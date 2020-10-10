package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.istack.Nullable;
import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import java.util.Collection;
import javax.xml.namespace.QName;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class XsiTypeLoader
  extends Loader
{
  private final JaxBeanInfo defaultBeanInfo;
  
  public XsiTypeLoader(JaxBeanInfo defaultBeanInfo)
  {
    super(true);
    this.defaultBeanInfo = defaultBeanInfo;
  }
  
  public void startElement(UnmarshallingContext.State state, TagName ea)
    throws SAXException
  {
    JaxBeanInfo beanInfo = parseXsiType(state, ea, this.defaultBeanInfo);
    if (beanInfo == null) {
      beanInfo = this.defaultBeanInfo;
    }
    Loader loader = beanInfo.getLoader(null, false);
    state.loader = loader;
    loader.startElement(state, ea);
  }
  
  static JaxBeanInfo parseXsiType(UnmarshallingContext.State state, TagName ea, @Nullable JaxBeanInfo defaultBeanInfo)
    throws SAXException
  {
    UnmarshallingContext context = state.getContext();
    JaxBeanInfo beanInfo = null;
    
    Attributes atts = ea.atts;
    int idx = atts.getIndex("http://www.w3.org/2001/XMLSchema-instance", "type");
    if (idx >= 0)
    {
      String value = atts.getValue(idx);
      
      QName type = DatatypeConverterImpl._parseQName(value, context);
      if (type == null)
      {
        reportError(Messages.NOT_A_QNAME.format(new Object[] { value }), true);
      }
      else
      {
        if ((defaultBeanInfo != null) && (defaultBeanInfo.getTypeNames().contains(type))) {
          return defaultBeanInfo;
        }
        beanInfo = context.getJAXBContext().getGlobalType(type);
        if (beanInfo == null)
        {
          String nearest = context.getJAXBContext().getNearestTypeName(type);
          if (nearest != null) {
            reportError(Messages.UNRECOGNIZED_TYPE_NAME_MAYBE.format(new Object[] { type, nearest }), true);
          } else {
            reportError(Messages.UNRECOGNIZED_TYPE_NAME.format(new Object[] { type }), true);
          }
        }
      }
    }
    return beanInfo;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\unmarshaller\XsiTypeLoader.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */