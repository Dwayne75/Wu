package com.sun.tools.xjc.generator.field;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.generator.JavadocBuilder;
import com.sun.tools.xjc.generator.MethodWriter;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.xml.bind.util.EmptyIterator;
import java.util.Iterator;

public class TypedListFieldRenderer
  extends AbstractListFieldRenderer
{
  public static final FieldRendererFactory theFactory = new TypedListFieldRenderer.1();
  static Class class$java$util$ArrayList;
  
  protected TypedListFieldRenderer(ClassContext context, FieldUse fu, JClass coreList)
  {
    super(context, fu, coreList);
  }
  
  public void generateAccessors()
  {
    JMethod $add = this.writer.declareMethod(this.codeModel.VOID, "add" + this.fu.name);
    JVar $idx = this.writer.addParameter(this.codeModel.INT, "idx");
    JVar $value = this.writer.addParameter(this.fu.type, "value");
    
    this.writer.javadoc().appendComment(this.fu.getJavadoc());
    
    JBlock body = $add.body();
    body.invoke(ref(false), "add").arg($idx).arg($value);
    
    this.writer.javadoc().addParam($value, "allowed object is\n" + JavadocBuilder.listPossibleTypes(this.fu));
    
    JMethod $get = this.writer.declareMethod(this.fu.type, "get" + this.fu.name);
    $idx = this.writer.addParameter(this.codeModel.INT, "idx");
    
    this.writer.javadoc().appendComment(this.fu.getJavadoc());
    
    $get.body()._return(JExpr.cast(this.fu.type, ref(true).invoke("get").arg($idx)));
    
    this.writer.javadoc().addReturn(JavadocBuilder.listPossibleTypes(this.fu));
    
    JMethod $iterate = this.writer.declareMethod(this.codeModel.ref(Iterator.class), "iterate" + this.fu.name);
    
    this.writer.javadoc().appendComment(this.fu.getJavadoc());
    $iterate.body()._return(JOp.cond(ref(true).eq(JExpr._null()), this.codeModel.ref(EmptyIterator.class).staticRef("theInstance"), ref(true).invoke("iterator")));
    
    JMethod $size = this.writer.declareMethod(this.codeModel.INT, "sizeOf" + this.fu.name);
    
    $size.body()._return(count());
    
    JMethod $set = this.writer.declareMethod(this.fu.type, "set" + this.fu.name);
    
    $idx = this.writer.addParameter(this.codeModel.INT, "idx");
    $value = this.writer.addParameter(this.fu.type, "value");
    
    this.writer.javadoc().appendComment(this.fu.getJavadoc());
    
    body = $set.body();
    body._return(JExpr.cast(this.fu.type, ref(false).invoke("set").arg($idx).arg($value)));
    
    this.writer.javadoc().addParam($value, "allowed object is\n" + JavadocBuilder.listPossibleTypes(this.fu));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\field\TypedListFieldRenderer.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */