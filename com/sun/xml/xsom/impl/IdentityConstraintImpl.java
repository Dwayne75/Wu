package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSXPath;
import com.sun.xml.xsom.impl.parser.SchemaDocumentImpl;
import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSVisitor;
import java.util.Collections;
import java.util.List;
import org.xml.sax.Locator;

public class IdentityConstraintImpl
  extends ComponentImpl
  implements XSIdentityConstraint, Ref.IdentityConstraint
{
  private XSElementDecl parent;
  private final short category;
  private final String name;
  private final XSXPath selector;
  private final List<XSXPath> fields;
  private final Ref.IdentityConstraint refer;
  
  public IdentityConstraintImpl(SchemaDocumentImpl _owner, AnnotationImpl _annon, Locator _loc, ForeignAttributesImpl fa, short category, String name, XPathImpl selector, List<XPathImpl> fields, Ref.IdentityConstraint refer)
  {
    super(_owner, _annon, _loc, fa);
    this.category = category;
    this.name = name;
    this.selector = selector;
    selector.setParent(this);
    this.fields = Collections.unmodifiableList(fields);
    for (XPathImpl xp : fields) {
      xp.setParent(this);
    }
    this.refer = refer;
  }
  
  public void visit(XSVisitor visitor)
  {
    visitor.identityConstraint(this);
  }
  
  public <T> T apply(XSFunction<T> function)
  {
    return (T)function.identityConstraint(this);
  }
  
  public void setParent(ElementDecl parent)
  {
    this.parent = parent;
    parent.getOwnerSchema().addIdentityConstraint(this);
  }
  
  public XSElementDecl getParent()
  {
    return this.parent;
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public String getTargetNamespace()
  {
    return getParent().getTargetNamespace();
  }
  
  public short getCategory()
  {
    return this.category;
  }
  
  public XSXPath getSelector()
  {
    return this.selector;
  }
  
  public List<XSXPath> getFields()
  {
    return this.fields;
  }
  
  public XSIdentityConstraint getReferencedKey()
  {
    if (this.category == 1) {
      return this.refer.get();
    }
    throw new IllegalStateException("not a keyref");
  }
  
  public XSIdentityConstraint get()
  {
    return this;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\IdentityConstraintImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */