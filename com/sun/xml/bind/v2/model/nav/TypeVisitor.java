package com.sun.xml.bind.v2.model.nav;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

abstract class TypeVisitor<T, P>
{
  public final T visit(Type t, P param)
  {
    assert (t != null);
    if ((t instanceof Class)) {
      return (T)onClass((Class)t, param);
    }
    if ((t instanceof ParameterizedType)) {
      return (T)onParameterizdType((ParameterizedType)t, param);
    }
    if ((t instanceof GenericArrayType)) {
      return (T)onGenericArray((GenericArrayType)t, param);
    }
    if ((t instanceof WildcardType)) {
      return (T)onWildcard((WildcardType)t, param);
    }
    if ((t instanceof TypeVariable)) {
      return (T)onVariable((TypeVariable)t, param);
    }
    if (!$assertionsDisabled) {
      throw new AssertionError();
    }
    throw new IllegalArgumentException();
  }
  
  protected abstract T onClass(Class paramClass, P paramP);
  
  protected abstract T onParameterizdType(ParameterizedType paramParameterizedType, P paramP);
  
  protected abstract T onGenericArray(GenericArrayType paramGenericArrayType, P paramP);
  
  protected abstract T onVariable(TypeVariable paramTypeVariable, P paramP);
  
  protected abstract T onWildcard(WildcardType paramWildcardType, P paramP);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\nav\TypeVisitor.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */