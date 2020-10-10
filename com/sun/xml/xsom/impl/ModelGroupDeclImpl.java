package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.impl.parser.SchemaDocumentImpl;
import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSTermFunction;
import com.sun.xml.xsom.visitor.XSTermFunctionWithParam;
import com.sun.xml.xsom.visitor.XSTermVisitor;
import com.sun.xml.xsom.visitor.XSVisitor;
import org.xml.sax.Locator;

public class ModelGroupDeclImpl
  extends DeclarationImpl
  implements XSModelGroupDecl, Ref.Term
{
  private final ModelGroupImpl modelGroup;
  
  public ModelGroupDeclImpl(SchemaDocumentImpl owner, AnnotationImpl _annon, Locator _loc, ForeignAttributesImpl _fa, String _targetNamespace, String _name, ModelGroupImpl _modelGroup)
  {
    super(owner, _annon, _loc, _fa, _targetNamespace, _name, false);
    this.modelGroup = _modelGroup;
    if (this.modelGroup == null) {
      throw new IllegalArgumentException();
    }
  }
  
  public XSModelGroup getModelGroup()
  {
    return this.modelGroup;
  }
  
  public void redefine(ModelGroupDeclImpl oldMG)
  {
    this.modelGroup.redefine(oldMG);
  }
  
  public void visit(XSVisitor visitor)
  {
    visitor.modelGroupDecl(this);
  }
  
  public void visit(XSTermVisitor visitor)
  {
    visitor.modelGroupDecl(this);
  }
  
  public Object apply(XSTermFunction function)
  {
    return function.modelGroupDecl(this);
  }
  
  public <T, P> T apply(XSTermFunctionWithParam<T, P> function, P param)
  {
    return (T)function.modelGroupDecl(this, param);
  }
  
  public Object apply(XSFunction function)
  {
    return function.modelGroupDecl(this);
  }
  
  public boolean isWildcard()
  {
    return false;
  }
  
  public boolean isModelGroupDecl()
  {
    return true;
  }
  
  public boolean isModelGroup()
  {
    return false;
  }
  
  public boolean isElementDecl()
  {
    return false;
  }
  
  public XSWildcard asWildcard()
  {
    return null;
  }
  
  public XSModelGroupDecl asModelGroupDecl()
  {
    return this;
  }
  
  public XSModelGroup asModelGroup()
  {
    return null;
  }
  
  public XSElementDecl asElementDecl()
  {
    return null;
  }
  
  public XSTerm getTerm()
  {
    return this;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\ModelGroupDeclImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */