package org.controlsfx.control.spreadsheet;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

public abstract class SpreadsheetCellType<T>
{
  protected StringConverter<T> converter;
  
  public SpreadsheetCellType() {}
  
  public SpreadsheetCellType(StringConverter<T> converter)
  {
    this.converter = converter;
  }
  
  public abstract SpreadsheetCellEditor createEditor(SpreadsheetView paramSpreadsheetView);
  
  public String toString(T object, String format)
  {
    return toString(object);
  }
  
  public abstract String toString(T paramT);
  
  public abstract boolean match(Object paramObject);
  
  public boolean isError(Object value)
  {
    return false;
  }
  
  public boolean acceptDrop()
  {
    return false;
  }
  
  public static final SpreadsheetCellType<Object> OBJECT = new ObjectType();
  public abstract T convertValue(Object paramObject);
  
  public static class ObjectType
    extends SpreadsheetCellType<Object>
  {
    public ObjectType()
    {
      this(new StringConverterWithFormat()
      {
        public Object fromString(String arg0)
        {
          return arg0;
        }
        
        public String toString(Object arg0)
        {
          return arg0 == null ? "" : arg0.toString();
        }
      });
    }
    
    public ObjectType(StringConverterWithFormat<Object> converter)
    {
      super();
    }
    
    public String toString()
    {
      return "object";
    }
    
    public boolean match(Object value)
    {
      return true;
    }
    
    public SpreadsheetCell createCell(int row, int column, int rowSpan, int columnSpan, Object value)
    {
      SpreadsheetCell cell = new SpreadsheetCellBase(row, column, rowSpan, columnSpan, this);
      cell.setItem(value);
      return cell;
    }
    
    public SpreadsheetCellEditor createEditor(SpreadsheetView view)
    {
      return new SpreadsheetCellEditor.ObjectEditor(view);
    }
    
    public Object convertValue(Object value)
    {
      return value;
    }
    
    public String toString(Object item)
    {
      return this.converter.toString(item);
    }
  }
  
  public static final StringType STRING = new StringType();
  
  public static class StringType
    extends SpreadsheetCellType<String>
  {
    public StringType()
    {
      this(new DefaultStringConverter());
    }
    
    public StringType(StringConverter<String> converter)
    {
      super();
    }
    
    public String toString()
    {
      return "string";
    }
    
    public boolean match(Object value)
    {
      return true;
    }
    
    public SpreadsheetCell createCell(int row, int column, int rowSpan, int columnSpan, String value)
    {
      SpreadsheetCell cell = new SpreadsheetCellBase(row, column, rowSpan, columnSpan, this);
      cell.setItem(value);
      return cell;
    }
    
    public SpreadsheetCellEditor createEditor(SpreadsheetView view)
    {
      return new SpreadsheetCellEditor.StringEditor(view);
    }
    
    public String convertValue(Object value)
    {
      String convertedValue = (String)this.converter.fromString(value == null ? null : value.toString());
      if ((convertedValue == null) || (convertedValue.equals(""))) {
        return null;
      }
      return convertedValue;
    }
    
    public String toString(String item)
    {
      return this.converter.toString(item);
    }
  }
  
  public static final DoubleType DOUBLE = new DoubleType();
  
  public static class DoubleType
    extends SpreadsheetCellType<Double>
  {
    public DoubleType()
    {
      this(new StringConverterWithFormat(new DoubleStringConverter())
      {
        public String toString(Double item)
        {
          return toStringFormat(item, "");
        }
        
        public Double fromString(String str)
        {
          if ((str == null) || (str.isEmpty()) || ("NaN".equals(str))) {
            return Double.valueOf(NaN.0D);
          }
          return (Double)this.myConverter.fromString(str);
        }
        
        public String toStringFormat(Double item, String format)
        {
          try
          {
            if ((item == null) || (Double.isNaN(item.doubleValue()))) {
              return "";
            }
            return new DecimalFormat(format).format(item);
          }
          catch (Exception ex) {}
          return this.myConverter.toString(item);
        }
      });
    }
    
    public DoubleType(StringConverter<Double> converter)
    {
      super();
    }
    
    public String toString()
    {
      return "double";
    }
    
    public SpreadsheetCell createCell(int row, int column, int rowSpan, int columnSpan, Double value)
    {
      SpreadsheetCell cell = new SpreadsheetCellBase(row, column, rowSpan, columnSpan, this);
      cell.setItem(value);
      return cell;
    }
    
    public SpreadsheetCellEditor createEditor(SpreadsheetView view)
    {
      return new SpreadsheetCellEditor.DoubleEditor(view);
    }
    
    public boolean match(Object value)
    {
      if ((value instanceof Double)) {
        return true;
      }
      try
      {
        this.converter.fromString(value == null ? null : value.toString());
        return true;
      }
      catch (Exception e) {}
      return false;
    }
    
    public Double convertValue(Object value)
    {
      if ((value instanceof Double)) {
        return (Double)value;
      }
      try
      {
        return (Double)this.converter.fromString(value == null ? null : value.toString());
      }
      catch (Exception e) {}
      return null;
    }
    
    public String toString(Double item)
    {
      return this.converter.toString(item);
    }
    
    public String toString(Double item, String format)
    {
      return ((StringConverterWithFormat)this.converter).toStringFormat(item, format);
    }
  }
  
  public static final IntegerType INTEGER = new IntegerType();
  
  public static class IntegerType
    extends SpreadsheetCellType<Integer>
  {
    public IntegerType()
    {
      this(new IntegerStringConverter()
      {
        public String toString(Integer item)
        {
          if ((item == null) || (Double.isNaN(item.intValue()))) {
            return "";
          }
          return super.toString(item);
        }
        
        public Integer fromString(String str)
        {
          if ((str == null) || (str.isEmpty()) || ("NaN".equals(str))) {
            return null;
          }
          try
          {
            Double temp = Double.valueOf(Double.parseDouble(str));
            return Integer.valueOf(temp.intValue());
          }
          catch (Exception e) {}
          return super.fromString(str);
        }
      });
    }
    
    public IntegerType(IntegerStringConverter converter)
    {
      super();
    }
    
    public String toString()
    {
      return "Integer";
    }
    
    public SpreadsheetCell createCell(int row, int column, int rowSpan, int columnSpan, Integer value)
    {
      SpreadsheetCell cell = new SpreadsheetCellBase(row, column, rowSpan, columnSpan, this);
      cell.setItem(value);
      return cell;
    }
    
    public SpreadsheetCellEditor createEditor(SpreadsheetView view)
    {
      return new SpreadsheetCellEditor.IntegerEditor(view);
    }
    
    public boolean match(Object value)
    {
      if ((value instanceof Integer)) {
        return true;
      }
      try
      {
        this.converter.fromString(value == null ? null : value.toString());
        return true;
      }
      catch (Exception e) {}
      return false;
    }
    
    public Integer convertValue(Object value)
    {
      if ((value instanceof Integer)) {
        return (Integer)value;
      }
      try
      {
        return (Integer)this.converter.fromString(value == null ? null : value.toString());
      }
      catch (Exception e) {}
      return null;
    }
    
    public String toString(Integer item)
    {
      return this.converter.toString(item);
    }
  }
  
  public static final ListType LIST(List<String> items)
  {
    return new ListType(items);
  }
  
  public static class ListType
    extends SpreadsheetCellType<String>
  {
    protected final List<String> items;
    
    public ListType(List<String> items)
    {
      super()
      {
        public String fromString(String str)
        {
          if ((str != null) && (SpreadsheetCellType.ListType.this.contains(str))) {
            return str;
          }
          return null;
        }
      };
      this.items = items;
    }
    
    public String toString()
    {
      return "list";
    }
    
    public SpreadsheetCell createCell(int row, int column, int rowSpan, int columnSpan, String value)
    {
      SpreadsheetCell cell = new SpreadsheetCellBase(row, column, rowSpan, columnSpan, this);
      if ((this.items != null) && (this.items.size() > 0)) {
        if ((value != null) && (this.items.contains(value))) {
          cell.setItem(value);
        } else {
          cell.setItem(this.items.get(0));
        }
      }
      return cell;
    }
    
    public SpreadsheetCellEditor createEditor(SpreadsheetView view)
    {
      return new SpreadsheetCellEditor.ListEditor(view, this.items);
    }
    
    public boolean match(Object value)
    {
      if (((value instanceof String)) && (this.items.contains(value.toString()))) {
        return true;
      }
      return this.items.contains(value == null ? null : value.toString());
    }
    
    public String convertValue(Object value)
    {
      return (String)this.converter.fromString(value == null ? null : value.toString());
    }
    
    public String toString(String item)
    {
      return this.converter.toString(item);
    }
  }
  
  public static final DateType DATE = new DateType();
  
  public static class DateType
    extends SpreadsheetCellType<LocalDate>
  {
    public DateType()
    {
      this(new StringConverterWithFormat()
      {
        public String toString(LocalDate item)
        {
          return toStringFormat(item, "");
        }
        
        public LocalDate fromString(String str)
        {
          try
          {
            return LocalDate.parse(str);
          }
          catch (Exception e) {}
          return null;
        }
        
        public String toStringFormat(LocalDate item, String format)
        {
          if (("".equals(format)) && (item != null)) {
            return item.toString();
          }
          if (item != null) {
            return item.format(DateTimeFormatter.ofPattern(format));
          }
          return "";
        }
      });
    }
    
    public DateType(StringConverter<LocalDate> converter)
    {
      super();
    }
    
    public String toString()
    {
      return "date";
    }
    
    public SpreadsheetCell createCell(int row, int column, int rowSpan, int columnSpan, LocalDate value)
    {
      SpreadsheetCell cell = new SpreadsheetCellBase(row, column, rowSpan, columnSpan, this);
      cell.setItem(value);
      return cell;
    }
    
    public SpreadsheetCellEditor createEditor(SpreadsheetView view)
    {
      return new SpreadsheetCellEditor.DateEditor(view, this.converter);
    }
    
    public boolean match(Object value)
    {
      if ((value instanceof LocalDate)) {
        return true;
      }
      try
      {
        LocalDate temp = (LocalDate)this.converter.fromString(value == null ? null : value.toString());
        return temp != null;
      }
      catch (Exception e) {}
      return false;
    }
    
    public LocalDate convertValue(Object value)
    {
      if ((value instanceof LocalDate)) {
        return (LocalDate)value;
      }
      try
      {
        return (LocalDate)this.converter.fromString(value == null ? null : value.toString());
      }
      catch (Exception e) {}
      return null;
    }
    
    public String toString(LocalDate item)
    {
      return this.converter.toString(item);
    }
    
    public String toString(LocalDate item, String format)
    {
      return ((StringConverterWithFormat)this.converter).toStringFormat(item, format);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\spreadsheet\SpreadsheetCellType.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */