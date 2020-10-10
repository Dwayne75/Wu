package org.kohsuke.rngom.nc;

import javax.xml.namespace.QName;

public class NameClassWalker
  implements NameClassVisitor<Void>
{
  public Void visitChoice(NameClass nc1, NameClass nc2)
  {
    nc1.accept(this);
    return (Void)nc2.accept(this);
  }
  
  public Void visitNsName(String ns)
  {
    return null;
  }
  
  public Void visitNsNameExcept(String ns, NameClass nc)
  {
    return (Void)nc.accept(this);
  }
  
  public Void visitAnyName()
  {
    return null;
  }
  
  public Void visitAnyNameExcept(NameClass nc)
  {
    return (Void)nc.accept(this);
  }
  
  public Void visitName(QName name)
  {
    return null;
  }
  
  public Void visitNull()
  {
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\nc\NameClassWalker.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */