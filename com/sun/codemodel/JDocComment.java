package com.sun.codemodel;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class JDocComment
  extends JCommentPart
  implements JGenerable
{
  private final Map<String, JCommentPart> atParams = new HashMap();
  private final Map<String, Map<String, String>> atXdoclets = new HashMap();
  private final Map<JClass, JCommentPart> atThrows = new HashMap();
  private JCommentPart atReturn = null;
  private JCommentPart atDeprecated = null;
  private final JCodeModel owner;
  private static final String INDENT = " *     ";
  
  public JDocComment(JCodeModel owner)
  {
    this.owner = owner;
  }
  
  public JDocComment append(Object o)
  {
    add(o);
    return this;
  }
  
  public JCommentPart addParam(String param)
  {
    JCommentPart p = (JCommentPart)this.atParams.get(param);
    if (p == null) {
      this.atParams.put(param, p = new JCommentPart());
    }
    return p;
  }
  
  public JCommentPart addParam(JVar param)
  {
    return addParam(param.name());
  }
  
  public JCommentPart addThrows(Class exception)
  {
    return addThrows(this.owner.ref(exception));
  }
  
  public JCommentPart addThrows(JClass exception)
  {
    JCommentPart p = (JCommentPart)this.atThrows.get(exception);
    if (p == null) {
      this.atThrows.put(exception, p = new JCommentPart());
    }
    return p;
  }
  
  public JCommentPart addReturn()
  {
    if (this.atReturn == null) {
      this.atReturn = new JCommentPart();
    }
    return this.atReturn;
  }
  
  public JCommentPart addDeprecated()
  {
    if (this.atDeprecated == null) {
      this.atDeprecated = new JCommentPart();
    }
    return this.atDeprecated;
  }
  
  public Map<String, String> addXdoclet(String name)
  {
    Map<String, String> p = (Map)this.atXdoclets.get(name);
    if (p == null) {
      this.atXdoclets.put(name, p = new HashMap());
    }
    return p;
  }
  
  public Map<String, String> addXdoclet(String name, Map<String, String> attributes)
  {
    Map<String, String> p = (Map)this.atXdoclets.get(name);
    if (p == null) {
      this.atXdoclets.put(name, p = new HashMap());
    }
    p.putAll(attributes);
    return p;
  }
  
  public Map<String, String> addXdoclet(String name, String attribute, String value)
  {
    Map<String, String> p = (Map)this.atXdoclets.get(name);
    if (p == null) {
      this.atXdoclets.put(name, p = new HashMap());
    }
    p.put(attribute, value);
    return p;
  }
  
  public void generate(JFormatter f)
  {
    f.p("/**").nl();
    
    format(f, " * ");
    
    f.p(" * ").nl();
    for (Map.Entry<String, JCommentPart> e : this.atParams.entrySet())
    {
      f.p(" * @param ").p((String)e.getKey()).nl();
      ((JCommentPart)e.getValue()).format(f, " *     ");
    }
    if (this.atReturn != null)
    {
      f.p(" * @return").nl();
      this.atReturn.format(f, " *     ");
    }
    for (Map.Entry<JClass, JCommentPart> e : this.atThrows.entrySet())
    {
      f.p(" * @throws ").t((JClass)e.getKey()).nl();
      ((JCommentPart)e.getValue()).format(f, " *     ");
    }
    if (this.atDeprecated != null)
    {
      f.p(" * @deprecated").nl();
      this.atDeprecated.format(f, " *     ");
    }
    for (Map.Entry<String, Map<String, String>> e : this.atXdoclets.entrySet())
    {
      f.p(" * @").p((String)e.getKey());
      if (e.getValue() != null) {
        for (Map.Entry<String, String> a : ((Map)e.getValue()).entrySet()) {
          f.p(" ").p((String)a.getKey()).p("= \"").p((String)a.getValue()).p("\"");
        }
      }
      f.nl();
    }
    f.p(" */").nl();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JDocComment.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */