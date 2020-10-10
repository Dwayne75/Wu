package com.sun.tools.xjc.grammar.ext;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCatchBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JTryBlock;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.msv.grammar.NameClass;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.generator.marshaller.FieldMarshallerGenerator;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.runtime.ContentHandlerAdaptor;
import com.sun.tools.xjc.runtime.W3CDOMUnmarshallingEventHandler;
import com.sun.xml.bind.unmarshaller.DOMScanner;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.xml.sax.Locator;

class W3CDOMItem
  extends AbstractDOMItem
{
  public static DOMItemFactory factory = new W3CDOMItem.1();
  
  public W3CDOMItem(NameClass _elementName, AnnotatedGrammar grammar, Locator loc)
  {
    super(_elementName, grammar, loc);
  }
  
  public void generateMarshaller(GeneratorContext context, JBlock block, FieldMarshallerGenerator fmg, JExpression $context)
  {
    block.invoke(JExpr._new(this.codeModel.ref(DOMScanner.class)), "parse").arg(JExpr.cast(this.codeModel.ref(Element.class), fmg.peek(true))).arg(JExpr._new(context.getRuntime(ContentHandlerAdaptor.class)).arg($context));
  }
  
  public JExpression generateUnmarshaller(GeneratorContext context, JExpression $context, JBlock block, JExpression memento, JVar $uri, JVar $local, JVar $qname, JVar $atts)
  {
    JVar $v = block.decl(this.codeModel.ref(Element.class), "ur", JExpr._null());
    JClass handlerClass = context.getRuntime(W3CDOMUnmarshallingEventHandler.class);
    
    JTryBlock tryBlock = block._try();
    
    block = tryBlock.body();
    JVar $u = block.decl(handlerClass, "u", JExpr._new(handlerClass).arg($context));
    block.invoke($context, "pushContentHandler").arg($u).arg(memento);
    block.invoke($context.invoke("getCurrentHandler"), "enterElement").arg($uri).arg($local).arg($qname).arg($atts);
    
    block.assign($v, $u.invoke("getOwner"));
    
    JCatchBlock catchBlock = tryBlock._catch(this.codeModel.ref(ParserConfigurationException.class));
    
    catchBlock.body().invoke("handleGenericException").arg(catchBlock.param("e"));
    
    return $v;
  }
  
  public JType getType()
  {
    return this.codeModel.ref(Element.class);
  }
  
  public JExpression createRootUnmarshaller(GeneratorContext context, JVar $unmarshallingContext)
  {
    JClass handlerClass = context.getRuntime(W3CDOMUnmarshallingEventHandler.class);
    
    return JExpr._new(handlerClass).arg($unmarshallingContext);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\ext\W3CDOMItem.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */