package com.sun.tools.xjc.generator.util;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JStringLiteral;
import com.sun.xml.bind.WhiteSpaceProcessor;

public abstract class WhitespaceNormalizer
{
  public abstract JExpression generate(JCodeModel paramJCodeModel, JExpression paramJExpression);
  
  public static WhitespaceNormalizer parse(String method)
  {
    if (method.equals("preserve")) {
      return PRESERVE;
    }
    if (method.equals("replace")) {
      return REPLACE;
    }
    if (method.equals("collapse")) {
      return COLLAPSE;
    }
    throw new IllegalArgumentException(method);
  }
  
  public static final WhitespaceNormalizer PRESERVE = new WhitespaceNormalizer()
  {
    public JExpression generate(JCodeModel codeModel, JExpression literal)
    {
      return literal;
    }
  };
  public static final WhitespaceNormalizer REPLACE = new WhitespaceNormalizer()
  {
    public JExpression generate(JCodeModel codeModel, JExpression literal)
    {
      if ((literal instanceof JStringLiteral)) {
        return JExpr.lit(WhiteSpaceProcessor.replace(((JStringLiteral)literal).str));
      }
      return codeModel.ref(WhiteSpaceProcessor.class).staticInvoke("replace").arg(literal);
    }
  };
  public static final WhitespaceNormalizer COLLAPSE = new WhitespaceNormalizer()
  {
    public JExpression generate(JCodeModel codeModel, JExpression literal)
    {
      if ((literal instanceof JStringLiteral)) {
        return JExpr.lit(WhiteSpaceProcessor.collapse(((JStringLiteral)literal).str));
      }
      return codeModel.ref(WhiteSpaceProcessor.class).staticInvoke("collapse").arg(literal);
    }
  };
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\util\WhitespaceNormalizer.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */