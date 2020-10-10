package impl.org.controlsfx.autocompletion;

import java.util.Collection;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.AutoCompletionBinding.ISuggestionRequest;

public class AutoCompletionTextFieldBinding<T>
  extends AutoCompletionBinding<T>
{
  private StringConverter<T> converter;
  
  private static <T> StringConverter<T> defaultStringConverter()
  {
    new StringConverter()
    {
      public String toString(T t)
      {
        return t == null ? null : t.toString();
      }
      
      public T fromString(String string)
      {
        return string;
      }
    };
  }
  
  public AutoCompletionTextFieldBinding(TextField textField, Callback<AutoCompletionBinding.ISuggestionRequest, Collection<T>> suggestionProvider)
  {
    this(textField, suggestionProvider, 
      defaultStringConverter());
  }
  
  public AutoCompletionTextFieldBinding(TextField textField, Callback<AutoCompletionBinding.ISuggestionRequest, Collection<T>> suggestionProvider, StringConverter<T> converter)
  {
    super(textField, suggestionProvider, converter);
    this.converter = converter;
    
    getCompletionTarget().textProperty().addListener(this.textChangeListener);
    getCompletionTarget().focusedProperty().addListener(this.focusChangedListener);
  }
  
  public TextField getCompletionTarget()
  {
    return (TextField)super.getCompletionTarget();
  }
  
  public void dispose()
  {
    getCompletionTarget().textProperty().removeListener(this.textChangeListener);
    getCompletionTarget().focusedProperty().removeListener(this.focusChangedListener);
  }
  
  protected void completeUserInput(T completion)
  {
    String newText = this.converter.toString(completion);
    getCompletionTarget().setText(newText);
    getCompletionTarget().positionCaret(newText.length());
  }
  
  private final ChangeListener<String> textChangeListener = new ChangeListener()
  {
    public void changed(ObservableValue<? extends String> obs, String oldText, String newText)
    {
      if (AutoCompletionTextFieldBinding.this.getCompletionTarget().isFocused()) {
        AutoCompletionTextFieldBinding.this.setUserInput(newText);
      }
    }
  };
  private final ChangeListener<Boolean> focusChangedListener = new ChangeListener()
  {
    public void changed(ObservableValue<? extends Boolean> obs, Boolean oldFocused, Boolean newFocused)
    {
      if (!newFocused.booleanValue()) {
        AutoCompletionTextFieldBinding.this.hidePopup();
      }
    }
  };
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\autocompletion\AutoCompletionTextFieldBinding.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */