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
    return new JInvocation((JExpression)null, method);
  }
  
  public static JInvocation invoke(JExpression lhs, JMethod method)
  {
    return new JInvocation(lhs, method);
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
    return new JFieldRef(lhs, field);
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
    new JExpressionImpl()
    {
      public void generate(JFormatter f)
      {
        JClass c;
        JClass c;
        if ((this.val$cl instanceof JNarrowedClass)) {
          c = ((JNarrowedClass)this.val$cl).basis;
        } else {
          c = this.val$cl;
        }
        f.g(c).p(".class");
      }
    };
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
    return newArray(type, null);
  }
  
  public static JArray newArray(JType type, JExpression size)
  {
    return new JArray(type.erasure(), size);
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
  static final String charEscape = "\b\t\n\f\r\"'\\";
  static final String charMacro = "btnfr\"'\\";
  
  public static JExpression lit(boolean b)
  {
    return b ? TRUE : FALSE;
  }
  
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
    StringBuilder sb = new StringBuilder(n + 2);
    sb.append(quote);
    for (int i = 0; i < n; i++)
    {
      char c = s.charAt(i);
      int j = "\b\t\n\f\r\"'\\".indexOf(c);
      if (j >= 0)
      {
        sb.append('\\');
        sb.append("btnfr\"'\\".charAt(j));
      }
      else if ((c < ' ') || ('~' < c))
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
    new JExpressionImpl()
    {
      public void generate(JFormatter f)
      {
        f.p('(').p(this.val$source).p(')');
      }
    };
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JExpr.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */