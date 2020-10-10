package org.controlsfx.glyphfont;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public final class GlyphFontRegistry
{
  private static Map<String, GlyphFont> fontMap = new HashMap();
  
  static
  {
    ServiceLoader<GlyphFont> loader = ServiceLoader.load(GlyphFont.class);
    for (GlyphFont font : loader) {
      register(font);
    }
  }
  
  public static void register(String familyName, String uri, int defaultSize)
  {
    register(new GlyphFont(familyName, defaultSize, uri));
  }
  
  public static void register(String familyName, InputStream in, int defaultSize)
  {
    register(new GlyphFont(familyName, defaultSize, in));
  }
  
  public static void register(GlyphFont font)
  {
    if (font != null) {
      fontMap.put(font.getName(), font);
    }
  }
  
  public static GlyphFont font(String familyName)
  {
    GlyphFont font = (GlyphFont)fontMap.get(familyName);
    if (font != null) {
      font.ensureFontIsLoaded();
    }
    return font;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\glyphfont\GlyphFontRegistry.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */