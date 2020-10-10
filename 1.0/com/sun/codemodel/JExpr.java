package com.sun.codemodel;

public abstract class JExpr
{
  public static JExpression assign(JAssignmentTarget lhs, JExpression rhs)
  {
    return new JAssignment(lhs, rhs);
  }
  
  public static JExpression assignPlus(JAssignmentTarget lhs, JExpression rhs)
  {
    return new JAssignment(lhs, rhs, "+");
  }
  
  public static JInvocation _new(JClass c)
  {
    return new JInvocation(c);
  }
  
  public static JInvocation _new(JType t)
  {
    return new JInvocation(t);
  }
  
  public static JInvocation invoke(String method)
  {
    return new JInvocation((JExpression)null, method);
  }
  
  public static JInvocation invoke(JMethod method)
  {
    return new JInvocation((JExpression)null, method.name());
  }
  
  public static JInvocation invoke(JExpression lhs, JMethod method)
  {
    return new JInvocation(lhs, method.name());
  }
  
  public static JInvocation invoke(JExpression lhs, String method)
  {
    return new JInvocation(lhs, method);
  }
  
  public static JFieldRef ref(String field)
  {
    return new JFieldRef((JExpression)null, field);
  }
  
  public static JFieldRef ref(JExpression lhs, JVar field)
  {
    return new JFieldRef(lhs, field.name());
  }
  
  public static JFieldRef ref(JExpression lhs, String field)
  {
    return new JFieldRef(lhs, field);
  }
  
  public static JFieldRef refthis(String field)
  {
    return new JFieldRef(null, field, true);
  }
  
  public static JExpression dotclass(JClass cl)
  {
    return new JExpr.1(cl);
  }
  
  public static JExpression dotclass(JType t)
  {
    return new JExpr.2(t);
  }
  
  public static JArrayCompRef component(JExpression lhs, JExpression index)
  {
    return new JArrayCompRef(lhs, index);
  }
  
  public static JCast cast(JType type, JExpression expr)
  {
    return new JCast(type, expr);
  }
  
  public static JArray newArray(JType type)
  {
    return new JArray(type, null);
  }
  
  public static JArray newArray(JType type, JExpression size)
  {
    return new JArray(type, size);
  }
  
  public static JArray newArray(JType type, int size)
  {
    return newArray(type, lit(size));
  }
  
  private static final JExpression __this = new JAtom("this");
  
  public static JExpression _this()
  {
    return __this;
  }
  
  private static final JExpression __super = new JAtom("super");
  
  public static JExpression _super()
  {
    return __super;
  }
  
  private static final JExpression __null = new JAtom("null");
  
  public static JExpression _null()
  {
    return __null;
  }
  
  public static final JExpression TRUE = new JAtom("true");
  public static final JExpression FALSE = new JAtom("false");
  static final String charEscape = "\bb\tt\nn\ff\rr\"\"''\\\\";
  
  public static JExpression lit(int n)
  {
    return new JAtom(Integer.toString(n));
  }
  
  public static JExpression lit(long n)
  {
    return new JAtom(Long.toString(n) + "L");
  }
  
  public static JExpression lit(float f)
  {
    return new JAtom(Float.toString(f) + "F");
  }
  
  public static JExpression lit(double d)
  {
    return new JAtom(Double.toString(d) + "D");
  }
  
  public static String quotify(char quote, String s)
  {
    int n = s.length();
    StringBuffer sb = new StringBuffer(n + 2);
    sb.append(quote);
    for (int i = 0; i < n; i++)
    {
      char c = s.charAt(i);
      for (int j = 0; j < 8; j++) {
        if (c == "\bb\tt\nn\ff\rr\"\"''\\\\".charAt(j * 2))
        {
          sb.append('\\');
          sb.append("\bb\tt\nn\ff\rr\"\"''\\\\".charAt(j * 2 + 1));
          break;
        }
      }
      if (j == 8) {
        if ((c < ' ') || ('~' < c))
        {
          sb.append("\\u");
          String hex = Integer.toHexString(c & 0xFFFF);
          for (int k = hex.length(); k < 4; k++) {
            sb.append('0');
          }
          sb.append(hex);
        }
        else
        {
          sb.append(c);
        }
      }
    }
    sb.append(quote);
    return sb.toString();
  }
  
  public static JExpression lit(char c)
  {
    return new JAtom(quotify('\'', "" + c));
  }
  
  public static JExpression lit(String s)
  {
    return new JStringLiteral(s);
  }
  
  public static JExpression direct(String source)
  {
    return new JExpr.3(source);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\JExpr.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */