package org.controlsfx.control.spreadsheet;

import javafx.util.StringConverter;

public abstract class StringConverterWithFormat<T>
  extends StringConverter<T>
{
  protected StringConverter<T> myConverter;
  
  public StringConverterWithFormat() {}
  
  public StringConverterWithFormat(StringConverter<T> specificStringConverter)
  {
    this.myConverter = specificStringConverter;
  }
  
  public String toStringFormat(T value, String format)
  {
    return toString(value);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\spreadsheet\StringConverterWithFormat.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */