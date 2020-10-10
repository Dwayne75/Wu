package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.xsom.XSComponent;
import org.xml.sax.Locator;

abstract class AbstractDeclarationImpl
  implements BIDeclaration
{
  private final Locator loc;
  protected BindInfo parent;
  
  protected AbstractDeclarationImpl(Locator _loc)
  {
    this.loc = _loc;
  }
  
  public Locator getLocation()
  {
    return this.loc;
  }
  
  public void setParent(BindInfo p)
  {
    this.parent = p;
  }
  
  protected final XSComponent getOwner()
  {
    return this.parent.getOwner();
  }
  
  protected final BGMBuilder getBuilder()
  {
    return this.parent.getBuilder();
  }
  
  private boolean isAcknowledged = false;
  
  public final boolean isAcknowledged()
  {
    return this.isAcknowledged;
  }
  
  public void markAsAcknowledged()
  {
    this.isAcknowledged = true;
  }
  
  protected static final void _assert(boolean b)
  {
    if (!b) {
      throw new JAXBAssertionError();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\bindinfo\AbstractDeclarationImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */