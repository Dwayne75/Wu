package com.sun.tools.xjc.reader.relaxng;

import com.sun.tools.xjc.model.CBuiltinLeafInfo;
import com.sun.tools.xjc.model.TypeUse;
import com.sun.tools.xjc.model.TypeUseFactory;
import java.util.Map;
import org.kohsuke.rngom.digested.DAttributePattern;
import org.kohsuke.rngom.digested.DChoicePattern;
import org.kohsuke.rngom.digested.DContainerPattern;
import org.kohsuke.rngom.digested.DDataPattern;
import org.kohsuke.rngom.digested.DDefine;
import org.kohsuke.rngom.digested.DElementPattern;
import org.kohsuke.rngom.digested.DEmptyPattern;
import org.kohsuke.rngom.digested.DGrammarPattern;
import org.kohsuke.rngom.digested.DGroupPattern;
import org.kohsuke.rngom.digested.DInterleavePattern;
import org.kohsuke.rngom.digested.DListPattern;
import org.kohsuke.rngom.digested.DMixedPattern;
import org.kohsuke.rngom.digested.DNotAllowedPattern;
import org.kohsuke.rngom.digested.DOneOrMorePattern;
import org.kohsuke.rngom.digested.DOptionalPattern;
import org.kohsuke.rngom.digested.DPattern;
import org.kohsuke.rngom.digested.DPatternVisitor;
import org.kohsuke.rngom.digested.DRefPattern;
import org.kohsuke.rngom.digested.DTextPattern;
import org.kohsuke.rngom.digested.DValuePattern;
import org.kohsuke.rngom.digested.DZeroOrMorePattern;

final class TypeUseBinder
  implements DPatternVisitor<TypeUse>
{
  private final RELAXNGCompiler compiler;
  
  public TypeUseBinder(RELAXNGCompiler compiler)
  {
    this.compiler = compiler;
  }
  
  public TypeUse onGrammar(DGrammarPattern p)
  {
    return CBuiltinLeafInfo.STRING;
  }
  
  public TypeUse onChoice(DChoicePattern p)
  {
    return CBuiltinLeafInfo.STRING;
  }
  
  public TypeUse onData(DDataPattern p)
  {
    return onDataType(p.getDatatypeLibrary(), p.getType());
  }
  
  public TypeUse onValue(DValuePattern p)
  {
    return onDataType(p.getDatatypeLibrary(), p.getType());
  }
  
  private TypeUse onDataType(String datatypeLibrary, String type)
  {
    DatatypeLib lib = (DatatypeLib)this.compiler.datatypes.get(datatypeLibrary);
    if (lib != null)
    {
      TypeUse use = lib.get(type);
      if (use != null) {
        return use;
      }
    }
    return CBuiltinLeafInfo.STRING;
  }
  
  public TypeUse onInterleave(DInterleavePattern p)
  {
    return onContainer(p);
  }
  
  public TypeUse onGroup(DGroupPattern p)
  {
    return onContainer(p);
  }
  
  private TypeUse onContainer(DContainerPattern p)
  {
    TypeUse t = null;
    for (DPattern child : p)
    {
      TypeUse s = (TypeUse)child.accept(this);
      if ((t != null) && (t != s)) {
        return CBuiltinLeafInfo.STRING;
      }
      t = s;
    }
    return t;
  }
  
  public TypeUse onNotAllowed(DNotAllowedPattern p)
  {
    return error();
  }
  
  public TypeUse onEmpty(DEmptyPattern p)
  {
    return CBuiltinLeafInfo.STRING;
  }
  
  public TypeUse onList(DListPattern p)
  {
    return (TypeUse)p.getChild().accept(this);
  }
  
  public TypeUse onOneOrMore(DOneOrMorePattern p)
  {
    return TypeUseFactory.makeCollection((TypeUse)p.getChild().accept(this));
  }
  
  public TypeUse onZeroOrMore(DZeroOrMorePattern p)
  {
    return TypeUseFactory.makeCollection((TypeUse)p.getChild().accept(this));
  }
  
  public TypeUse onOptional(DOptionalPattern p)
  {
    return CBuiltinLeafInfo.STRING;
  }
  
  public TypeUse onRef(DRefPattern p)
  {
    return (TypeUse)p.getTarget().getPattern().accept(this);
  }
  
  public TypeUse onText(DTextPattern p)
  {
    return CBuiltinLeafInfo.STRING;
  }
  
  public TypeUse onAttribute(DAttributePattern p)
  {
    return error();
  }
  
  public TypeUse onElement(DElementPattern p)
  {
    return error();
  }
  
  public TypeUse onMixed(DMixedPattern p)
  {
    return error();
  }
  
  private TypeUse error()
  {
    throw new IllegalStateException();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\relaxng\TypeUseBinder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */