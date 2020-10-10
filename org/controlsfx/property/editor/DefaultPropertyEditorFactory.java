package org.controlsfx.property.editor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.util.Callback;
import org.controlsfx.control.PropertySheet.Item;

public class DefaultPropertyEditorFactory
  implements Callback<PropertySheet.Item, PropertyEditor<?>>
{
  public PropertyEditor<?> call(PropertySheet.Item item)
  {
    Class<?> type = item.getType();
    if (item.getPropertyEditorClass().isPresent())
    {
      Optional<PropertyEditor<?>> ed = Editors.createCustomEditor(item);
      if (ed.isPresent()) {
        return (PropertyEditor)ed.get();
      }
    }
    if (type == String.class) {
      return Editors.createTextEditor(item);
    }
    if (isNumber(type)) {
      return Editors.createNumericEditor(item);
    }
    if ((type == Boolean.TYPE) || (type == Boolean.class)) {
      return Editors.createCheckEditor(item);
    }
    if (type == LocalDate.class) {
      return Editors.createDateEditor(item);
    }
    if ((type == Color.class) || (type == Paint.class)) {
      return Editors.createColorEditor(item);
    }
    if ((type != null) && (type.isEnum())) {
      return Editors.createChoiceEditor(item, Arrays.asList(type.getEnumConstants()));
    }
    if (type == Font.class) {
      return Editors.createFontEditor(item);
    }
    return null;
  }
  
  private static Class<?>[] numericTypes = { Byte.TYPE, Byte.class, Short.TYPE, Short.class, Integer.TYPE, Integer.class, Long.TYPE, Long.class, Float.TYPE, Float.class, Double.TYPE, Double.class, BigInteger.class, BigDecimal.class };
  
  private static boolean isNumber(Class<?> type)
  {
    if (type == null) {
      return false;
    }
    for (Class<?> cls : numericTypes) {
      if (type == cls) {
        return true;
      }
    }
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\property\editor\DefaultPropertyEditorFactory.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */