package com.sun.tools.xjc.generator.util;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpression;

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
  
  public static WhitespaceNormalizer PRESERVE = new WhitespaceNormalizer.1();
  public static WhitespaceNormalizer REPLACE = new WhitespaceNormalizer.2();
  
  static Class class$(String x0)
  {
    try
    {
      return Class.forName(x0);
    }
    catch (ClassNotFoundException x1)
    {
      throw new NoClassDefFoundError(x1.getMessage());
    }
  }
  
  public static WhitespaceNormalizer COLLAPSE = new WhitespaceNormalizer.3();
  static Class class$com$sun$xml$bind$WhiteSpaceProcessor;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\util\WhitespaceNormalizer.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */