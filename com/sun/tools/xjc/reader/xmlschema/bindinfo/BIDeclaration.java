package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import java.util.Collection;
import javax.xml.namespace.QName;
import org.xml.sax.Locator;

public abstract interface BIDeclaration
{
  public abstract void setParent(BindInfo paramBindInfo);
  
  public abstract QName getName();
  
  public abstract Locator getLocation();
  
  public abstract void markAsAcknowledged();
  
  public abstract boolean isAcknowledged();
  
  public abstract void onSetOwner();
  
  public abstract Collection<BIDeclaration> getChildren();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\bindinfo\BIDeclaration.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */