package impl.org.controlsfx.autocompletion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.util.Callback;
import org.controlsfx.control.textfield.AutoCompletionBinding.ISuggestionRequest;

public abstract class SuggestionProvider<T>
  implements Callback<AutoCompletionBinding.ISuggestionRequest, Collection<T>>
{
  private final List<T> possibleSuggestions = new ArrayList();
  private final Object possibleSuggestionsLock = new Object();
  private final BooleanProperty showAllIfEmptyProperty = new SimpleBooleanProperty(false);
  
  public final BooleanProperty showAllIfEmptyProperty()
  {
    return this.showAllIfEmptyProperty;
  }
  
  public final boolean isShowAllIfEmpty()
  {
    return this.showAllIfEmptyProperty.get();
  }
  
  public final void setShowAllIfEmpty(boolean showAllIfEmpty)
  {
    this.showAllIfEmptyProperty.set(showAllIfEmpty);
  }
  
  public void addPossibleSuggestions(T... newPossible)
  {
    addPossibleSuggestions(Arrays.asList(newPossible));
  }
  
  public void addPossibleSuggestions(Collection<T> newPossible)
  {
    synchronized (this.possibleSuggestionsLock)
    {
      this.possibleSuggestions.addAll(newPossible);
    }
  }
  
  public void clearSuggestions()
  {
    synchronized (this.possibleSuggestionsLock)
    {
      this.possibleSuggestions.clear();
    }
  }
  
  public final Collection<T> call(AutoCompletionBinding.ISuggestionRequest request)
  {
    List<T> suggestions = new ArrayList();
    if (!request.getUserText().isEmpty())
    {
      synchronized (this.possibleSuggestionsLock)
      {
        for (T possibleSuggestion : this.possibleSuggestions) {
          if (isMatch(possibleSuggestion, request)) {
            suggestions.add(possibleSuggestion);
          }
        }
      }
      Collections.sort(suggestions, getComparator());
    }
    else if (isShowAllIfEmpty())
    {
      synchronized (this.possibleSuggestionsLock)
      {
        suggestions.addAll(this.possibleSuggestions);
      }
    }
    return suggestions;
  }
  
  protected abstract Comparator<T> getComparator();
  
  protected abstract boolean isMatch(T paramT, AutoCompletionBinding.ISuggestionRequest paramISuggestionRequest);
  
  public static <T> SuggestionProvider<T> create(Collection<T> possibleSuggestions)
  {
    return create(null, possibleSuggestions);
  }
  
  public static <T> SuggestionProvider<T> create(Callback<T, String> stringConverter, Collection<T> possibleSuggestions)
  {
    SuggestionProviderString<T> suggestionProvider = new SuggestionProviderString(stringConverter);
    suggestionProvider.addPossibleSuggestions(possibleSuggestions);
    return suggestionProvider;
  }
  
  private static class SuggestionProviderString<T>
    extends SuggestionProvider<T>
  {
    private Callback<T, String> stringConverter;
    private final Comparator<T> stringComparator = new Comparator()
    {
      public int compare(T o1, T o2)
      {
        String o1str = (String)SuggestionProvider.SuggestionProviderString.this.stringConverter.call(o1);
        String o2str = (String)SuggestionProvider.SuggestionProviderString.this.stringConverter.call(o2);
        return o1str.compareTo(o2str);
      }
    };
    
    public SuggestionProviderString(Callback<T, String> stringConverter)
    {
      this.stringConverter = stringConverter;
      if (this.stringConverter == null) {
        this.stringConverter = new Callback()
        {
          public String call(T obj)
          {
            return obj != null ? obj.toString() : "";
          }
        };
      }
    }
    
    protected Comparator<T> getComparator()
    {
      return this.stringComparator;
    }
    
    protected boolean isMatch(T suggestion, AutoCompletionBinding.ISuggestionRequest request)
    {
      String userTextLower = request.getUserText().toLowerCase();
      String suggestionStr = ((String)this.stringConverter.call(suggestion)).toLowerCase();
      return suggestionStr.contains(userTextLower);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\autocompletion\SuggestionProvider.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */