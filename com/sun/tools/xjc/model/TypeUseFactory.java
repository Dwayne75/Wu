package com.sun.tools.xjc.model;

import com.sun.xml.bind.v2.TODO;
import com.sun.xml.bind.v2.model.core.ID;
import javax.activation.MimeType;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public final class TypeUseFactory
{
  public static TypeUse makeID(TypeUse t, ID id)
  {
    if (t.idUse() != ID.NONE) {
      throw new IllegalStateException();
    }
    return new TypeUseImpl(t.getInfo(), t.isCollection(), id, t.getExpectedMimeType(), t.getAdapterUse());
  }
  
  public static TypeUse makeMimeTyped(TypeUse t, MimeType mt)
  {
    if (t.getExpectedMimeType() != null) {
      throw new IllegalStateException();
    }
    return new TypeUseImpl(t.getInfo(), t.isCollection(), t.idUse(), mt, t.getAdapterUse());
  }
  
  public static TypeUse makeCollection(TypeUse t)
  {
    if (t.isCollection()) {
      return t;
    }
    CAdapter au = t.getAdapterUse();
    if ((au != null) && (!au.isWhitespaceAdapter()))
    {
      TODO.checkSpec();
      return CBuiltinLeafInfo.STRING_LIST;
    }
    return new TypeUseImpl(t.getInfo(), true, t.idUse(), t.getExpectedMimeType(), null);
  }
  
  public static TypeUse adapt(TypeUse t, CAdapter adapter)
  {
    assert (t.getAdapterUse() == null);
    return new TypeUseImpl(t.getInfo(), t.isCollection(), t.idUse(), t.getExpectedMimeType(), adapter);
  }
  
  public static TypeUse adapt(TypeUse t, Class<? extends XmlAdapter> adapter, boolean copy)
  {
    return adapt(t, new CAdapter(adapter, copy));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\model\TypeUseFactory.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */