package com.sun.codemodel;

public abstract class JOp
{
  static boolean hasTopOp(JExpression e)
  {
    return ((e instanceof JOp.UnaryOp)) || ((e instanceof JOp.BinaryOp));
  }
  
  public static JExpression minus(JExpression e)
  {
    return new JOp.UnaryOp("-", e);
  }
  
  public static JExpression not(JExpression e)
  {
    if (e == JExpr.TRUE) {
      return JExpr.FALSE;
    }
    if (e == JExpr.FALSE) {
      return JExpr.TRUE;
    }
    return new JOp.UnaryOp("!", e);
  }
  
  public static JExpression complement(JExpression e)
  {
    return new JOp.UnaryOp("~", e);
  }
  
  public static JExpression incr(JExpression e)
  {
    return new JOp.TightUnaryOp(e, "++");
  }
  
  public static JExpression decr(JExpression e)
  {
    return new JOp.TightUnaryOp(e, "--");
  }
  
  public static JExpression plus(JExpression left, JExpression right)
  {
    return new JOp.BinaryOp("+", left, right);
  }
  
  public static JExpression minus(JExpression left, JExpression right)
  {
    return new JOp.BinaryOp("-", left, right);
  }
  
  public static JExpression mul(JExpression left, JExpression right)
  {
    return new JOp.BinaryOp("*", left, right);
  }
  
  public static JExpression div(JExpression left, JExpression right)
  {
    return new JOp.BinaryOp("/", left, right);
  }
  
  public static JExpression mod(JExpression left, JExpression right)
  {
    return new JOp.BinaryOp("%", left, right);
  }
  
  public static JExpression shl(JExpression left, JExpression right)
  {
    return new JOp.BinaryOp("<<", left, right);
  }
  
  public static JExpression shr(JExpression left, JExpression right)
  {
    return new JOp.BinaryOp(">>", left, right);
  }
  
  public static JExpression shrz(JExpression left, JExpression right)
  {
    return new JOp.BinaryOp(">>>", left, right);
  }
  
  public static JExpression band(JExpression left, JExpression right)
  {
    return new JOp.BinaryOp("&", left, right);
  }
  
  public static JExpression bor(JExpression left, JExpression right)
  {
    return new JOp.BinaryOp("|", left, right);
  }
  
  public static JExpression cand(JExpression left, JExpression right)
  {
    if (left == JExpr.TRUE) {
      return right;
    }
    if (right == JExpr.TRUE) {
      return left;
    }
    if (left == JExpr.FALSE) {
      return left;
    }
    if (right == JExpr.FALSE) {
      return right;
    }
    return new JOp.BinaryOp("&&", left, right);
  }
  
  public static JExpression cor(JExpression left, JExpression right)
  {
    if (left == JExpr.TRUE) {
      return left;
    }
    if (right == JExpr.TRUE) {
      return right;
    }
    if (left == JExpr.FALSE) {
      return right;
    }
    if (right == JExpr.FALSE) {
      return left;
    }
    return new JOp.BinaryOp("||", left, right);
  }
  
  public static JExpression xor(JExpression left, JExpression right)
  {
    return new JOp.BinaryOp("^", left, right);
  }
  
  public static JExpression lt(JExpression left, JExpression right)
  {
    return new JOp.BinaryOp("<", left, right);
  }
  
  public static JExpression lte(JExpression left, JExpression right)
  {
    return new JOp.BinaryOp("<=", left, right);
  }
  
  public static JExpression gt(JExpression left, JExpression right)
  {
    return new JOp.BinaryOp(">", left, right);
  }
  
  public static JExpression gte(JExpression left, JExpression right)
  {
    return new JOp.BinaryOp(">=", left, right);
  }
  
  public static JExpression eq(JExpression left, JExpression right)
  {
    return new JOp.BinaryOp("==", left, right);
  }
  
  public static JExpression ne(JExpression left, JExpression right)
  {
    return new JOp.BinaryOp("!=", left, right);
  }
  
  public static JExpression _instanceof(JExpression left, JType right)
  {
    return new JOp.BinaryOp("instanceof", left, right);
  }
  
  public static JExpression cond(JExpression cond, JExpression ifTrue, JExpression ifFalse)
  {
    return new JOp.TernaryOp("?", ":", cond, ifTrue, ifFalse);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\JOp.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */