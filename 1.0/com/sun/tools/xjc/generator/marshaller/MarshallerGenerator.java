package com.sun.tools.xjc.generator.marshaller;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.msv.grammar.Expression;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.generator.field.FieldRenderer;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.tools.xjc.grammar.util.Multiplicity;
import com.sun.tools.xjc.runtime.XMLSerializable;
import com.sun.tools.xjc.runtime.XMLSerializer;
import com.sun.xml.bind.serializer.Util;
import java.util.HashMap;
import java.util.Map;
import org.xml.sax.SAXException;

public class MarshallerGenerator
{
  private final AnnotatedGrammar grammar;
  private final GeneratorContext context;
  static Class class$java$lang$String;
  static Class class$com$sun$xml$bind$marshaller$IdentifiableObject;
  
  public static void generate(AnnotatedGrammar grammar, GeneratorContext context, Options opt)
  {
    new MarshallerGenerator(grammar, context, opt);
  }
  
  private MarshallerGenerator(AnnotatedGrammar _grammar, GeneratorContext _context, Options _opt)
  {
    this.grammar = _grammar;
    this.context = _context;
    
    ClassItem[] cs = this.grammar.getClasses();
    for (int i = 0; i < cs.length; i++) {
      generate(this.context.getClassContext(cs[i]));
    }
  }
  
  private void generate(ClassContext cc)
  {
    cc.implClass._implements(this.context.getRuntime(XMLSerializable.class));
    
    generateMethodSkeleton(cc, "serializeBody").bodyPass.build(cc.target.exp);
    generateMethodSkeleton(cc, "serializeAttributes").attPass.build(cc.target.exp);
    generateMethodSkeleton(cc, "serializeURIs").uriPass.build(cc.target.exp);
    
    processID(cc);
  }
  
  private void processID(ClassContext cc)
  {
    cc.target.exp.visit(new MarshallerGenerator.1(this, cc));
  }
  
  private Context generateMethodSkeleton(ClassContext cc, String methodName)
  {
    JMethod p = cc.implClass.method(1, this.grammar.codeModel.VOID, methodName);
    JVar $serializer = p.param(this.context.getRuntime(XMLSerializer.class), "context");
    p._throws(SAXException.class);
    JBlock body = p.body();
    
    FieldUse[] uses = cc.target.getDeclaredFieldUses();
    Map fieldMarshallers = new HashMap();
    for (int i = 0; i < uses.length; i++)
    {
      fieldMarshallers.put(uses[i], this.context.getField(uses[i]).createMarshaller(body, Integer.toString(i + 1)));
      if ((uses[i].multiplicity.isUnique()) && (uses[i].type.isPrimitive()))
      {
        JExpression hasSetValue = this.context.getField(uses[i]).hasSetValue();
        if (hasSetValue != null)
        {
          JConditional cond = body._if(hasSetValue.not());
          cond._then().invoke($serializer, "reportError").arg(this.grammar.codeModel.ref(Util.class).staticInvoke("createMissingObjectError").arg(JExpr._this()).arg(JExpr.lit(uses[i].name)));
        }
      }
    }
    return new Context(this.context, this.grammar.getPool(), cc.target, body, $serializer, fieldMarshallers);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\marshaller\MarshallerGenerator.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */