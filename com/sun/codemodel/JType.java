package com.sun.codemodel;

public abstract class JType
  implements JGenerable, Comparable
{
  public static JPrimitiveType parse(JCodeModel codeModel, String typeName)
  {
    if (typeName.equals("void")) {
      return codeModel.VOID;
    }
    if (typeName.equals("boolean")) {
      return codeModel.BOOLEAN;
    }
    if (typeName.equals("byte")) {
      return codeModel.BYTE;
    }
    if (typeName.equals("short")) {
      return codeModel.SHORT;
    }
    if (typeName.equals("char")) {
      return codeModel.CHAR;
    }
    if (typeName.equals("int")) {
      return codeModel.INT;
    }
    if (typeName.equals("float")) {
      return codeModel.FLOAT;
    }
    if (typeName.equals("long")) {
      return codeModel.LONG;
    }
    if (typeName.equals("double")) {
      return codeModel.DOUBLE;
    }
    throw new IllegalArgumentException("Not a primitive type: " + typeName);
  }
  
  public abstract JCodeModel owner();
  
  public abstract String fullName();
  
  public String binaryName()
  {
    return fullName();
  }
  
  public abstract String name();
  
  public abstract JClass array();
  
  public boolean isArray()
  {
    return false;
  }
  
  public boolean isPrimitive()
  {
    return false;
  }
  
  public abstract JClass boxify();
  
  public abstract JType unboxify();
  
  public JType erasure()
  {
    return this;
  }
  
  public final boolean isReference()
  {
    return !isPrimitive();
  }
  
  public JType elementType()
  {
    throw new IllegalArgumentException("Not an array type");
  }
  
  public String toString()
  {
    return getClass().getName() + '(' + fullName() + ')';
  }
  
  public int compareTo(Object o)
  {
    String rhs = ((JType)o).fullName();
    boolean p = fullName().startsWith("java");
    boolean q = rhs.startsWith("java");
    if ((p) && (!q)) {
      return -1;
    }
    if ((!p) && (q)) {
      return 1;
    }
    return fullName().compareTo(rhs);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JType.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */