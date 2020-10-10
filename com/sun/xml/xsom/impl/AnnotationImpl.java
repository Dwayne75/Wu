package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSAnnotation;
import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

public class AnnotationImpl
  implements XSAnnotation
{
  private Object annotation;
  private final Locator locator;
  
  public Object getAnnotation()
  {
    return this.annotation;
  }
  
  public Object setAnnotation(Object o)
  {
    Object r = this.annotation;
    this.annotation = o;
    return r;
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
  
  public AnnotationImpl()
  {
    this.locator = NULL_LOCATION;
  }
  
  private static final LocatorImpl NULL_LOCATION = new LocatorImpl();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\AnnotationImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */