package com.sun.tools.xjc.grammar.ext;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JType;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.NameClass;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.msv.grammar.trex.ElementPattern;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ExternalItem;
import org.xml.sax.Locator;

abstract class AbstractDOMItem
  extends ExternalItem
{
  private final Expression agm;
  protected final JCodeModel codeModel;
  
  public AbstractDOMItem(NameClass _elementName, AnnotatedGrammar grammar, Locator loc)
  {
    super("dom", _elementName, loc);
    ExpressionPool pool = grammar.getPool();
    
    this.codeModel = grammar.codeModel;
    
    ReferenceExp any = new ReferenceExp(null);
    any.exp = pool.createMixed(pool.createZeroOrMore(pool.createChoice(pool.createAttribute(NameClass.ALL), new ElementPattern(NameClass.ALL, any))));
    
    this.exp = new ElementPattern(_elementName, any);
    this.agm = this.exp;
  }
  
  protected final JType createPhantomType(String name)
  {
    try
    {
      JDefinedClass def = this.codeModel._class(name);
      def.hide();
      return def;
    }
    catch (JClassAlreadyExistsException e)
    {
      return e.getExistingClass();
    }
  }
  
  public Expression createAGM(ExpressionPool pool)
  {
    return this.agm;
  }
  
  public Expression createValidationFragment()
  {
    return this.agm;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\ext\AbstractDOMItem.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */