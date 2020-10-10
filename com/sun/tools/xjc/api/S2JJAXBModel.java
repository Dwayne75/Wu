package com.sun.tools.xjc.api;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.tools.xjc.Plugin;
import java.util.Collection;
import java.util.List;
import javax.xml.namespace.QName;

public abstract interface S2JJAXBModel
  extends JAXBModel
{
  public abstract Mapping get(QName paramQName);
  
  public abstract List<JClass> getAllObjectFactories();
  
  public abstract Collection<? extends Mapping> getMappings();
  
  public abstract TypeAndAnnotation getJavaType(QName paramQName);
  
  public abstract JCodeModel generateCode(Plugin[] paramArrayOfPlugin, ErrorListener paramErrorListener);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\api\S2JJAXBModel.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */