package com.sun.tools.xjc.generator.marshaller;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JVar;
import com.sun.msv.datatype.xsd.QnameType;
import com.sun.msv.datatype.xsd.QnameValueType;
import com.sun.msv.datatype.xsd.XSDatatype;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.generator.util.BlockReference;
import com.sun.xml.bind.JAXBAssertionError;
import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;
import org.relaxng.datatype.Datatype;

abstract class AbstractPassImpl
  implements Pass
{
  private final String name;
  protected final Context context;
  
  AbstractPassImpl(Context _context, String _name)
  {
    this.context = _context;
    this.name = _name;
  }
  
  public final String getName()
  {
    return this.name;
  }
  
  public final void build(Expression exp)
  {
    Pass old = this.context.currentPass;
    this.context.currentPass = this;
    this.context.build(exp);
    this.context.currentPass = old;
  }
  
  protected final void marshalValue(ValueExp exp)
  {
    if (!exp.dt.isContextDependent())
    {
      String literal = null;
      if ((exp.dt instanceof XSDatatype)) {
        literal = ((XSDatatype)exp.dt).convertToLexicalValue(exp.value, null);
      }
      if (literal == null) {
        throw new JAXBAssertionError();
      }
      getBlock(true).invoke(this.context.$serializer, "text").arg(JExpr.lit(literal)).arg(JExpr._null());
    }
    else if ((exp.dt instanceof QnameType))
    {
      QnameValueType qn = (QnameValueType)exp.value;
      
      getBlock(true).invoke(this.context.$serializer, "text").arg(this.context.codeModel.ref(DatatypeConverter.class).staticInvoke("printQName").arg(JExpr._new(this.context.codeModel.ref(QName.class)).arg(JExpr.lit(qn.namespaceURI)).arg(JExpr.lit(qn.localPart))).arg(this.context.$serializer.invoke("getNamespaceContext"))).arg(JExpr._null());
    }
    else
    {
      throw new JAXBAssertionError("unsupported datatype " + exp.name);
    }
  }
  
  protected final JBlock getBlock(boolean create)
  {
    return this.context.getCurrentBlock().get(create);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\marshaller\AbstractPassImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */