package com.sun.tools.xjc.grammar;

import com.sun.codemodel.JPackage;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.OtherExp;
import com.sun.tools.xjc.util.CodeModelClassFactory;
import org.xml.sax.Locator;

public class ClassCandidateItem
  extends OtherExp
{
  public final String name;
  private final CodeModelClassFactory classFactory;
  private final AnnotatedGrammar grammar;
  public final JPackage targetPackage;
  public final Locator locator;
  
  public ClassCandidateItem(CodeModelClassFactory _classFactory, AnnotatedGrammar _grammar, JPackage _targetPackage, String _name, Locator _loc, Expression body)
  {
    super(body);
    
    this.grammar = _grammar;
    this.classFactory = _classFactory;
    this.targetPackage = _targetPackage;
    this.name = _name;
    this.locator = _loc;
  }
  
  private ClassItem ci = null;
  
  public ClassItem toClassItem()
  {
    if (this.ci == null) {
      this.ci = this.grammar.createClassItem(this.classFactory.createInterface(this.targetPackage, this.name, this.locator), this.exp, this.locator);
    }
    return this.ci;
  }
  
  public String printName()
  {
    return super.printName() + "#" + this.name;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\ClassCandidateItem.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */