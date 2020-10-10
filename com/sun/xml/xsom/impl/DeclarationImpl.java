package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.impl.parser.SchemaDocumentImpl;
import org.xml.sax.Locator;

abstract class DeclarationImpl
  extends ComponentImpl
  implements XSDeclaration
{
  private final String name;
  private final String targetNamespace;
  private final boolean anonymous;
  
  DeclarationImpl(SchemaDocumentImpl owner, AnnotationImpl _annon, Locator loc, ForeignAttributesImpl fa, String _targetNamespace, String _name, boolean _anonymous)
  {
    super(owner, _annon, loc, fa);
    this.targetNamespace = _targetNamespace;
    this.name = _name;
    this.anonymous = _anonymous;
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public String getTargetNamespace()
  {
    return this.targetNamespace;
  }
  
  /**
   * @deprecated
   */
  public boolean isAnonymous()
  {
    return this.anonymous;
  }
  
  public final boolean isGlobal()
  {
    return !isAnonymous();
  }
  
  public final boolean isLocal()
  {
    return isAnonymous();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\DeclarationImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */