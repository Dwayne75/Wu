package com.sun.codemodel;

public final class ClassType
{
  final String declarationToken;
  
  private ClassType(String token)
  {
    this.declarationToken = token;
  }
  
  public static final ClassType CLASS = new ClassType("class");
  public static final ClassType INTERFACE = new ClassType("interface");
  public static final ClassType ANNOTATION_TYPE_DECL = new ClassType("@interface");
  public static final ClassType ENUM = new ClassType("enum");
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\ClassType.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */