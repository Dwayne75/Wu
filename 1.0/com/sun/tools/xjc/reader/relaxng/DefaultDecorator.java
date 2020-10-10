package com.sun.tools.xjc.reader.relaxng;

import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.SimpleNameClass;
import com.sun.msv.reader.State;
import com.sun.msv.util.StartTagInfo;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassCandidateItem;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.reader.NameConverter;
import com.sun.tools.xjc.reader.StackPackageManager;
import com.sun.tools.xjc.reader.decorator.DecoratorImpl;
import com.sun.tools.xjc.util.CodeModelClassFactory;

class DefaultDecorator
  extends DecoratorImpl
{
  DefaultDecorator(TRELAXNGReader reader, NameConverter nc)
  {
    super(reader, reader.annGrammar, nc);
  }
  
  private CodeModelClassFactory getClassFactory()
  {
    return ((TRELAXNGReader)this.reader).classFactory;
  }
  
  public Expression decorate(State state, Expression exp)
  {
    StartTagInfo tag = state.getStartTag();
    TRELAXNGReader reader = (TRELAXNGReader)this.reader;
    if ((tag.localName.equals("define")) && 
      ((exp == Expression.nullSet) || (exp == Expression.epsilon)) && (state.getStartTag().containsAttribute("combine"))) {
      return exp;
    }
    if (((tag.localName.equals("element")) || (tag.localName.equals("define"))) && (!(exp instanceof ClassItem)) && (!(exp instanceof ClassCandidateItem)))
    {
      String baseName = decideName(state, exp, "class", "", state.getLocation());
      
      return new ClassCandidateItem(getClassFactory(), this.grammar, reader.packageManager.getCurrentPackage(), baseName, state.getLocation(), exp);
    }
    if (((exp instanceof AttributeExp)) && (!(((AttributeExp)exp).nameClass instanceof SimpleNameClass))) {
      return this.grammar.createClassItem(getClassFactory().createInterface(reader.packageManager.getCurrentPackage(), decideName(state, exp, "class", "Attr", state.getLocation()), state.getLocation()), exp, state.getLocation());
    }
    return exp;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\relaxng\DefaultDecorator.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */