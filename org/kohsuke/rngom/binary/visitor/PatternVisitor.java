package org.kohsuke.rngom.binary.visitor;

import org.kohsuke.rngom.binary.Pattern;
import org.kohsuke.rngom.nc.NameClass;
import org.relaxng.datatype.Datatype;

public abstract interface PatternVisitor
{
  public abstract void visitEmpty();
  
  public abstract void visitNotAllowed();
  
  public abstract void visitError();
  
  public abstract void visitAfter(Pattern paramPattern1, Pattern paramPattern2);
  
  public abstract void visitGroup(Pattern paramPattern1, Pattern paramPattern2);
  
  public abstract void visitInterleave(Pattern paramPattern1, Pattern paramPattern2);
  
  public abstract void visitChoice(Pattern paramPattern1, Pattern paramPattern2);
  
  public abstract void visitOneOrMore(Pattern paramPattern);
  
  public abstract void visitElement(NameClass paramNameClass, Pattern paramPattern);
  
  public abstract void visitAttribute(NameClass paramNameClass, Pattern paramPattern);
  
  public abstract void visitData(Datatype paramDatatype);
  
  public abstract void visitDataExcept(Datatype paramDatatype, Pattern paramPattern);
  
  public abstract void visitValue(Datatype paramDatatype, Object paramObject);
  
  public abstract void visitText();
  
  public abstract void visitList(Pattern paramPattern);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\binary\visitor\PatternVisitor.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */