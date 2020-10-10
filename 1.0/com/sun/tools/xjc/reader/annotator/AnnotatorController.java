package com.sun.tools.xjc.reader.annotator;

import com.sun.msv.grammar.Expression;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.reader.NameConverter;
import com.sun.tools.xjc.reader.PackageTracker;
import org.xml.sax.Locator;

public abstract interface AnnotatorController
{
  public abstract NameConverter getNameConverter();
  
  public abstract PackageTracker getPackageTracker();
  
  public abstract void reportError(Expression[] paramArrayOfExpression, String paramString);
  
  public abstract void reportError(Locator[] paramArrayOfLocator, String paramString);
  
  public abstract ErrorReceiver getErrorReceiver();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\annotator\AnnotatorController.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */