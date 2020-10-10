package com.sun.istack.tools;

import com.sun.mirror.type.ArrayType;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.type.PrimitiveType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.type.TypeVariable;
import com.sun.mirror.type.VoidType;
import com.sun.mirror.type.WildcardType;

public abstract class APTTypeVisitor<T, P>
{
  public final T apply(TypeMirror type, P param)
  {
    if ((type instanceof ArrayType)) {
      return (T)onArrayType((ArrayType)type, param);
    }
    if ((type instanceof PrimitiveType)) {
      return (T)onPrimitiveType((PrimitiveType)type, param);
    }
    if ((type instanceof ClassType)) {
      return (T)onClassType((ClassType)type, param);
    }
    if ((type instanceof InterfaceType)) {
      return (T)onInterfaceType((InterfaceType)type, param);
    }
    if ((type instanceof TypeVariable)) {
      return (T)onTypeVariable((TypeVariable)type, param);
    }
    if ((type instanceof VoidType)) {
      return (T)onVoidType((VoidType)type, param);
    }
    if ((type instanceof WildcardType)) {
      return (T)onWildcard((WildcardType)type, param);
    }
    if (!$assertionsDisabled) {
      throw new AssertionError();
    }
    throw new IllegalArgumentException();
  }
  
  protected abstract T onPrimitiveType(PrimitiveType paramPrimitiveType, P paramP);
  
  protected abstract T onArrayType(ArrayType paramArrayType, P paramP);
  
  protected abstract T onClassType(ClassType paramClassType, P paramP);
  
  protected abstract T onInterfaceType(InterfaceType paramInterfaceType, P paramP);
  
  protected abstract T onTypeVariable(TypeVariable paramTypeVariable, P paramP);
  
  protected abstract T onVoidType(VoidType paramVoidType, P paramP);
  
  protected abstract T onWildcard(WildcardType paramWildcardType, P paramP);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\istack\tools\APTTypeVisitor.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */