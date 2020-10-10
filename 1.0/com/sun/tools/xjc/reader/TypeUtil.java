package com.sun.tools.xjc.reader;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;
import com.sun.xml.bind.JAXBAssertionError;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class TypeUtil
{
  public static JType getCommonBaseType(JCodeModel codeModel, Set types)
  {
    return getCommonBaseType(codeModel, (JType[])types.toArray(new JType[types.size()]));
  }
  
  public static JType getCommonBaseType(JCodeModel codeModel, JType[] t)
  {
    Set uniqueTypes = new TreeSet(typeComparator);
    for (int i = 0; i < t.length; i++) {
      uniqueTypes.add(t[i]);
    }
    if (uniqueTypes.size() == 1) {
      return (JType)uniqueTypes.iterator().next();
    }
    if (uniqueTypes.size() == 0) {
      throw new JAXBAssertionError();
    }
    Set s = null;
    for (Iterator itr = uniqueTypes.iterator(); itr.hasNext();)
    {
      JType type = (JType)itr.next();
      if (type != codeModel.NULL)
      {
        JClass cls = box(codeModel, type);
        if (s == null) {
          s = getAssignableTypes(cls);
        } else {
          s.retainAll(getAssignableTypes(cls));
        }
      }
    }
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
    Iterator itr = s.iterator();
    while (itr.hasNext())
    {
      JClass c = (JClass)itr.next();
      if ((c instanceof JDefinedClass)) {
        return c;
      }
    }
    return (JClass)s.iterator().next();
  }
  
  public static Set getAssignableTypes(JClass t)
  {
    Set s = new TreeSet(typeComparator);
    
    s.add(t.owner().ref(Object.class));
    
    _getAssignableTypes(t, s);
    return s;
  }
  
  private static void _getAssignableTypes(JClass t, Set s)
  {
    if (!s.add(t)) {
      return;
    }
    JClass _super = t._extends();
    if (_super != null) {
      _getAssignableTypes(_super, s);
    }
    Iterator itr = t._implements();
    while (itr.hasNext()) {
      _getAssignableTypes((JClass)itr.next(), s);
    }
  }
  
  private static JClass box(JCodeModel codeModel, JType t)
  {
    if ((t instanceof JClass)) {
      return (JClass)t;
    }
    return ((JPrimitiveType)t).getWrapperClass();
  }
  
  public static JType getType(JCodeModel codeModel, String typeName, ErrorHandler errorHandler, Locator errorSource)
    throws SAXException
  {
    try
    {
      return JType.parse(codeModel, typeName);
    }
    catch (IllegalArgumentException e)
    {
      try
      {
        return codeModel.ref(typeName);
      }
      catch (ClassNotFoundException ee)
      {
        errorHandler.error(new SAXParseException(Messages.format("TypeUtil.ClassNotFound", typeName), errorSource));
      }
    }
    return codeModel.ref(Object.class);
  }
  
  private static final Comparator typeComparator = new TypeUtil.1();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\TypeUtil.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */