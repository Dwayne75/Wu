package com.sun.tools.xjc.generator.field;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
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
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.generator.JavadocBuilder;
import com.sun.tools.xjc.generator.MethodWriter;
import com.sun.tools.xjc.grammar.FieldUse;

public class ArrayFieldRenderer
  extends AbstractListFieldRenderer
{
  public static final FieldRendererFactory theFactory = new ArrayFieldRenderer.1();
  static Class class$java$util$ArrayList;
  
  protected ArrayFieldRenderer(ClassContext cc, FieldUse fu, JClass coreList)
  {
    super(cc, fu, coreList);
  }
  
  public void generateAccessors()
  {
    JType arrayType = this.fu.type.array();
    
    JType exposedType = this.fu.type;
    
    JType internalType = this.primitiveType != null ? this.primitiveType.getWrapperClass() : this.fu.type;
    
    JMethod $get = this.writer.declareMethod(exposedType.array(), "get" + this.fu.name);
    this.writer.javadoc().appendComment(this.fu.getJavadoc());
    JBlock body = $get.body();
    if (this.$defValues != null)
    {
      JBlock then = body._if(hasSetValue().not())._then();
      JVar $r = then.decl(exposedType.array(), "r", JExpr.newArray(exposedType, this.$defValues.ref("length")));
      
      then.staticInvoke(this.codeModel.ref(System.class), "arraycopy").arg(this.$defValues).arg(JExpr.lit(0)).arg($r).arg(JExpr.lit(0)).arg(this.$defValues.ref("length"));
      
      then._return($r);
    }
    else
    {
      body._if(ref(true).eq(JExpr._null()))._then()._return(JExpr.newArray(exposedType, 0));
    }
    if (this.primitiveType == null)
    {
      body._return(JExpr.cast(arrayType, ref(true).invoke("toArray").arg(JExpr.newArray(this.fu.type, ref(true).invoke("size")))));
    }
    else
    {
      JVar $r = body.decl(exposedType.array(), "r", JExpr.newArray(exposedType, ref(true).invoke("size")));
      JForLoop loop = body._for();
      JVar $i = loop.init(this.codeModel.INT, "__i", JExpr.lit(0));
      loop.test($i.lt($r.ref("length")));
      loop.update($i.incr());
      loop.body().assign($r.component($i), this.primitiveType.unwrap(JExpr.cast(internalType, ref(true).invoke("get").arg($i))));
      
      body._return($r);
    }
    this.writer.javadoc().addReturn("array of\n" + JavadocBuilder.listPossibleTypes(this.fu));
    
    $get = this.writer.declareMethod(exposedType, "get" + this.fu.name);
    JVar $idx = this.writer.addParameter(this.codeModel.INT, "idx");
    if (this.$defValues != null)
    {
      JBlock then = $get.body()._if(hasSetValue().not())._then();
      then._return(this.$defValues.component($idx));
    }
    else
    {
      $get.body()._if(ref(true).eq(JExpr._null()))._then()._throw(JExpr._new(this.codeModel.ref(IndexOutOfBoundsException.class)));
    }
    this.writer.javadoc().appendComment(this.fu.getJavadoc());
    $get.body()._return(unbox(JExpr.cast(internalType, ref(true).invoke("get").arg($idx))));
    
    this.writer.javadoc().addReturn("one of\n" + JavadocBuilder.listPossibleTypes(this.fu));
    
    JMethod $getLength = this.writer.declareMethod(this.codeModel.INT, "get" + this.fu.name + "Length");
    if (this.$defValues != null) {
      $getLength.body()._if(hasSetValue().not())._then()._return(this.$defValues.ref("length"));
    } else {
      $getLength.body()._if(ref(true).eq(JExpr._null()))._then()._return(JExpr.lit(0));
    }
    $getLength.body()._return(ref(true).invoke("size"));
    
    JMethod $set = this.writer.declareMethod(this.codeModel.VOID, "set" + this.fu.name);
    
    this.writer.javadoc().appendComment(this.fu.getJavadoc());
    
    JVar $value = this.writer.addParameter(exposedType.array(), "values");
    $set.body().invoke(ref(false), "clear");
    JVar $len = $set.body().decl(this.codeModel.INT, "len", $value.ref("length"));
    JForLoop _for = $set.body()._for();
    JVar $i = _for.init(this.codeModel.INT, "i", JExpr.lit(0));
    _for.test(JOp.lt($i, $len));
    _for.update($i.incr());
    _for.body().invoke(ref(true), "add").arg(box($value.component($i)));
    
    this.writer.javadoc().addParam($value, "allowed objects are\n" + JavadocBuilder.listPossibleTypes(this.fu));
    
    $set = this.writer.declareMethod(exposedType, "set" + this.fu.name);
    
    $idx = this.writer.addParameter(this.codeModel.INT, "idx");
    $value = this.writer.addParameter(exposedType, "value");
    
    this.writer.javadoc().appendComment(this.fu.getJavadoc());
    
    body = $set.body();
    body._return(unbox(JExpr.cast(internalType, ref(true).invoke("set").arg($idx).arg(box($value)))));
    
    this.writer.javadoc().addParam($value, "allowed object is\n" + JavadocBuilder.listPossibleTypes(this.fu));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\field\ArrayFieldRenderer.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */