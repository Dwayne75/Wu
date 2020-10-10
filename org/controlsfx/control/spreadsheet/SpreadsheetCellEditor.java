package org.controlsfx.control.spreadsheet;

import impl.org.controlsfx.i18n.Localization;
import impl.org.controlsfx.spreadsheet.GridCellEditor;
import impl.org.controlsfx.spreadsheet.GridViewSkin;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.time.LocalDate;
import java.util.List;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.IndexRange;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;

public abstract class SpreadsheetCellEditor
{
  private static final double MAX_EDITOR_HEIGHT = 50.0D;
  private static final DecimalFormat decimalFormat = new DecimalFormat("#.##########");
  SpreadsheetView view;
  
  public SpreadsheetCellEditor(SpreadsheetView view)
  {
    this.view = view;
  }
  
  public final void endEdit(boolean b)
  {
    this.view.getCellsViewSkin().getSpreadsheetCellEditorImpl().endEdit(b);
  }
  
  public void startEdit(Object item)
  {
    startEdit(item, null);
  }
  
  public abstract void startEdit(Object paramObject, String paramString);
  
  public abstract Control getEditor();
  
  public abstract String getControlValue();
  
  public abstract void end();
  
  public double getMaxHeight()
  {
    return 50.0D;
  }
  
  public static class ObjectEditor
    extends SpreadsheetCellEditor
  {
    private final TextField tf;
    
    public ObjectEditor(SpreadsheetView view)
    {
      super();
      this.tf = new TextField();
    }
    
    public void startEdit(Object value, String format)
    {
      if ((value instanceof String)) {
        this.tf.setText(value.toString());
      }
      attachEnterEscapeEventHandler();
      
      this.tf.requestFocus();
      this.tf.end();
    }
    
    public String getControlValue()
    {
      return this.tf.getText();
    }
    
    public void end()
    {
      this.tf.setOnKeyPressed(null);
    }
    
    public TextField getEditor()
    {
      return this.tf;
    }
    
    private void attachEnterEscapeEventHandler()
    {
      this.tf.setOnKeyPressed(new EventHandler()
      {
        public void handle(KeyEvent t)
        {
          if (t.getCode() == KeyCode.ENTER) {
            SpreadsheetCellEditor.ObjectEditor.this.endEdit(true);
          } else if (t.getCode() == KeyCode.ESCAPE) {
            SpreadsheetCellEditor.ObjectEditor.this.endEdit(false);
          }
        }
      });
    }
  }
  
  public static class StringEditor
    extends SpreadsheetCellEditor
  {
    private final TextField tf;
    
    public StringEditor(SpreadsheetView view)
    {
      super();
      this.tf = new TextField();
    }
    
    public void startEdit(Object value, String format)
    {
      if (((value instanceof String)) || (value == null)) {
        this.tf.setText((String)value);
      }
      attachEnterEscapeEventHandler();
      
      this.tf.requestFocus();
      this.tf.selectAll();
    }
    
    public String getControlValue()
    {
      return this.tf.getText();
    }
    
    public void end()
    {
      this.tf.setOnKeyPressed(null);
    }
    
    public TextField getEditor()
    {
      return this.tf;
    }
    
    private void attachEnterEscapeEventHandler()
    {
      this.tf.setOnKeyPressed(new EventHandler()
      {
        public void handle(KeyEvent t)
        {
          if (t.getCode() == KeyCode.ENTER) {
            SpreadsheetCellEditor.StringEditor.this.endEdit(true);
          } else if (t.getCode() == KeyCode.ESCAPE) {
            SpreadsheetCellEditor.StringEditor.this.endEdit(false);
          }
        }
      });
    }
  }
  
  public static class TextAreaEditor
    extends SpreadsheetCellEditor
  {
    private final TextArea textArea;
    
    public TextAreaEditor(SpreadsheetView view)
    {
      super();
      this.textArea = new TextArea();
      this.textArea.setWrapText(true);
      
      this.textArea.minHeightProperty().bind(this.textArea.maxHeightProperty());
    }
    
    public void startEdit(Object value, String format)
    {
      if (((value instanceof String)) || (value == null)) {
        this.textArea.setText((String)value);
      }
      attachEnterEscapeEventHandler();
      
      this.textArea.requestFocus();
      this.textArea.selectAll();
    }
    
    public String getControlValue()
    {
      return this.textArea.getText();
    }
    
    public void end()
    {
      this.textArea.setOnKeyPressed(null);
    }
    
    public TextArea getEditor()
    {
      return this.textArea;
    }
    
    public double getMaxHeight()
    {
      return Double.MAX_VALUE;
    }
    
    private void attachEnterEscapeEventHandler()
    {
      this.textArea.setOnKeyPressed(new EventHandler()
      {
        public void handle(KeyEvent keyEvent)
        {
          if (keyEvent.getCode() == KeyCode.ENTER)
          {
            if (keyEvent.isShiftDown()) {
              SpreadsheetCellEditor.TextAreaEditor.this.textArea.replaceSelection("\n");
            } else {
              SpreadsheetCellEditor.TextAreaEditor.this.endEdit(true);
            }
          }
          else if (keyEvent.getCode() == KeyCode.ESCAPE) {
            SpreadsheetCellEditor.TextAreaEditor.this.endEdit(false);
          } else if (keyEvent.getCode() == KeyCode.TAB) {
            if (keyEvent.isShiftDown())
            {
              SpreadsheetCellEditor.TextAreaEditor.this.textArea.replaceSelection("\t");
              keyEvent.consume();
            }
            else
            {
              SpreadsheetCellEditor.TextAreaEditor.this.endEdit(true);
            }
          }
        }
      });
    }
  }
  
  public static class DoubleEditor
    extends SpreadsheetCellEditor
  {
    private final TextField tf;
    
    public DoubleEditor(SpreadsheetView view)
    {
      super();
      this.tf = new TextField()
      {
        public void insertText(int index, String text)
        {
          String fixedText = fixText(text);
          super.insertText(index, fixedText);
        }
        
        public void replaceText(int start, int end, String text)
        {
          String fixedText = fixText(text);
          super.replaceText(start, end, fixedText);
        }
        
        public void replaceText(IndexRange range, String text)
        {
          replaceText(range.getStart(), range.getEnd(), text);
        }
        
        private String fixText(String text)
        {
          DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Localization.getLocale());
          text = text.replace(' ', ' ');
          return text.replaceAll("\\.", Character.toString(symbols.getDecimalSeparator()));
        }
      };
    }
    
    public void startEdit(Object value, String format)
    {
      if ((value instanceof Double))
      {
        SpreadsheetCellEditor.decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Localization.getLocale()));
        this.tf.setText(((Double)value).isNaN() ? "" : SpreadsheetCellEditor.decimalFormat.format(value));
      }
      else
      {
        this.tf.setText(null);
      }
      this.tf.getStyleClass().removeAll(new String[] { "error" });
      attachEnterEscapeEventHandler();
      
      this.tf.requestFocus();
      this.tf.selectAll();
    }
    
    public void end()
    {
      this.tf.setOnKeyPressed(null);
    }
    
    public TextField getEditor()
    {
      return this.tf;
    }
    
    public String getControlValue()
    {
      NumberFormat format = NumberFormat.getInstance(Localization.getLocale());
      ParsePosition parsePosition = new ParsePosition(0);
      if (this.tf.getText() != null)
      {
        Number number = format.parse(this.tf.getText(), parsePosition);
        if ((number != null) && (parsePosition.getIndex() == this.tf.getText().length())) {
          return String.valueOf(number.doubleValue());
        }
      }
      return this.tf.getText();
    }
    
    private void attachEnterEscapeEventHandler()
    {
      this.tf.setOnKeyPressed(new EventHandler()
      {
        public void handle(KeyEvent t)
        {
          if (t.getCode() == KeyCode.ENTER) {
            try
            {
              if (SpreadsheetCellEditor.DoubleEditor.this.tf.getText().equals(""))
              {
                SpreadsheetCellEditor.DoubleEditor.this.endEdit(true);
              }
              else
              {
                SpreadsheetCellEditor.DoubleEditor.this.tryParsing();
                SpreadsheetCellEditor.DoubleEditor.this.endEdit(true);
              }
            }
            catch (Exception localException) {}
          } else if (t.getCode() == KeyCode.ESCAPE) {
            SpreadsheetCellEditor.DoubleEditor.this.endEdit(false);
          }
        }
      });
      this.tf.setOnKeyReleased(new EventHandler()
      {
        public void handle(KeyEvent t)
        {
          try
          {
            if (SpreadsheetCellEditor.DoubleEditor.this.tf.getText().equals(""))
            {
              SpreadsheetCellEditor.DoubleEditor.this.tf.getStyleClass().removeAll(new String[] { "error" });
            }
            else
            {
              SpreadsheetCellEditor.DoubleEditor.this.tryParsing();
              SpreadsheetCellEditor.DoubleEditor.this.tf.getStyleClass().removeAll(new String[] { "error" });
            }
          }
          catch (Exception e)
          {
            SpreadsheetCellEditor.DoubleEditor.this.tf.getStyleClass().add("error");
          }
        }
      });
    }
    
    private void tryParsing()
      throws ParseException
    {
      NumberFormat format = NumberFormat.getNumberInstance(Localization.getLocale());
      ParsePosition parsePosition = new ParsePosition(0);
      format.parse(this.tf.getText(), parsePosition);
      if (parsePosition.getIndex() != this.tf.getText().length()) {
        throw new ParseException("Invalid input", parsePosition.getIndex());
      }
    }
  }
  
  public static class IntegerEditor
    extends SpreadsheetCellEditor
  {
    private final TextField tf;
    
    public IntegerEditor(SpreadsheetView view)
    {
      super();
      this.tf = new TextField();
    }
    
    public void startEdit(Object value, String format)
    {
      if ((value instanceof Integer)) {
        this.tf.setText(Integer.toString(((Integer)value).intValue()));
      } else {
        this.tf.setText(null);
      }
      this.tf.getStyleClass().removeAll(new String[] { "error" });
      attachEnterEscapeEventHandler();
      
      this.tf.requestFocus();
      this.tf.selectAll();
    }
    
    public void end()
    {
      this.tf.setOnKeyPressed(null);
    }
    
    public TextField getEditor()
    {
      return this.tf;
    }
    
    public String getControlValue()
    {
      return this.tf.getText();
    }
    
    private void attachEnterEscapeEventHandler()
    {
      this.tf.setOnKeyPressed(new EventHandler()
      {
        public void handle(KeyEvent t)
        {
          if (t.getCode() == KeyCode.ENTER) {
            try
            {
              if (SpreadsheetCellEditor.IntegerEditor.this.tf.getText().equals(""))
              {
                SpreadsheetCellEditor.IntegerEditor.this.endEdit(true);
              }
              else
              {
                Integer.parseInt(SpreadsheetCellEditor.IntegerEditor.this.tf.getText());
                SpreadsheetCellEditor.IntegerEditor.this.endEdit(true);
              }
            }
            catch (Exception localException) {}
          } else if (t.getCode() == KeyCode.ESCAPE) {
            SpreadsheetCellEditor.IntegerEditor.this.endEdit(false);
          }
        }
      });
      this.tf.setOnKeyReleased(new EventHandler()
      {
        public void handle(KeyEvent t)
        {
          try
          {
            if (SpreadsheetCellEditor.IntegerEditor.this.tf.getText().equals(""))
            {
              SpreadsheetCellEditor.IntegerEditor.this.tf.getStyleClass().removeAll(new String[] { "error" });
            }
            else
            {
              Integer.parseInt(SpreadsheetCellEditor.IntegerEditor.this.tf.getText());
              SpreadsheetCellEditor.IntegerEditor.this.tf.getStyleClass().removeAll(new String[] { "error" });
            }
          }
          catch (Exception e)
          {
            SpreadsheetCellEditor.IntegerEditor.this.tf.getStyleClass().add("error");
          }
        }
      });
    }
  }
  
  public static class ListEditor<R>
    extends SpreadsheetCellEditor
  {
    private final List<String> itemList;
    private final ComboBox<String> cb;
    private String originalValue;
    
    public ListEditor(SpreadsheetView view, List<String> itemList)
    {
      super();
      this.itemList = itemList;
      this.cb = new ComboBox();
      this.cb.setVisibleRowCount(5);
    }
    
    public void startEdit(Object value, String format)
    {
      if ((value instanceof String)) {
        this.originalValue = value.toString();
      } else {
        this.originalValue = null;
      }
      ObservableList<String> items = FXCollections.observableList(this.itemList);
      this.cb.setItems(items);
      this.cb.setValue(this.originalValue);
      
      attachEnterEscapeEventHandler();
      this.cb.show();
      this.cb.requestFocus();
    }
    
    public void end()
    {
      this.cb.setOnKeyPressed(null);
    }
    
    public ComboBox<String> getEditor()
    {
      return this.cb;
    }
    
    public String getControlValue()
    {
      return (String)this.cb.getSelectionModel().getSelectedItem();
    }
    
    private void attachEnterEscapeEventHandler()
    {
      this.cb.setOnKeyPressed(new EventHandler()
      {
        public void handle(KeyEvent t)
        {
          if (t.getCode() == KeyCode.ESCAPE)
          {
            SpreadsheetCellEditor.ListEditor.this.cb.setValue(SpreadsheetCellEditor.ListEditor.this.originalValue);
            SpreadsheetCellEditor.ListEditor.this.endEdit(false);
          }
          else if (t.getCode() == KeyCode.ENTER)
          {
            SpreadsheetCellEditor.ListEditor.this.endEdit(true);
          }
        }
      });
    }
  }
  
  public static class DateEditor
    extends SpreadsheetCellEditor
  {
    private final DatePicker datePicker;
    private EventHandler<KeyEvent> eh;
    private ChangeListener<LocalDate> cl;
    private boolean ending = false;
    
    public DateEditor(SpreadsheetView view, StringConverter<LocalDate> converter)
    {
      super();
      this.datePicker = new DatePicker();
      this.datePicker.setConverter(converter);
    }
    
    public void startEdit(Object value, String format)
    {
      if ((value instanceof LocalDate)) {
        this.datePicker.setValue((LocalDate)value);
      }
      attachEnterEscapeEventHandler();
      this.datePicker.show();
      this.datePicker.getEditor().requestFocus();
    }
    
    public void end()
    {
      if (this.datePicker.isShowing()) {
        this.datePicker.hide();
      }
      this.datePicker.removeEventFilter(KeyEvent.KEY_PRESSED, this.eh);
      this.datePicker.valueProperty().removeListener(this.cl);
    }
    
    public DatePicker getEditor()
    {
      return this.datePicker;
    }
    
    public String getControlValue()
    {
      return this.datePicker.getEditor().getText();
    }
    
    private void attachEnterEscapeEventHandler()
    {
      this.eh = new EventHandler()
      {
        public void handle(KeyEvent t)
        {
          if (t.getCode() == KeyCode.ENTER)
          {
            SpreadsheetCellEditor.DateEditor.this.ending = true;
            SpreadsheetCellEditor.DateEditor.this.endEdit(true);
            SpreadsheetCellEditor.DateEditor.this.ending = false;
          }
          else if (t.getCode() == KeyCode.ESCAPE)
          {
            SpreadsheetCellEditor.DateEditor.this.endEdit(false);
          }
        }
      };
      this.datePicker.addEventFilter(KeyEvent.KEY_PRESSED, this.eh);
      
      this.cl = new ChangeListener()
      {
        public void changed(ObservableValue<? extends LocalDate> arg0, LocalDate arg1, LocalDate arg2)
        {
          if (!SpreadsheetCellEditor.DateEditor.this.ending) {
            SpreadsheetCellEditor.DateEditor.this.endEdit(true);
          }
        }
      };
      this.datePicker.valueProperty().addListener(this.cl);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\spreadsheet\SpreadsheetCellEditor.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */