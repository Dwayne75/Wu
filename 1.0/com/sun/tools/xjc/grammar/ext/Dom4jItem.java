package com.sun.tools.xjc.grammar.ext;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.msv.grammar.NameClass;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.generator.marshaller.FieldMarshallerGenerator;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.runtime.ContentHandlerAdaptor;
import com.sun.tools.xjc.runtime.Dom4jUnmarshallingEventHandler;
import org.xml.sax.Locator;

class Dom4jItem
  extends AbstractDOMItem
{
  private final JType elementType;
  public static DOMItemFactory factory = new Dom4jItem.1();
  
  public Dom4jItem(NameClass _elementName, AnnotatedGrammar grammar, Locator loc)
  {
    super(_elementName, grammar, loc);
    
    this.elementType = createPhantomType("org.dom4j.Element");
  }
  
  public void generateMarshaller(GeneratorContext context, JBlock block, FieldMarshallerGenerator fmg, JExpression $context)
  {
    block = block.block();
    block.directStatement("org.dom4j.io.SAXWriter w = new org.dom4j.io.SAXWriter();");
    JExpression $w = JExpr.direct("w");
    block.invoke($w, "setContentHandler").arg(JExpr._new(context.getRuntime(ContentHandlerAdaptor.class)).arg($context));
    
    block.invoke($w, "write").arg(JExpr.cast(this.elementType, fmg.peek(true)));
  }
  
  public JExpression generateUnmarshaller(GeneratorContext context, JExpression $context, JBlock block, JExpression memento, JVar $uri, JVar $local, JVar $qname, JVar $atts)
  {
    JClass handlerClass = context.getRuntime(Dom4jUnmarshallingEventHandler.class);
    JVar $u = block.decl(handlerClass, "u", JExpr._new(handlerClass).arg($context));
    
    block.invoke($context, "pushContentHandler").arg($u).arg(memento);
    block.invoke($context.invoke("getCurrentHandler"), "enterElement").arg($uri).arg($local).arg($qname).arg($atts);
    
    return $u.invoke("getOwner");
  }
  
  public JType getType()
  {
    return this.elementType;
  }
  
  public JExpression createRootUnmarshaller(GeneratorContext context, JVar $unmarshallingContext)
  {
    JClass handlerClass = context.getRuntime(Dom4jUnmarshallingEventHandler.class);
    
    return JExpr._new(handlerClass).arg($unmarshallingContext);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\ext\Dom4jItem.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */