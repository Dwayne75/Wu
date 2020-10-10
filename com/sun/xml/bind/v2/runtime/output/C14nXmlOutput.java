package com.sun.xml.bind.v2.runtime.output;

import com.sun.istack.FinalArrayList;
import com.sun.xml.bind.v2.runtime.Name;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;

public class C14nXmlOutput
  extends UTF8XmlOutput
{
  public C14nXmlOutput(OutputStream out, Encoded[] localNames, boolean namedAttributesAreOrdered)
  {
    super(out, localNames);
    this.namedAttributesAreOrdered = namedAttributesAreOrdered;
    for (int i = 0; i < this.staticAttributes.length; i++) {
      this.staticAttributes[i] = new StaticAttribute();
    }
  }
  
  private StaticAttribute[] staticAttributes = new StaticAttribute[8];
  private int len = 0;
  private int[] nsBuf = new int[8];
  private final FinalArrayList<DynamicAttribute> otherAttributes = new FinalArrayList();
  private final boolean namedAttributesAreOrdered;
  
  final class StaticAttribute
    implements Comparable<StaticAttribute>
  {
    Name name;
    String value;
    
    StaticAttribute() {}
    
    public void set(Name name, String value)
    {
      this.name = name;
      this.value = value;
    }
    
    void write()
      throws IOException
    {
      C14nXmlOutput.this.attribute(this.name, this.value);
    }
    
    C14nXmlOutput.DynamicAttribute toDynamicAttribute()
    {
      int nsUriIndex = this.name.nsUriIndex;
      int prefix;
      int prefix;
      if (nsUriIndex == -1) {
        prefix = -1;
      } else {
        prefix = C14nXmlOutput.this.nsUriIndex2prefixIndex[nsUriIndex];
      }
      return new C14nXmlOutput.DynamicAttribute(C14nXmlOutput.this, prefix, this.name.localName, this.value);
    }
    
    public int compareTo(StaticAttribute that)
    {
      return this.name.compareTo(that.name);
    }
  }
  
  final class DynamicAttribute
    implements Comparable<DynamicAttribute>
  {
    final int prefix;
    final String localName;
    final String value;
    
    public DynamicAttribute(int prefix, String localName, String value)
    {
      this.prefix = prefix;
      this.localName = localName;
      this.value = value;
    }
    
    private String getURI()
    {
      if (this.prefix == -1) {
        return "";
      }
      return C14nXmlOutput.this.nsContext.getNamespaceURI(this.prefix);
    }
    
    public int compareTo(DynamicAttribute that)
    {
      int r = getURI().compareTo(that.getURI());
      if (r != 0) {
        return r;
      }
      return this.localName.compareTo(that.localName);
    }
  }
  
  public void attribute(Name name, String value)
    throws IOException
  {
    if (this.staticAttributes.length == this.len)
    {
      int newLen = this.len * 2;
      StaticAttribute[] newbuf = new StaticAttribute[newLen];
      System.arraycopy(this.staticAttributes, 0, newbuf, 0, this.len);
      for (int i = this.len; i < newLen; i++) {
        this.staticAttributes[i] = new StaticAttribute();
      }
      this.staticAttributes = newbuf;
    }
    this.staticAttributes[(this.len++)].set(name, value);
  }
  
  public void attribute(int prefix, String localName, String value)
    throws IOException
  {
    this.otherAttributes.add(new DynamicAttribute(prefix, localName, value));
  }
  
  public void endStartTag()
    throws IOException
  {
    if (this.otherAttributes.isEmpty())
    {
      if (this.len != 0)
      {
        if (!this.namedAttributesAreOrdered) {
          Arrays.sort(this.staticAttributes, 0, this.len);
        }
        for (int i = 0; i < this.len; i++) {
          this.staticAttributes[i].write();
        }
        this.len = 0;
      }
    }
    else
    {
      for (int i = 0; i < this.len; i++) {
        this.otherAttributes.add(this.staticAttributes[i].toDynamicAttribute());
      }
      this.len = 0;
      Collections.sort(this.otherAttributes);
      
      int size = this.otherAttributes.size();
      for (int i = 0; i < size; i++)
      {
        DynamicAttribute a = (DynamicAttribute)this.otherAttributes.get(i);
        super.attribute(a.prefix, a.localName, a.value);
      }
      this.otherAttributes.clear();
    }
    super.endStartTag();
  }
  
  protected void writeNsDecls(int base)
    throws IOException
  {
    int count = this.nsContext.getCurrent().count();
    if (count == 0) {
      return;
    }
    if (count > this.nsBuf.length) {
      this.nsBuf = new int[count];
    }
    for (int i = count - 1; i >= 0; i--) {
      this.nsBuf[i] = (base + i);
    }
    for (int i = 0; i < count; i++) {
      for (int j = i + 1; j < count; j++)
      {
        String p = this.nsContext.getPrefix(this.nsBuf[i]);
        String q = this.nsContext.getPrefix(this.nsBuf[j]);
        if (p.compareTo(q) > 0)
        {
          int t = this.nsBuf[j];
          this.nsBuf[j] = this.nsBuf[i];
          this.nsBuf[i] = t;
        }
      }
    }
    for (int i = 0; i < count; i++) {
      writeNsDecl(this.nsBuf[i]);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\output\C14nXmlOutput.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */