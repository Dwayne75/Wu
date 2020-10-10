package com.sun.tools.xjc.generator;

import com.sun.codemodel.fmt.JStaticJavaFile.LineFilter;
import java.text.ParseException;
import java.util.Stack;

public abstract class PreProcessingLineFilter
  implements JStaticJavaFile.LineFilter
{
  private final Stack conditions = new Stack();
  private static final String META_TOKEN = "// META-";
  
  private boolean isOn()
  {
    for (int i = this.conditions.size() - 1; i >= 0; i--) {
      if (!((Boolean)this.conditions.get(i)).booleanValue()) {
        return false;
      }
    }
    return true;
  }
  
  public String process(String line)
    throws ParseException
  {
    int idx = line.indexOf("// META-");
    if (idx < 0)
    {
      if (isOn()) {
        return line;
      }
      return null;
    }
    String cond = line.substring(idx + "// META-".length()).trim();
    if (cond.startsWith("IF("))
    {
      idx = cond.indexOf(')');
      if (idx < 0) {
        throw new ParseException("Unable to parse " + cond, -1);
      }
      String exp = cond.substring(3, idx);
      
      this.conditions.push(eval(exp) ? Boolean.TRUE : Boolean.FALSE);
      return null;
    }
    if (cond.equals("ELSE"))
    {
      Boolean b = (Boolean)this.conditions.pop();
      this.conditions.push(!b.booleanValue() ? Boolean.TRUE : Boolean.FALSE);
      return null;
    }
    if (cond.equals("ENDIF"))
    {
      this.conditions.pop();
      return null;
    }
    throw new ParseException("unrecognized meta statement " + line, -1);
  }
  
  private boolean eval(String exp)
    throws ParseException
  {
    boolean r = getVar(exp.charAt(0));
    int i = 1;
    if (i < exp.length())
    {
      char op = exp.charAt(i++);
      if (i == exp.length()) {
        throw new ParseException("Unable to parse " + exp, -1);
      }
      boolean rhs = getVar(exp.charAt(i++));
      switch (op)
      {
      case '|': 
        r |= rhs; break;
      case '&': 
        r &= rhs; break;
      default: 
        throw new ParseException("Unable to parse" + exp, -1);
      }
    }
    return r;
  }
  
  protected abstract boolean getVar(char paramChar)
    throws ParseException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\PreProcessingLineFilter.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */