package com.sun.tools.xjc.reader;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.ErrorReceiver;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

public class TypeUtil
{
  public static JType getCommonBaseType(JCodeModel codeModel, Collection<? extends JType> types)
  {
    return getCommonBaseType(codeModel, (JType[])types.toArray(new JType[types.size()]));
  }
  
  public static JType getCommonBaseType(JCodeModel codeModel, JType... t)
  {
    Set<JType> uniqueTypes = new TreeSet(typeComparator);
    for (JType type : t) {
      uniqueTypes.add(type);
    }
    if (uniqueTypes.size() == 1) {
      return (JType)uniqueTypes.iterator().next();
    }
    assert (!uniqueTypes.isEmpty());
    
    uniqueTypes.remove(codeModel.NULL);
    
    Set<JClass> s = null;
    for (JType type : uniqueTypes)
    {
      JClass cls = type.boxify();
      if (s == null) {
        s = getAssignableTypes(cls);
      } else {
        s.retainAll(getAssignableTypes(cls));
      }
    }
    s.add(codeModel.ref(Object.class));
    
    JClass[] raw = (JClass[])s.toArray(new JClass[s.size()]);
    s.clear();
    for (int i = 0; i < raw.length; i++)
    {
      for (int j = 0; j < raw.length; j++) {
        if ((i != j) && 
        
          (raw[i].isAssignableFrom(raw[j]))) {
          break;
        }
      }
      if (j == raw.length) {
        s.add(raw[i]);
      }
    }
    assert (!s.isEmpty());
    
    JClass result = pickOne(s);
    if (result.isParameterized()) {
      return result;
    }
    List<List<JClass>> parameters = new ArrayList(uniqueTypes.size());
    int paramLen = -1;
    for (JType type : uniqueTypes)
    {
      JClass cls = type.boxify();
      JClass bp = cls.getBaseClass(result);
      if (bp.equals(result)) {
        return result;
      }
      assert (bp.isParameterized());
      List<JClass> tp = bp.getTypeParameters();
      parameters.add(tp);
      
      assert ((paramLen == -1) || (paramLen == tp.size()));
      
      paramLen = tp.size();
    }
    List<JClass> paramResult = new ArrayList();
    List<JClass> argList = new ArrayList(parameters.size());
    for (int i = 0; i < paramLen; i++)
    {
      argList.clear();
      for (List<JClass> list : parameters) {
        argList.add(list.get(i));
      }
      JClass bound = (JClass)getCommonBaseType(codeModel, argList);
      boolean allSame = true;
      for (JClass a : argList) {
        allSame &= a.equals(bound);
      }
      if (!allSame) {
        bound = bound.wildcard();
      }
      paramResult.add(bound);
    }
    return result.narrow(paramResult);
  }
  
  private static JClass pickOne(Set<JClass> s)
  {
    for (JClass c : s) {
      if ((c instanceof JDefinedClass)) {
        return c;
      }
    }
    return (JClass)s.iterator().next();
  }
  
  private static Set<JClass> getAssignableTypes(JClass t)
  {
    Set<JClass> r = new TreeSet(typeComparator);
    getAssignableTypes(t, r);
    return r;
  }
  
  private static void getAssignableTypes(JClass t, Set<JClass> s)
  {
    if (!s.add(t)) {
      return;
    }
    s.add(t.erasure());
    
    JClass _super = t._extends();
    if (_super != null) {
      getAssignableTypes(_super, s);
    }
    Iterator<JClass> itr = t._implements();
    while (itr.hasNext()) {
      getAssignableTypes((JClass)itr.next(), s);
    }
  }
  
  public static JType getType(JCodeModel codeModel, String typeName, ErrorReceiver errorHandler, Locator errorSource)
  {
    try
    {
      return codeModel.parseType(typeName);
    }
    catch (ClassNotFoundException ee)
    {
      errorHandler.warning(new SAXParseException(Messages.ERR_CLASS_NOT_FOUND.format(new Object[] { typeName }), errorSource));
    }
    return codeModel.directClass(typeName);
  }
  
  private static final Comparator<JType> typeComparator = new Comparator()
  {
    public int compare(JType t1, JType t2)
    {
      return t1.fullName().compareTo(t2.fullName());
    }
  };
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\TypeUtil.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */