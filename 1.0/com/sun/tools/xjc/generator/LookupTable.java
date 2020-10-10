package com.sun.tools.xjc.generator;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JVar;
import com.sun.msv.datatype.xsd.XSDatatype;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.generator.util.BlockReference;
import com.sun.tools.xjc.grammar.xducer.BuiltinDatatypeTransducerFactory;
import com.sun.tools.xjc.grammar.xducer.DeserializerContext;
import com.sun.tools.xjc.grammar.xducer.SerializerContext;
import com.sun.tools.xjc.grammar.xducer.Transducer;
import com.sun.tools.xjc.runtime.UnmarshallingContext;
import com.sun.tools.xjc.runtime.ValidatableObject;
import com.sun.xml.bind.ProxyGroup;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class LookupTable
{
  private final Set entries = new HashSet();
  private final int id;
  private final LookupTableFactory owner;
  private JMethod $lookup;
  private JMethod $reverseLookup;
  private JMethod $add;
  private JFieldVar $map;
  private JFieldVar $rmap;
  private Transducer xducer;
  private GeneratorContext genContext;
  
  LookupTable(LookupTableFactory _owner, int _id)
  {
    this.owner = _owner;
    this.id = _id;
  }
  
  public boolean isConsistentWith(LookupTable rhs)
  {
    for (Iterator itr = this.entries.iterator(); itr.hasNext();)
    {
      LookupTable.Entry a = (LookupTable.Entry)itr.next();
      if (!rhs.isConsistentWith(a)) {
        return false;
      }
    }
    return true;
  }
  
  public boolean isConsistentWith(LookupTable.Entry e)
  {
    for (Iterator itr = this.entries.iterator(); itr.hasNext();)
    {
      LookupTable.Entry a = (LookupTable.Entry)itr.next();
      if (!a.isConsistentWith(e)) {
        return false;
      }
    }
    return true;
  }
  
  public void add(LookupTable.Entry e)
  {
    this.entries.add(e);
    if (this.$lookup != null) {
      generateEntry(e);
    }
  }
  
  public void absorb(LookupTable rhs)
  {
    for (Iterator itr = rhs.entries.iterator(); itr.hasNext();)
    {
      LookupTable.Entry e = (LookupTable.Entry)itr.next();
      add(e);
    }
  }
  
  public JExpression lookup(GeneratorContext context, JExpression literal, JExpression unmContext)
  {
    if (this.$lookup == null) {
      generateCode(context);
    }
    return this.owner.getTableClass().staticInvoke(this.$lookup).arg(literal).arg(unmContext);
  }
  
  public JExpression reverseLookup(JExpression obj, SerializerContext serializer)
  {
    return this.xducer.generateSerializer(this.owner.getTableClass().staticInvoke(this.$reverseLookup).arg(obj), serializer);
  }
  
  public void declareNamespace(BlockReference body, JExpression value, SerializerContext serializer)
  {
    this.xducer.declareNamespace(body, this.owner.getTableClass().staticInvoke(this.$reverseLookup).arg(value), serializer);
  }
  
  private void generateCode(GeneratorContext context)
  {
    this.genContext = context;
    
    JDefinedClass table = this.owner.getTableClass();
    JCodeModel codeModel = table.owner();
    
    this.$map = table.field(28, Map.class, "table" + this.id, JExpr._new(codeModel.ref(HashMap.class)));
    
    this.$rmap = table.field(28, Map.class, "rtable" + this.id, JExpr._new(codeModel.ref(HashMap.class)));
    
    LookupTable.Entry[] e = (LookupTable.Entry[])this.entries.toArray(new LookupTable.Entry[this.entries.size()]);
    
    this.xducer = BuiltinDatatypeTransducerFactory.get(context.getGrammar(), (XSDatatype)LookupTable.Entry.access$000(e[0]).dt);
    for (int i = 0; i < e.length; i++) {
      generateEntry(e[i]);
    }
    this.$lookup = table.method(25, Class.class, "lookup" + this.id);
    JVar $literal = this.$lookup.param(String.class, "literal");
    DeserializerContext dc = new XMLDeserializerContextImpl(this.$lookup.param(context.getRuntime(UnmarshallingContext.class), "context"));
    
    this.$lookup.body()._return(JExpr.cast(codeModel.ref(Class.class), this.$map.invoke("get").arg(this.xducer.generateDeserializer($literal, dc))));
    
    this.$reverseLookup = table.method(25, this.xducer.getReturnType(), "reverseLookup" + this.id);
    JVar $o = this.$reverseLookup.param(Object.class, "o");
    
    this.$reverseLookup.body()._return(JExpr.cast(this.xducer.getReturnType(), this.$rmap.invoke("get").arg(codeModel.ref(ProxyGroup.class).staticInvoke("blindWrap").arg($o).arg(context.getRuntime(ValidatableObject.class).dotclass()).arg(JExpr._null()).invoke("getClass"))));
    
    this.$add = table.method(20, codeModel.VOID, "add" + this.id);
    JVar $key = this.$add.param(Object.class, "key");
    JVar $value = this.$add.param(Object.class, "value");
    
    this.$add.body().invoke(this.$map, "put").arg($key).arg($value);
    this.$add.body().invoke(this.$rmap, "put").arg($value).arg($key);
  }
  
  private void generateEntry(LookupTable.Entry e)
  {
    this.owner.getTableClass().init().invoke("add" + this.id).arg(this.xducer.generateConstant(LookupTable.Entry.access$000(e))).arg(this.genContext.getClassContext(LookupTable.Entry.access$100(e)).implRef.dotclass());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\LookupTable.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */