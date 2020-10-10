package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.impl.parser.SchemaDocumentImpl;
import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSVisitor;
import org.xml.sax.Locator;

public class NotationImpl
  extends DeclarationImpl
  implements XSNotation
{
  private final String publicId;
  private final String systemId;
  
  public NotationImpl(SchemaDocumentImpl owner, AnnotationImpl _annon, Locator _loc, ForeignAttributesImpl _fa, String _name, String _publicId, String _systemId)
  {
    super(owner, _annon, _loc, _fa, owner.getTargetNamespace(), _name, false);
    
    this.publicId = _publicId;
    this.systemId = _systemId;
  }
  
  public String getPublicId()
  {
    return this.publicId;
  }
  
  public String getSystemId()
  {
    return this.systemId;
  }
  
  public void visit(XSVisitor visitor)
  {
    visitor.notation(this);
  }
  
  public Object apply(XSFunction function)
  {
    return function.notation(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\NotationImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */