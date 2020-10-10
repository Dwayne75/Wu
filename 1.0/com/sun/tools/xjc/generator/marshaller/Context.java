package com.sun.tools.xjc.generator.marshaller;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JVar;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.generator.field.FieldRenderer;
import com.sun.tools.xjc.generator.util.BlockReference;
import com.sun.tools.xjc.generator.util.ExistingBlockReference;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.tools.xjc.grammar.xducer.SerializerContext;
import com.sun.xml.bind.JAXBAssertionError;
import java.util.Map;
import java.util.Stack;

final class Context
  implements SerializerContext
{
  Side currentSide;
  Pass currentPass;
  public final GeneratorContext genContext;
  public final JVar $serializer;
  public final JCodeModel codeModel;
  protected final ClassItem classItem;
  public final ExpressionPool pool;
  boolean inOneOrMore = false;
  private final Builder builder = new Builder(this);
  private FieldUse fu = null;
  private int iota = 0;
  private final Map fieldMarshallers;
  
  public Context(GeneratorContext _genContext, ExpressionPool _pool, ClassItem _class, JBlock codeBlock, JVar _$serializer, Map _fieldMarshallers)
  {
    this.genContext = _genContext;
    this.pool = _pool;
    this.classItem = _class;
    this.$serializer = _$serializer;
    this.fieldMarshallers = _fieldMarshallers;
    this.codeModel = this.classItem.owner.codeModel;
    this.currentSide = this.outside;
    pushNewBlock(new ExistingBlockReference(codeBlock));
  }
  
  protected final JClass getRuntime(Class clazz)
  {
    return this.genContext.getRuntime(clazz);
  }
  
  protected final boolean isInside()
  {
    return this.currentSide == this.inside;
  }
  
  public FieldMarshallerGenerator getMarshaller(FieldItem fi)
  {
    return (FieldMarshallerGenerator)this.fieldMarshallers.get(this.classItem.getDeclaredField(fi.name));
  }
  
  private final Stack overridedFMGs = new Stack();
  
  public void pushNewFieldMarshallerMapping(FieldMarshallerGenerator original, FieldMarshallerGenerator _new)
  {
    Object old = this.fieldMarshallers.put(original.owner().getFieldUse(), _new);
    _assert(old == original);
    
    this.overridedFMGs.push(original);
  }
  
  public void popFieldMarshallerMapping()
  {
    FieldMarshallerGenerator fmg = (FieldMarshallerGenerator)this.overridedFMGs.pop();
    this.fieldMarshallers.put(fmg.owner().getFieldUse(), fmg);
  }
  
  public void pushFieldItem(FieldItem item)
  {
    _assert(this.fu == null);
    this.fu = this.classItem.getDeclaredField(item.name);
    
    this.currentSide = this.inside;
    _assert(this.fu != null);
  }
  
  public void popFieldItem(FieldItem item)
  {
    _assert((this.fu != null) && (this.fu.name.equals(item.name)));
    this.fu = null;
    this.currentSide = this.outside;
  }
  
  public FieldMarshallerGenerator getCurrentFieldMarshaller()
  {
    return (FieldMarshallerGenerator)this.fieldMarshallers.get(this.fu);
  }
  
  private final Stack blocks = new Stack();
  
  public void pushNewBlock(BlockReference newBlock)
  {
    this.blocks.push(newBlock);
  }
  
  public void pushNewBlock(JBlock block)
  {
    pushNewBlock(new ExistingBlockReference(block));
  }
  
  public void popBlock()
  {
    this.blocks.pop();
  }
  
  public BlockReference getCurrentBlock()
  {
    return (BlockReference)this.blocks.peek();
  }
  
  public String createIdentifier()
  {
    return '_' + Integer.toString(this.iota++);
  }
  
  public void build(Expression exp)
  {
    exp.visit(this.builder);
  }
  
  private final Inside inside = new Inside(this);
  private final Outside outside = new Outside(this);
  final Pass bodyPass = new BodyPass(this, "Body");
  final Pass attPass = new AttributePass(this);
  final Pass uriPass = new URIPass(this);
  final Pass skipPass = new SkipPass(this);
  
  public JExpression getNamespaceContext()
  {
    return this.$serializer.invoke("getNamespaceContext");
  }
  
  public JExpression onID(JExpression object, JExpression value)
  {
    return this.$serializer.invoke("onID").arg(object).arg(value);
  }
  
  public JExpression onIDREF(JExpression target)
  {
    return this.$serializer.invoke("onIDREF").arg(target);
  }
  
  public void declareNamespace(JBlock block, JExpression uri, JExpression prefix, JExpression requirePrefix)
  {
    block.invoke(getNamespaceContext(), "declareNamespace").arg(uri).arg(prefix).arg(requirePrefix);
  }
  
  private static void _assert(boolean b)
  {
    if (!b) {
      throw new JAXBAssertionError();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\marshaller\Context.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */