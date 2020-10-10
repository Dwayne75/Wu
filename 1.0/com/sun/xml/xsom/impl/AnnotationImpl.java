package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSAnnotation;
import org.xml.sax.Locator;

public class AnnotationImpl
  implements XSAnnotation
{
  private final Object annotation;
  private final Locator locator;
  
  public Object getAnnotation()
  {
    return this.annotation;
  }
  
  public Locator getLocator()
  {
    return this.locator;
  }
  
  public AnnotationImpl(Object o, Locator _loc)
  {
    this.annotation = o;
    this.locator = _loc;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\impl\AnnotationImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */