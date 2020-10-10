package com.sun.codemodel;

public abstract class JType
  implements JGenerable
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
    return getClass().getName() + "(" + fullName() + ")";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\JType.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */