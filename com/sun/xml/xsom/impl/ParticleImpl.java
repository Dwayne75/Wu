package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.impl.parser.DelayedRef;
import com.sun.xml.xsom.impl.parser.DelayedRef.ModelGroup;
import com.sun.xml.xsom.impl.parser.SchemaDocumentImpl;
import com.sun.xml.xsom.visitor.XSContentTypeFunction;
import com.sun.xml.xsom.visitor.XSContentTypeVisitor;
import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSVisitor;
import java.util.List;
import org.xml.sax.Locator;

public class ParticleImpl
  extends ComponentImpl
  implements XSParticle, ContentTypeImpl
{
  private Ref.Term term;
  private int maxOccurs;
  private int minOccurs;
  
  public ParticleImpl(SchemaDocumentImpl owner, AnnotationImpl _ann, Ref.Term _term, Locator _loc, int _maxOccurs, int _minOccurs)
  {
    super(owner, _ann, _loc, null);
    this.term = _term;
    this.maxOccurs = _maxOccurs;
    this.minOccurs = _minOccurs;
  }
  
  public ParticleImpl(SchemaDocumentImpl owner, AnnotationImpl _ann, Ref.Term _term, Locator _loc)
  {
    this(owner, _ann, _term, _loc, 1, 1);
  }
  
  public XSTerm getTerm()
  {
    return this.term.getTerm();
  }
  
  public int getMaxOccurs()
  {
    return this.maxOccurs;
  }
  
  public boolean isRepeated()
  {
    return (this.maxOccurs != 0) && (this.maxOccurs != 1);
  }
  
  public int getMinOccurs()
  {
    return this.minOccurs;
  }
  
  public void redefine(ModelGroupDeclImpl oldMG)
  {
    if ((this.term instanceof ModelGroupImpl))
    {
      ((ModelGroupImpl)this.term).redefine(oldMG);
      return;
    }
    if ((this.term instanceof DelayedRef.ModelGroup)) {
      ((DelayedRef)this.term).redefine(oldMG);
    }
  }
  
  public XSSimpleType asSimpleType()
  {
    return null;
  }
  
  public XSParticle asParticle()
  {
    return this;
  }
  
  public XSContentType asEmpty()
  {
    return null;
  }
  
  public final Object apply(XSFunction function)
  {
    return function.particle(this);
  }
  
  public final Object apply(XSContentTypeFunction function)
  {
    return function.particle(this);
  }
  
  public final void visit(XSVisitor visitor)
  {
    visitor.particle(this);
  }
  
  public final void visit(XSContentTypeVisitor visitor)
  {
    visitor.particle(this);
  }
  
  public XSContentType getContentType()
  {
    return this;
  }
  
  public List getForeignAttributes()
  {
    return getTerm().getForeignAttributes();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\ParticleImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */