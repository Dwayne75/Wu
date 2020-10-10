package com.sun.xml.xsom;

import com.sun.xml.xsom.impl.scd.Iterators;
import com.sun.xml.xsom.impl.scd.SCDImpl;
import com.sun.xml.xsom.impl.scd.SCDParser;
import com.sun.xml.xsom.impl.scd.Step;
import com.sun.xml.xsom.impl.scd.Token;
import com.sun.xml.xsom.impl.scd.TokenMgrError;
import com.sun.xml.xsom.util.DeferedCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.NamespaceContext;

public abstract class SCD
{
  public static SCD create(String path, NamespaceContext nsContext)
    throws java.text.ParseException
  {
    try
    {
      SCDParser p = new SCDParser(path, nsContext);
      List<?> list = p.RelativeSchemaComponentPath();
      return new SCDImpl(path, (Step[])list.toArray(new Step[list.size()]));
    }
    catch (TokenMgrError e)
    {
      throw setCause(new java.text.ParseException(e.getMessage(), -1), e);
    }
    catch (com.sun.xml.xsom.impl.scd.ParseException e)
    {
      throw setCause(new java.text.ParseException(e.getMessage(), e.currentToken.beginColumn), e);
    }
  }
  
  private static java.text.ParseException setCause(java.text.ParseException e, Throwable x)
  {
    e.initCause(x);
    return e;
  }
  
  public final Collection<XSComponent> select(XSComponent contextNode)
  {
    return new DeferedCollection(select(Iterators.singleton(contextNode)));
  }
  
  public final Collection<XSComponent> select(XSSchemaSet contextNode)
  {
    return select(contextNode.getSchemas());
  }
  
  public final XSComponent selectSingle(XSComponent contextNode)
  {
    Iterator<XSComponent> r = select(Iterators.singleton(contextNode));
    if (r.hasNext()) {
      return (XSComponent)r.next();
    }
    return null;
  }
  
  public final XSComponent selectSingle(XSSchemaSet contextNode)
  {
    Iterator<XSComponent> r = select(contextNode.iterateSchema());
    if (r.hasNext()) {
      return (XSComponent)r.next();
    }
    return null;
  }
  
  public abstract Iterator<XSComponent> select(Iterator<? extends XSComponent> paramIterator);
  
  public final Collection<XSComponent> select(Collection<? extends XSComponent> contextNodes)
  {
    return new DeferedCollection(select(contextNodes.iterator()));
  }
  
  public abstract String toString();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\SCD.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */