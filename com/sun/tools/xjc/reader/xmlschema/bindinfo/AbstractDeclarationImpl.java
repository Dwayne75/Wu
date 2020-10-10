package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import com.sun.codemodel.JCodeModel;
import com.sun.tools.xjc.reader.Ring;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.xml.bind.annotation.XmlLocation;
import com.sun.xml.xsom.XSComponent;
import java.util.Collection;
import java.util.Collections;
import org.xml.sax.Locator;

abstract class AbstractDeclarationImpl
  implements BIDeclaration
{
  @XmlLocation
  Locator loc;
  protected BindInfo parent;
  
  @Deprecated
  protected AbstractDeclarationImpl(Locator loc)
  {
    this.loc = loc;
  }
  
  protected AbstractDeclarationImpl() {}
  
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
  
  protected final JCodeModel getCodeModel()
  {
    return (JCodeModel)Ring.get(JCodeModel.class);
  }
  
  private boolean isAcknowledged = false;
  
  public final boolean isAcknowledged()
  {
    return this.isAcknowledged;
  }
  
  public void onSetOwner() {}
  
  public Collection<BIDeclaration> getChildren()
  {
    return Collections.emptyList();
  }
  
  public void markAsAcknowledged()
  {
    this.isAcknowledged = true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\bindinfo\AbstractDeclarationImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */