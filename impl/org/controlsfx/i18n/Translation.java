package impl.org.controlsfx.i18n;

import java.nio.file.Path;
import java.util.Locale;

public class Translation
  implements Comparable<Translation>
{
  private final String localeString;
  private final Locale locale;
  private final Path path;
  
  public Translation(String locale, Path path)
  {
    this.localeString = locale;
    this.path = path;
    
    String[] split = this.localeString.split("_");
    if (split.length == 1) {
      this.locale = new Locale(this.localeString);
    } else if (split.length == 2) {
      this.locale = new Locale(split[0], split[1]);
    } else if (split.length == 3) {
      this.locale = new Locale(split[0], split[1], split[2]);
    } else {
      throw new IllegalArgumentException("Unknown locale string '" + locale + "'");
    }
  }
  
  public final String getLocaleString()
  {
    return this.localeString;
  }
  
  public final Locale getLocale()
  {
    return this.locale;
  }
  
  public final Path getPath()
  {
    return this.path;
  }
  
  public String toString()
  {
    return this.localeString;
  }
  
  public int compareTo(Translation o)
  {
    if (o == null) {
      return 1;
    }
    return this.localeString.compareTo(o.localeString);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\i18n\Translation.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */