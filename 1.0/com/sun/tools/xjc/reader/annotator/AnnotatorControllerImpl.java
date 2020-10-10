package com.sun.tools.xjc.reader.annotator;

import com.sun.msv.grammar.Expression;
import com.sun.msv.reader.Controller;
import com.sun.msv.reader.GrammarReader;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.reader.NameConverter;
import com.sun.tools.xjc.reader.PackageTracker;
import java.util.Vector;
import org.xml.sax.Locator;

public class AnnotatorControllerImpl
  implements AnnotatorController
{
  private final GrammarReader reader;
  private final PackageTracker tracker;
  private final ErrorReceiver errorReceiver;
  
  public AnnotatorControllerImpl(GrammarReader _reader, ErrorReceiver _errorReceiver, PackageTracker _tracker)
  {
    this.reader = _reader;
    this.tracker = _tracker;
    this.errorReceiver = _errorReceiver;
  }
  
  public NameConverter getNameConverter()
  {
    return NameConverter.smart;
  }
  
  public PackageTracker getPackageTracker()
  {
    return this.tracker;
  }
  
  public void reportError(Expression[] srcs, String msg)
  {
    Vector locs = new Vector();
    for (int i = 0; i < srcs.length; i++)
    {
      Locator loc = this.reader.getDeclaredLocationOf(srcs[i]);
      if (loc != null) {
        locs.add(loc);
      }
    }
    this.reader.controller.error((Locator[])locs.toArray(new Locator[0]), msg, null);
  }
  
  public void reportError(Locator[] srcs, String msg)
  {
    this.reader.controller.error(srcs, msg, null);
  }
  
  public ErrorReceiver getErrorReceiver()
  {
    return this.errorReceiver;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\annotator\AnnotatorControllerImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */