package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSSchema;
import org.xml.sax.Locator;

public abstract class ComponentImpl
  implements XSComponent
{
  protected final SchemaImpl ownerSchema;
  private final AnnotationImpl annotation;
  private final Locator locator;
  
  protected ComponentImpl(SchemaImpl _owner, AnnotationImpl _annon, Locator _loc)
  {
    this.ownerSchema = _owner;
    this.annotation = _annon;
    this.locator = _loc;
  }
  
  public final XSSchema getOwnerSchema()
  {
    return this.ownerSchema;
  }
  
  public final XSAnnotation getAnnotation()
  {
    return this.annotation;
  }
  
  public final Locator getLocator()
  {
    return this.locator;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\impl\ComponentImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */