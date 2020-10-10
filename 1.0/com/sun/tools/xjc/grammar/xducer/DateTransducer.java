package com.sun.tools.xjc.grammar.xducer;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JType;
import com.sun.msv.datatype.xsd.datetime.IDateTimeValueType;
import com.sun.msv.grammar.ValueExp;
import com.sun.xml.bind.util.CalendarConv;
import java.text.DateFormat;
import java.util.Calendar;

public class DateTransducer
  extends TransducerImpl
{
  private final JCodeModel codeModel;
  private final JClass datatypeImpl;
  
  public DateTransducer(JCodeModel cm, JClass datatypeImpl)
  {
    this.codeModel = cm;
    this.datatypeImpl = datatypeImpl;
  }
  
  public JExpression generateConstant(ValueExp exp)
  {
    Calendar data = ((IDateTimeValueType)exp.value).toCalendar();
    
    String str = CalendarConv.formatter.format(data.getTime());
    
    return this.codeModel.ref(CalendarConv.class).staticInvoke("createCalendar").arg(JExpr.lit(str));
  }
  
  public JExpression generateDeserializer(JExpression literal, DeserializerContext context)
  {
    return JExpr.cast(getReturnType(), this.datatypeImpl.staticRef("theInstance").invoke("createJavaObject").arg(literal).arg(JExpr._null()));
  }
  
  public JExpression generateSerializer(JExpression value, SerializerContext context)
  {
    return this.datatypeImpl.staticRef("theInstance").invoke("serializeJavaObject").arg(value).arg(JExpr._null());
  }
  
  public JType getReturnType()
  {
    return this.codeModel.ref(Calendar.class);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\xducer\DateTransducer.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */