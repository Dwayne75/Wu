package impl.org.controlsfx.i18n;

import javafx.beans.property.SimpleStringProperty;

public class SimpleLocalizedStringProperty
  extends SimpleStringProperty
{
  public SimpleLocalizedStringProperty() {}
  
  public SimpleLocalizedStringProperty(String initialValue)
  {
    super(initialValue);
  }
  
  public SimpleLocalizedStringProperty(Object bean, String name)
  {
    super(bean, name);
  }
  
  public SimpleLocalizedStringProperty(Object bean, String name, String initialValue)
  {
    super(bean, name, initialValue);
  }
  
  public String getValue()
  {
    return Localization.localize(super.getValue());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\i18n\SimpleLocalizedStringProperty.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */