package com.sun.tools.xjc.api;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.SourcePosition;
import com.sun.mirror.util.Types;

public final class Reference
{
  public final TypeMirror type;
  public final Declaration annotations;
  
  public Reference(MethodDeclaration method)
  {
    this(method.getReturnType(), method);
  }
  
  public Reference(ParameterDeclaration param)
  {
    this(param.getType(), param);
  }
  
  public Reference(TypeDeclaration type, AnnotationProcessorEnvironment env)
  {
    this(env.getTypeUtils().getDeclaredType(type, new TypeMirror[0]), type);
  }
  
  public Reference(TypeMirror type, Declaration annotations)
  {
    if ((type == null) || (annotations == null)) {
      throw new IllegalArgumentException();
    }
    this.type = type;
    this.annotations = annotations;
  }
  
  public SourcePosition getPosition()
  {
    return this.annotations.getPosition();
  }
  
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Reference)) {
      return false;
    }
    Reference that = (Reference)o;
    
    return (this.annotations.equals(that.annotations)) && (this.type.equals(that.type));
  }
  
  public int hashCode()
  {
    return 29 * this.type.hashCode() + this.annotations.hashCode();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\api\Reference.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */