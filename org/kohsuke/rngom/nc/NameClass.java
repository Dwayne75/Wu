package org.kohsuke.rngom.nc;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;
import org.kohsuke.rngom.ast.om.ParsedNameClass;

public abstract class NameClass
  implements ParsedNameClass, Serializable
{
  static final int SPECIFICITY_NONE = -1;
  static final int SPECIFICITY_ANY_NAME = 0;
  static final int SPECIFICITY_NS_NAME = 1;
  static final int SPECIFICITY_NAME = 2;
  
  public abstract boolean contains(QName paramQName);
  
  public abstract int containsSpecificity(QName paramQName);
  
  public abstract <V> V accept(NameClassVisitor<V> paramNameClassVisitor);
  
  public abstract boolean isOpen();
  
  public Set<QName> listNames()
  {
    final Set<QName> names = new HashSet();
    accept(new NameClassWalker()
    {
      public Void visitName(QName name)
      {
        names.add(name);
        return null;
      }
    });
    return names;
  }
  
  public final boolean hasOverlapWith(NameClass nc2)
  {
    return OverlapDetector.overlap(this, nc2);
  }
  
  public static final NameClass ANY = new AnyNameClass();
  public static final NameClass NULL = new NullNameClass();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\nc\NameClass.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */