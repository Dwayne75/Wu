package com.sun.tools.xjc.reader.xmlschema;

import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.visitor.XSFunction;

public abstract class AbstractXSFunctionImpl
  implements XSFunction
{
  public Object annotation(XSAnnotation ann)
  {
    _assert(false);
    return null;
  }
  
  public Object schema(XSSchema schema)
  {
    _assert(false);
    return null;
  }
  
  public Object facet(XSFacet facet)
  {
    _assert(false);
    return null;
  }
  
  public Object notation(XSNotation not)
  {
    _assert(false);
    return null;
  }
  
  protected static void _assert(boolean b)
  {
    if (!b) {
      throw new JAXBAssertionError();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\AbstractXSFunctionImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */