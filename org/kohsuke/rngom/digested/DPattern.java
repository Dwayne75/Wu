package org.kohsuke.rngom.digested;

import org.kohsuke.rngom.ast.om.ParsedPattern;
import org.kohsuke.rngom.parse.Parseable;
import org.xml.sax.Locator;

public abstract class DPattern
  implements ParsedPattern
{
  Locator location;
  DAnnotation annotation;
  DPattern next;
  DPattern prev;
  
  public Locator getLocation()
  {
    return this.location;
  }
  
  public DAnnotation getAnnotation()
  {
    if (this.annotation == null) {
      return DAnnotation.EMPTY;
    }
    return this.annotation;
  }
  
  public abstract boolean isNullable();
  
  public abstract <V> V accept(DPatternVisitor<V> paramDPatternVisitor);
  
  public Parseable createParseable()
  {
    return new PatternParseable(this);
  }
  
  public final boolean isElement()
  {
    return this instanceof DElementPattern;
  }
  
  public final boolean isAttribute()
  {
    return this instanceof DAttributePattern;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\digested\DPattern.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */