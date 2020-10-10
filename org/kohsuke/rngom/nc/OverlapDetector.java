package org.kohsuke.rngom.nc;

import javax.xml.namespace.QName;

class OverlapDetector
  implements NameClassVisitor<Void>
{
  private NameClass nc1;
  private NameClass nc2;
  private boolean overlaps = false;
  static final String IMPOSSIBLE = "\000";
  
  private OverlapDetector(NameClass nc1, NameClass nc2)
  {
    this.nc1 = nc1;
    this.nc2 = nc2;
    nc1.accept(this);
    nc2.accept(this);
  }
  
  private void probe(QName name)
  {
    if ((this.nc1.contains(name)) && (this.nc2.contains(name))) {
      this.overlaps = true;
    }
  }
  
  public Void visitChoice(NameClass nc1, NameClass nc2)
  {
    nc1.accept(this);
    nc2.accept(this);
    return null;
  }
  
  public Void visitNsName(String ns)
  {
    probe(new QName(ns, "\000"));
    return null;
  }
  
  public Void visitNsNameExcept(String ns, NameClass ex)
  {
    probe(new QName(ns, "\000"));
    ex.accept(this);
    return null;
  }
  
  public Void visitAnyName()
  {
    probe(new QName("\000", "\000"));
    return null;
  }
  
  public Void visitAnyNameExcept(NameClass ex)
  {
    probe(new QName("\000", "\000"));
    ex.accept(this);
    return null;
  }
  
  public Void visitName(QName name)
  {
    probe(name);
    return null;
  }
  
  public Void visitNull()
  {
    return null;
  }
  
  static boolean overlap(NameClass nc1, NameClass nc2)
  {
    if ((nc2 instanceof SimpleNameClass))
    {
      SimpleNameClass snc = (SimpleNameClass)nc2;
      return nc1.contains(snc.name);
    }
    if ((nc1 instanceof SimpleNameClass))
    {
      SimpleNameClass snc = (SimpleNameClass)nc1;
      return nc2.contains(snc.name);
    }
    return new OverlapDetector(nc1, nc2).overlaps;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\nc\OverlapDetector.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */