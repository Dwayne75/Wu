package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.ForeignAttributes;
import org.relaxng.datatype.ValidationContext;
import org.xml.sax.Locator;
import org.xml.sax.helpers.AttributesImpl;

public final class ForeignAttributesImpl
  extends AttributesImpl
  implements ForeignAttributes
{
  private final ValidationContext context;
  private final Locator locator;
  final ForeignAttributesImpl next;
  
  public ForeignAttributesImpl(ValidationContext context, Locator locator, ForeignAttributesImpl next)
  {
    this.context = context;
    this.locator = locator;
    this.next = next;
  }
  
  public ValidationContext getContext()
  {
    return this.context;
  }
  
  public Locator getLocator()
  {
    return this.locator;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\ForeignAttributesImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */