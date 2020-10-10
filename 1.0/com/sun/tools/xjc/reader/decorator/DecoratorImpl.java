package com.sun.tools.xjc.reader.decorator;

import com.sun.codemodel.JCodeModel;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.NameClass;
import com.sun.msv.grammar.NameClassAndExpression;
import com.sun.msv.grammar.SimpleNameClass;
import com.sun.msv.reader.Controller;
import com.sun.msv.reader.ExpressionState;
import com.sun.msv.reader.GrammarReader;
import com.sun.msv.reader.State;
import com.sun.msv.reader.trex.DefineState;
import com.sun.msv.util.StartTagInfo;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.reader.NameConverter;
import com.sun.xml.bind.JAXBAssertionError;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

public abstract class DecoratorImpl
  implements Decorator
{
  protected final GrammarReader reader;
  protected final AnnotatedGrammar grammar;
  protected final JCodeModel codeModel;
  protected final NameConverter nameConverter;
  
  protected DecoratorImpl(GrammarReader _reader, AnnotatedGrammar _grammar, NameConverter nc)
  {
    this.reader = _reader;
    this.grammar = _grammar;
    this.codeModel = this.grammar.codeModel;
    this.nameConverter = nc;
  }
  
  protected final String getAttribute(StartTagInfo tag, String attName)
  {
    return tag.getAttribute("http://java.sun.com/xml/ns/jaxb", attName);
  }
  
  protected final String getAttribute(StartTagInfo tag, String attName, String defaultValue)
  {
    String r = getAttribute(tag, attName);
    if (r != null) {
      return r;
    }
    return defaultValue;
  }
  
  protected final String decideName(State state, Expression exp, String role, String suffix, Locator loc)
  {
    StartTagInfo tag = state.getStartTag();
    
    String name = getAttribute(tag, "name");
    if (name == null)
    {
      name = tag.getAttribute("name");
      if (name != null) {
        name = xmlNameToJavaName(role, name + suffix);
      }
    }
    if (name == null) {
      if ((exp instanceof NameClassAndExpression))
      {
        NameClass nc = ((NameClassAndExpression)exp).getNameClass();
        if ((nc instanceof SimpleNameClass)) {
          name = xmlNameToJavaName(role, ((SimpleNameClass)nc).localName + suffix);
        }
      }
    }
    if (name == null)
    {
      if (((state.getParentState() instanceof ExpressionState)) || ((state.getParentState() instanceof DefineState))) {
        return decideName(state.getParentState(), exp, role, suffix, loc);
      }
      this.reader.controller.error(new SAXParseException(Messages.format("NameNeeded"), loc));
      
      return "DUMMY";
    }
    return name;
  }
  
  private final String xmlNameToJavaName(String role, String name)
  {
    if (role.equals("field")) {
      return this.nameConverter.toPropertyName(name);
    }
    if (role.equals("interface")) {
      return this.nameConverter.toInterfaceName(name);
    }
    if (role.equals("class")) {
      return this.nameConverter.toClassName(name);
    }
    throw new JAXBAssertionError(role);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\decorator\DecoratorImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */