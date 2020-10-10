package javax.xml.bind;

import java.io.Serializable;
import javax.xml.namespace.QName;

public class JAXBElement<T>
  implements Serializable
{
  protected final QName name;
  protected final Class<T> declaredType;
  protected final Class scope;
  protected T value;
  protected boolean nil = false;
  private static final long serialVersionUID = 1L;
  
  public JAXBElement(QName name, Class<T> declaredType, Class scope, T value)
  {
    if ((declaredType == null) || (name == null)) {
      throw new IllegalArgumentException();
    }
    this.declaredType = declaredType;
    if (scope == null) {
      scope = GlobalScope.class;
    }
    this.scope = scope;
    this.name = name;
    setValue(value);
  }
  
  public JAXBElement(QName name, Class<T> declaredType, T value)
  {
    this(name, declaredType, GlobalScope.class, value);
  }
  
  public Class<T> getDeclaredType()
  {
    return this.declaredType;
  }
  
  public QName getName()
  {
    return this.name;
  }
  
  public void setValue(T t)
  {
    this.value = t;
  }
  
  public T getValue()
  {
    return (T)this.value;
  }
  
  public Class getScope()
  {
    return this.scope;
  }
  
  public boolean isNil()
  {
    return (this.value == null) || (this.nil);
  }
  
  public void setNil(boolean value)
  {
    this.nil = value;
  }
  
  public boolean isGlobalScope()
  {
    return this.scope == GlobalScope.class;
  }
  
  public boolean isTypeSubstituted()
  {
    if (this.value == null) {
      return false;
    }
    return this.value.getClass() != this.declaredType;
  }
  
  public static final class GlobalScope {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\xml\bind\JAXBElement.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */