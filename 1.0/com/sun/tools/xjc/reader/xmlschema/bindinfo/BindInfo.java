package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.xml.xsom.XSComponent;
import java.util.Vector;
import javax.xml.namespace.QName;
import org.xml.sax.Locator;

public final class BindInfo
{
  private final Locator location;
  private String documentation;
  
  public BindInfo(Locator loc)
  {
    this.location = loc;
  }
  
  private boolean _hasTitleInDocumentation = false;
  private XSComponent owner;
  private BGMBuilder builder;
  
  public Locator getSourceLocation()
  {
    return this.location;
  }
  
  public void setOwner(BGMBuilder _builder, XSComponent _owner)
  {
    this.owner = _owner;
    this.builder = _builder;
  }
  
  public XSComponent getOwner()
  {
    return this.owner;
  }
  
  public BGMBuilder getBuilder()
  {
    return this.builder;
  }
  
  private final Vector decls = new Vector();
  
  public void addDecl(BIDeclaration decl)
  {
    if (decl == null) {
      throw new IllegalArgumentException();
    }
    decl.setParent(this);
    this.decls.add(decl);
  }
  
  public BIDeclaration get(QName name)
  {
    int len = this.decls.size();
    for (int i = 0; i < len; i++)
    {
      BIDeclaration decl = (BIDeclaration)this.decls.get(i);
      if (decl.getName().equals(name)) {
        return decl;
      }
    }
    return null;
  }
  
  public BIDeclaration[] getDecls()
  {
    return (BIDeclaration[])this.decls.toArray(new BIDeclaration[this.decls.size()]);
  }
  
  public boolean hasTitleInDocumentation()
  {
    return this._hasTitleInDocumentation;
  }
  
  public String getDocumentation()
  {
    return this.documentation;
  }
  
  public void appendDocumentation(String fragment, boolean hasTitleInDocumentation)
  {
    if (this.documentation == null)
    {
      this.documentation = fragment;
      this._hasTitleInDocumentation = hasTitleInDocumentation;
    }
    else
    {
      this.documentation = (this.documentation + "\n\n" + fragment);
    }
  }
  
  public void absorb(BindInfo bi)
  {
    for (int i = 0; i < bi.decls.size(); i++) {
      ((BIDeclaration)bi.decls.get(i)).setParent(this);
    }
    this.decls.addAll(bi.decls);
    appendDocumentation(bi.documentation, bi.hasTitleInDocumentation());
  }
  
  public int size()
  {
    return this.decls.size();
  }
  
  public BIDeclaration get(int idx)
  {
    return (BIDeclaration)this.decls.get(idx);
  }
  
  public static final BindInfo empty = new BindInfo(null);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\bindinfo\BindInfo.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */