package com.sun.tools.xjc.grammar.xducer;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JJavaName;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.msv.datatype.xsd.XSDatatype;
import com.sun.msv.grammar.ChoiceExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ValueExp;
import com.sun.msv.grammar.util.ExpressionPrinter;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.reader.NameConverter;
import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.JAXBAssertionError;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.relaxng.datatype.Datatype;
import org.xml.sax.Locator;

public class EnumerationXducer
  extends TransducerImpl
{
  private final JDefinedClass type;
  private final NameConverter nameConverter;
  private final JCodeModel codeModel;
  private final Map members;
  private Locator sourceLocator;
  
  public JType getReturnType()
  {
    return this.type;
  }
  
  public EnumerationXducer(NameConverter _nc, JDefinedClass clz, Expression enumExp, Map _members, Locator _sourceLocator)
  {
    this.type = clz;
    this.codeModel = clz.owner();
    this.nameConverter = _nc;
    this.members = _members;
    this.sourceLocator = _sourceLocator;
    
    this.values = getValues(enumExp);
  }
  
  private boolean populated = false;
  private ValueExp[] values;
  private JFieldVar[] items;
  private JType valueType;
  private static final String ERR_CONTEXT_DEPENDENT_TYPE = "EnumerationXducer.ContextDependentType";
  private static final String ERR_UNSUPPORTED_TYPE_FOR_ENUM = "EnumerationXducer.UnsupportedTypeForEnum";
  private static final String ERR_UNUSABLE_NAME = "EnumerationXducer.UnusableName";
  private static final String ERR_MULTIPLE_TYPES_IN_ENUM = "EnumerationXducer.MultipleTypesInEnum";
  private static final String ERR_NAME_COLLISION = "EnumerationXducer.NameCollision";
  
  public void populate(AnnotatedGrammar grammar, GeneratorContext context)
  {
    if (this.populated) {
      return;
    }
    this.populated = true;
    
    JClass stringRef = this.codeModel.ref(String.class);
    JClass objectRef = this.codeModel.ref(Object.class);
    if (!sanityCheck(context)) {
      return;
    }
    Transducer xducer = BuiltinDatatypeTransducerFactory.get(grammar, (XSDatatype)this.values[0].dt);
    
    this.valueType = xducer.getReturnType();
    
    JVar $valueMap = this.type.field(28, Map.class, "valueMap", JExpr._new(this.codeModel.ref(HashMap.class)));
    
    this.items = new JFieldVar[this.values.length];
    JVar[] valueObjs = new JVar[this.values.length];
    
    Set enumFieldNames = new HashSet();
    for (int i = 0; i < this.values.length; i++)
    {
      String lexical;
      String lexical;
      if ((this.values[i].dt instanceof XSDatatype)) {
        lexical = ((XSDatatype)this.values[i].dt).convertToLexicalValue(this.values[i].value, null);
      } else {
        lexical = this.values[i].value.toString();
      }
      EnumerationXducer.MemberInfo mem = (EnumerationXducer.MemberInfo)this.members.get(this.values[i]);
      String constName = null;
      if (mem != null) {
        constName = mem.name;
      }
      if (constName == null) {
        constName = this.nameConverter.toConstantName(fixUnsafeCharacters(lexical));
      }
      if (!JJavaName.isJavaIdentifier(constName)) {
        reportError(context, Messages.format("EnumerationXducer.UnusableName", lexical, constName));
      }
      if (!enumFieldNames.add(constName)) {
        reportError(context, Messages.format("EnumerationXducer.NameCollision", constName));
      } else if (!enumFieldNames.add('_' + constName)) {
        reportError(context, Messages.format("EnumerationXducer.NameCollision", '_' + constName));
      }
      valueObjs[i] = this.type.field(25, this.valueType, '_' + constName);
      
      this.items[i] = this.type.field(25, this.type, constName);
      
      this.items[i].init(JExpr._new(this.type).arg(valueObjs[i]));
      if ((mem != null) && (mem.javadoc != null)) {
        this.items[i].javadoc().appendComment(mem.javadoc);
      }
      valueObjs[i].init(xducer.generateDeserializer(this.codeModel.ref(DatatypeConverterImpl.class).staticInvoke("installHook").arg(JExpr.lit(lexical)), null));
    }
    JVar $lexical = this.type.field(12, stringRef, "lexicalValue");
    
    JVar $value = this.type.field(12, this.valueType, "value");
    
    JMethod m = this.type.constructor(2);
    JVar $v = m.param(this.valueType, "v");
    m.body().assign($value, $v);
    m.body().assign($lexical, xducer.generateSerializer($v, null));
    
    m.body().invoke($valueMap, "put").arg(wrapToObject($v)).arg(JExpr._this());
    
    this.type.method(1, stringRef, "toString").body()._return($lexical);
    
    this.type.method(1, this.valueType, "getValue").body()._return($value);
    
    this.type.method(9, this.codeModel.INT, "hashCode").body()._return(JExpr._super().invoke("hashCode"));
    
    JMethod m = this.type.method(9, this.codeModel.BOOLEAN, "equals");
    JVar o = m.param(Object.class, "o");
    m.body()._return(JExpr._super().invoke("equals").arg(o));
    
    JMethod fromValue = this.type.method(17, this.type, "fromValue");
    JVar $v = fromValue.param(this.valueType, "value");
    
    JVar $t = fromValue.body().decl(this.type, "t", JExpr.cast(this.type, $valueMap.invoke("get").arg(wrapToObject($v))));
    
    JConditional cond = fromValue.body()._if($t.eq(JExpr._null()));
    cond._then()._throw(JExpr._new(this.codeModel.ref(IllegalArgumentException.class)));
    cond._else()._return($t);
    
    JMethod fromString = this.type.method(17, this.type, "fromString");
    JVar $str = fromString.param(stringRef, "str");
    
    JExpression rhs = xducer.generateDeserializer($str, null);
    fromString.body()._return(JExpr.invoke("fromValue").arg(rhs));
    if (grammar.serialVersionUID != null)
    {
      this.type._implements(Serializable.class);
      
      this.type.method(4, objectRef, "readResolve").body()._return(JExpr.invoke("fromValue").arg(JExpr.invoke("getValue")));
    }
  }
  
  private JExpression wrapToObject(JExpression var)
  {
    if (this.valueType.isPrimitive()) {
      return ((JPrimitiveType)this.valueType).wrap(var);
    }
    return var;
  }
  
  private String fixUnsafeCharacters(String lexical)
  {
    StringBuffer buf = new StringBuffer();
    int len = lexical.length();
    for (int i = 0; i < len; i++)
    {
      char ch = lexical.charAt(i);
      if (!Character.isJavaIdentifierPart(ch)) {
        buf.append('-');
      } else {
        buf.append(ch);
      }
    }
    return buf.toString();
  }
  
  private boolean sanityCheck(GeneratorContext context)
  {
    for (int i = 0; i < this.values.length; i++)
    {
      if (this.values[i].dt.isContextDependent())
      {
        reportError(context, Messages.format("EnumerationXducer.ContextDependentType"));
        return false;
      }
      if (!(this.values[i].dt instanceof XSDatatype)) {
        reportError(context, Messages.format("EnumerationXducer.UnsupportedTypeForEnum", this.values[i].getName()));
      }
      if (!this.values[0].dt.equals(this.values[i].dt))
      {
        reportError(context, Messages.format("EnumerationXducer.MultipleTypesInEnum", this.values[0].name, this.values[i].name));
        
        return false;
      }
    }
    return true;
  }
  
  public JExpression generateSerializer(JExpression value, SerializerContext context)
  {
    return value.invoke("toString");
  }
  
  public JExpression generateDeserializer(JExpression value, DeserializerContext context)
  {
    return this.type.staticInvoke("fromString").arg(value);
  }
  
  public JExpression generateConstant(ValueExp exp)
  {
    for (int i = 0; i < this.values.length; i++) {
      if (exp.dt.sameValue(this.values[i].value, exp.value)) {
        return this.type.staticRef(this.items[i].name());
      }
    }
    throw new JAXBAssertionError();
  }
  
  private ValueExp[] getValues(Expression exp)
  {
    if (!(exp instanceof ChoiceExp))
    {
      if (!(exp instanceof ValueExp))
      {
        System.out.println(ExpressionPrinter.printContentModel(exp));
        
        throw new InternalError("assertion failed");
      }
      return new ValueExp[] { (ValueExp)exp };
    }
    Expression[] children = ((ChoiceExp)exp).getChildren();
    ValueExp[] values = new ValueExp[children.length];
    System.arraycopy(children, 0, values, 0, children.length);
    return values;
  }
  
  private void reportError(GeneratorContext context, String msg)
  {
    context.getErrorReceiver().error(this.sourceLocator, msg);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\xducer\EnumerationXducer.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */