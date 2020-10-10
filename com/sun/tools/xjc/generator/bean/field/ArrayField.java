package com.sun.tools.xjc.generator.bean.field;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JCommentPart;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JForLoop;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.generator.bean.MethodWriter;
import com.sun.tools.xjc.model.CPropertyInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class ArrayField
  extends AbstractListField
{
  private JMethod $setAll;
  private JMethod $getAll;
  
  class Accessor
    extends AbstractListField.Accessor
  {
    protected Accessor(JExpression $target)
    {
      super($target);
    }
    
    public void toRawValue(JBlock block, JVar $var)
    {
      block.assign($var, ArrayField.this.codeModel.ref(Arrays.class).staticInvoke("asList").arg(this.$target.invoke(ArrayField.this.$getAll)));
    }
    
    public void fromRawValue(JBlock block, String uniqueName, JExpression $var)
    {
      block.invoke(this.$target, ArrayField.this.$setAll).arg($var.invoke("toArray").arg(JExpr.newArray(ArrayField.this.exposedType, $var.invoke("size"))));
    }
  }
  
  ArrayField(ClassOutlineImpl context, CPropertyInfo prop)
  {
    super(context, prop, false);
    generate();
  }
  
  public void generateAccessors()
  {
    MethodWriter writer = this.outline.createMethodWriter();
    Accessor acc = create(JExpr._this());
    
    JType arrayType = this.exposedType.array();
    
    this.$getAll = writer.declareMethod(this.exposedType.array(), "get" + this.prop.getName(true));
    writer.javadoc().append(this.prop.javadoc);
    JBlock body = this.$getAll.body();
    
    body._if(acc.ref(true).eq(JExpr._null()))._then()._return(JExpr.newArray(this.exposedType, 0));
    if (this.primitiveType == null)
    {
      body._return(JExpr.cast(arrayType, acc.ref(true).invoke("toArray").arg(JExpr.newArray(this.implType, acc.ref(true).invoke("size")))));
    }
    else
    {
      JVar $r = body.decl(this.exposedType.array(), "r", JExpr.newArray(this.exposedType, acc.ref(true).invoke("size")));
      JForLoop loop = body._for();
      JVar $i = loop.init(this.codeModel.INT, "__i", JExpr.lit(0));
      loop.test($i.lt($r.ref("length")));
      loop.update($i.incr());
      loop.body().assign($r.component($i), this.primitiveType.unwrap(acc.ref(true).invoke("get").arg($i)));
      
      body._return($r);
    }
    List<Object> returnTypes = listPossibleTypes(this.prop);
    writer.javadoc().addReturn().append("array of\n").append(returnTypes);
    
    JMethod $get = writer.declareMethod(this.exposedType, "get" + this.prop.getName(true));
    JVar $idx = writer.addParameter(this.codeModel.INT, "idx");
    
    $get.body()._if(acc.ref(true).eq(JExpr._null()))._then()._throw(JExpr._new(this.codeModel.ref(IndexOutOfBoundsException.class)));
    
    writer.javadoc().append(this.prop.javadoc);
    $get.body()._return(acc.unbox(acc.ref(true).invoke("get").arg($idx)));
    
    writer.javadoc().addReturn().append("one of\n").append(returnTypes);
    
    JMethod $getLength = writer.declareMethod(this.codeModel.INT, "get" + this.prop.getName(true) + "Length");
    $getLength.body()._if(acc.ref(true).eq(JExpr._null()))._then()._return(JExpr.lit(0));
    
    $getLength.body()._return(acc.ref(true).invoke("size"));
    
    this.$setAll = writer.declareMethod(this.codeModel.VOID, "set" + this.prop.getName(true));
    
    writer.javadoc().append(this.prop.javadoc);
    
    JVar $value = writer.addParameter(this.exposedType.array(), "values");
    this.$setAll.body().invoke(acc.ref(false), "clear");
    JVar $len = this.$setAll.body().decl(this.codeModel.INT, "len", $value.ref("length"));
    JForLoop _for = this.$setAll.body()._for();
    JVar $i = _for.init(this.codeModel.INT, "i", JExpr.lit(0));
    _for.test(JOp.lt($i, $len));
    _for.update($i.incr());
    _for.body().invoke(acc.ref(true), "add").arg(castToImplType(acc.box($value.component($i))));
    
    writer.javadoc().addParam($value).append("allowed objects are\n").append(returnTypes);
    
    JMethod $set = writer.declareMethod(this.exposedType, "set" + this.prop.getName(true));
    
    $idx = writer.addParameter(this.codeModel.INT, "idx");
    $value = writer.addParameter(this.exposedType, "value");
    
    writer.javadoc().append(this.prop.javadoc);
    
    body = $set.body();
    body._return(acc.unbox(acc.ref(true).invoke("set").arg($idx).arg(castToImplType(acc.box($value)))));
    
    writer.javadoc().addParam($value).append("allowed object is\n").append(returnTypes);
  }
  
  protected JClass getCoreListType()
  {
    return this.codeModel.ref(ArrayList.class).narrow(this.exposedType.boxify());
  }
  
  public Accessor create(JExpression targetObject)
  {
    return new Accessor(targetObject);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\bean\field\ArrayField.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */