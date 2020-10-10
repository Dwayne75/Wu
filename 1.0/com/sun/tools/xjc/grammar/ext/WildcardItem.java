package com.sun.tools.xjc.grammar.ext;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.NameClass;
import com.sun.msv.grammar.NamespaceNameClass;
import com.sun.msv.grammar.trex.ElementPattern;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.generator.field.FieldRenderer;
import com.sun.tools.xjc.generator.marshaller.FieldMarshallerGenerator;
import com.sun.tools.xjc.grammar.ExternalItem;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.tools.xjc.reader.xmlschema.WildcardNameClassBuilder;
import com.sun.xml.bind.GrammarImpl.Plug;
import com.sun.xml.bind.JAXBObject;
import com.sun.xml.bind.xmlschema.LaxWildcardPlug;
import com.sun.xml.bind.xmlschema.StrictWildcardPlug;
import com.sun.xml.xsom.XSWildcard;
import org.xml.sax.Locator;

public class WildcardItem
  extends ExternalItem
{
  public final boolean errorIfNotFound;
  private final JClass refObject;
  
  public WildcardItem(JCodeModel codeModel, NameClass nc, boolean errorIfNotFound, Locator loc)
  {
    super("wildcard", nc, loc);
    this.refObject = codeModel.ref(Object.class);
    this.errorIfNotFound = errorIfNotFound;
  }
  
  public WildcardItem(JCodeModel codeModel, XSWildcard wc)
  {
    this(codeModel, WildcardNameClassBuilder.build(wc), wc.getMode() == 2, wc.getLocator());
  }
  
  public JType getType()
  {
    return this.refObject;
  }
  
  public Expression createAGM(ExpressionPool pool)
  {
    GrammarImpl.Plug p;
    GrammarImpl.Plug p;
    if (this.errorIfNotFound) {
      p = new StrictWildcardPlug(this.elementName);
    } else {
      p = new LaxWildcardPlug(this.elementName);
    }
    return p;
  }
  
  public Expression createValidationFragment()
  {
    return new ElementPattern(new NamespaceNameClass("http://java.sun.com/jaxb/xjc/dummy-elements"), new AttributeExp(this.elementName, Expression.anyString));
  }
  
  public void generateMarshaller(GeneratorContext context, JBlock block, FieldMarshallerGenerator fmg, JExpression $context)
  {
    block.invoke($context, "childAsBody").arg(JExpr.cast(context.getCodeModel().ref(JAXBObject.class), fmg.peek(true))).arg(JExpr.lit(fmg.owner().getFieldUse().name));
  }
  
  public JExpression generateUnmarshaller(GeneratorContext context, JExpression $unmarshallingContext, JBlock block, JExpression memento, JVar $uri, JVar $local, JVar $qname, JVar $atts)
  {
    JInvocation spawn = JExpr.invoke("spawnWildcard").arg(memento).arg($uri).arg($local).arg($qname).arg($atts);
    
    return block.decl(getType(), "co", spawn);
  }
  
  public JExpression createRootUnmarshaller(GeneratorContext context, JVar $unmarshallingContext)
  {
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\ext\WildcardItem.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */