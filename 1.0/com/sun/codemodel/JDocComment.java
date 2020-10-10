package com.sun.codemodel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class JDocComment
  implements JGenerable
{
  private String comment = "";
  private final Map atParams = new HashMap();
  private final Map atThrows = new HashMap();
  private String atReturn = null;
  private String atDeprecated = null;
  
  public String getComment()
  {
    return this.comment;
  }
  
  public JDocComment setComment(String comment)
  {
    this.comment = comment;
    return this;
  }
  
  public JDocComment appendComment(String comment)
  {
    this.comment += comment;
    return this;
  }
  
  public JDocComment addParam(String param, String comment)
  {
    String s = (String)this.atParams.get(param);
    if (s != null) {
      comment = s + comment;
    }
    this.atParams.put(param, comment);
    return this;
  }
  
  public JDocComment addParam(JVar param, String comment)
  {
    return addParam(param.name, comment);
  }
  
  public JDocComment addThrows(String exception, String comment)
  {
    String s = (String)this.atThrows.get(exception);
    if (s != null) {
      comment = s + comment;
    }
    this.atThrows.put(exception, comment);
    return this;
  }
  
  public JDocComment addThrows(Class exception, String comment)
  {
    return addThrows(exception.getName(), comment);
  }
  
  public JDocComment addThrows(JClass exception, String comment)
  {
    return addThrows(exception.fullName(), comment);
  }
  
  public JDocComment addReturn(String comment)
  {
    if (this.atReturn == null) {
      this.atReturn = comment;
    } else {
      this.atReturn += comment;
    }
    return this;
  }
  
  public void setDeprecated(String comment)
  {
    this.atDeprecated = comment;
  }
  
  public void generate(JFormatter f)
  {
    f.p("/**").nl();
    
    format(f, this.comment);
    
    f.p(" * ").nl();
    for (Iterator i = this.atParams.entrySet().iterator(); i.hasNext();)
    {
      Map.Entry e = (Map.Entry)i.next();
      format(f, "@param " + e.getKey(), (String)e.getValue());
    }
    if (this.atReturn != null) {
      format(f, "@return", this.atReturn);
    }
    for (Iterator i = this.atThrows.entrySet().iterator(); i.hasNext();)
    {
      Map.Entry e = (Map.Entry)i.next();
      format(f, "@throws " + e.getKey(), (String)e.getValue());
    }
    if (this.atDeprecated != null) {
      format(f, "@deprecated", this.atDeprecated);
    }
    f.p(" */").nl();
  }
  
  private void format(JFormatter f, String key, String s)
  {
    f.p(" * " + key).nl();
    int idx;
    while ((idx = s.indexOf('\n')) != -1)
    {
      f.p(" *     " + s.substring(0, idx)).nl();
      s = s.substring(idx + 1);
    }
    if (s.length() != 0) {
      f.p(" *     " + s).nl();
    }
  }
  
  private void format(JFormatter f, String s)
  {
    int idx;
    while ((idx = s.indexOf('\n')) != -1)
    {
      f.p(" * " + s.substring(0, idx)).nl();
      s = s.substring(idx + 1);
    }
    if (s.length() != 0) {
      f.p(" * " + s).nl();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\JDocComment.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */