package com.sun.tools.xjc.reader;

import com.sun.tools.xjc.model.CElementPropertyInfo;
import com.sun.tools.xjc.model.CElementPropertyInfo.CollectionMode;
import com.sun.tools.xjc.model.CNonElement;
import com.sun.tools.xjc.model.CReferencePropertyInfo;
import com.sun.tools.xjc.model.CTypeRef;
import com.sun.tools.xjc.model.Multiplicity;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.xml.bind.v2.model.core.ID;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.activation.MimeType;

public final class RawTypeSet
{
  public final Set<Ref> refs;
  public final Mode canBeTypeRefs;
  public final Multiplicity mul;
  private CElementPropertyInfo.CollectionMode collectionMode;
  
  public RawTypeSet(Set<Ref> refs, Multiplicity m)
  {
    this.refs = refs;
    this.mul = m;
    this.canBeTypeRefs = canBeTypeRefs();
  }
  
  public CElementPropertyInfo.CollectionMode getCollectionMode()
  {
    return this.collectionMode;
  }
  
  public boolean isRequired()
  {
    return this.mul.min > 0;
  }
  
  public static enum Mode
  {
    SHOULD_BE_TYPEREF(0),  CAN_BE_TYPEREF(1),  MUST_BE_REFERENCE(2);
    
    private final int rank;
    
    private Mode(int rank)
    {
      this.rank = rank;
    }
    
    Mode or(Mode that)
    {
      switch (Math.max(this.rank, that.rank))
      {
      case 0: 
        return SHOULD_BE_TYPEREF;
      case 1: 
        return CAN_BE_TYPEREF;
      case 2: 
        return MUST_BE_REFERENCE;
      }
      throw new AssertionError();
    }
  }
  
  private Mode canBeTypeRefs()
  {
    Set<NType> types = new HashSet();
    
    this.collectionMode = (this.mul.isAtMostOnce() ? CElementPropertyInfo.CollectionMode.NOT_REPEATED : CElementPropertyInfo.CollectionMode.REPEATED_ELEMENT);
    
    Mode mode = Mode.SHOULD_BE_TYPEREF;
    for (Ref r : this.refs)
    {
      mode = mode.or(r.canBeType(this));
      if (mode == Mode.MUST_BE_REFERENCE) {
        return mode;
      }
      if (!types.add(r.toTypeRef(null).getTarget().getType())) {
        return Mode.MUST_BE_REFERENCE;
      }
      if (r.isListOfValues())
      {
        if ((this.refs.size() > 1) || (!this.mul.isAtMostOnce())) {
          return Mode.MUST_BE_REFERENCE;
        }
        this.collectionMode = CElementPropertyInfo.CollectionMode.REPEATED_VALUE;
      }
    }
    return mode;
  }
  
  public void addTo(CElementPropertyInfo prop)
  {
    assert (this.canBeTypeRefs != Mode.MUST_BE_REFERENCE);
    if (this.mul.isZero()) {
      return;
    }
    List<CTypeRef> dst = prop.getTypes();
    for (Ref t : this.refs) {
      dst.add(t.toTypeRef(prop));
    }
  }
  
  public void addTo(CReferencePropertyInfo prop)
  {
    if (this.mul.isZero()) {
      return;
    }
    for (Ref t : this.refs) {
      t.toElementRef(prop);
    }
  }
  
  public ID id()
  {
    for (Ref t : this.refs)
    {
      ID id = t.id();
      if (id != ID.NONE) {
        return id;
      }
    }
    return ID.NONE;
  }
  
  public MimeType getExpectedMimeType()
  {
    for (Ref t : this.refs)
    {
      MimeType mt = t.getExpectedMimeType();
      if (mt != null) {
        return mt;
      }
    }
    return null;
  }
  
  public static abstract class Ref
  {
    protected abstract CTypeRef toTypeRef(CElementPropertyInfo paramCElementPropertyInfo);
    
    protected abstract void toElementRef(CReferencePropertyInfo paramCReferencePropertyInfo);
    
    protected abstract RawTypeSet.Mode canBeType(RawTypeSet paramRawTypeSet);
    
    protected abstract boolean isListOfValues();
    
    protected abstract ID id();
    
    protected MimeType getExpectedMimeType()
    {
      return null;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\RawTypeSet.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */