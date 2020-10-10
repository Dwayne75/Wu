package org.controlsfx.property;

import impl.org.controlsfx.i18n.Localization;
import java.beans.PropertyDescriptor;
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.property.editor.PropertyEditor;

public class BeanProperty
  implements PropertySheet.Item
{
  public static final String CATEGORY_LABEL_KEY = "propertysheet.item.category.label";
  private final Object bean;
  private final PropertyDescriptor beanPropertyDescriptor;
  private final Method readMethod;
  private boolean editable = true;
  private Optional<ObservableValue<? extends Object>> observableValue = Optional.empty();
  
  public BeanProperty(Object bean, PropertyDescriptor propertyDescriptor)
  {
    this.bean = bean;
    this.beanPropertyDescriptor = propertyDescriptor;
    this.readMethod = propertyDescriptor.getReadMethod();
    if (this.beanPropertyDescriptor.getWriteMethod() == null) {
      setEditable(false);
    }
    findObservableValue();
  }
  
  public String getName()
  {
    return this.beanPropertyDescriptor.getDisplayName();
  }
  
  public String getDescription()
  {
    return this.beanPropertyDescriptor.getShortDescription();
  }
  
  public Class<?> getType()
  {
    return this.beanPropertyDescriptor.getPropertyType();
  }
  
  public Object getValue()
  {
    try
    {
      return this.readMethod.invoke(this.bean, new Object[0]);
    }
    catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException e)
    {
      e.printStackTrace();
    }
    return null;
  }
  
  public void setValue(Object value)
  {
    Method writeMethod = this.beanPropertyDescriptor.getWriteMethod();
    if (writeMethod != null) {
      try
      {
        writeMethod.invoke(this.bean, new Object[] { value });
      }
      catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException e)
      {
        e.printStackTrace();
      }
      catch (Throwable e)
      {
        if ((e instanceof PropertyVetoException))
        {
          Alert alert = new Alert(Alert.AlertType.ERROR);
          alert.setTitle(Localization.localize(Localization.asKey("bean.property.change.error.title")));
          alert.setHeaderText(Localization.localize(Localization.asKey("bean.property.change.error.masthead")));
          alert.setContentText(e.getLocalizedMessage());
          alert.showAndWait();
        }
        else
        {
          throw e;
        }
      }
    }
  }
  
  public String getCategory()
  {
    String category = (String)this.beanPropertyDescriptor.getValue("propertysheet.item.category.label");
    if (category == null) {
      category = Localization.localize(Localization.asKey(this.beanPropertyDescriptor.isExpert() ? "bean.property.category.expert" : "bean.property.category.basic"));
    }
    return category;
  }
  
  public Object getBean()
  {
    return this.bean;
  }
  
  public PropertyDescriptor getPropertyDescriptor()
  {
    return this.beanPropertyDescriptor;
  }
  
  public Optional<Class<? extends PropertyEditor<?>>> getPropertyEditorClass()
  {
    if ((this.beanPropertyDescriptor.getPropertyEditorClass() != null) && 
      (PropertyEditor.class.isAssignableFrom(this.beanPropertyDescriptor.getPropertyEditorClass()))) {
      return Optional.of(this.beanPropertyDescriptor.getPropertyEditorClass());
    }
    return super.getPropertyEditorClass();
  }
  
  public boolean isEditable()
  {
    return this.editable;
  }
  
  public void setEditable(boolean editable)
  {
    this.editable = editable;
  }
  
  public Optional<ObservableValue<? extends Object>> getObservableValue()
  {
    return this.observableValue;
  }
  
  private void findObservableValue()
  {
    try
    {
      String propName = this.beanPropertyDescriptor.getName() + "Property";
      Method m = getBean().getClass().getMethod(propName, new Class[0]);
      Object val = m.invoke(getBean(), new Object[0]);
      if ((val != null) && ((val instanceof ObservableValue))) {
        this.observableValue = Optional.of((ObservableValue)val);
      }
    }
    catch (NoSuchMethodException|SecurityException|IllegalAccessException|IllegalArgumentException|InvocationTargetException localNoSuchMethodException) {}
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\property\BeanProperty.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */