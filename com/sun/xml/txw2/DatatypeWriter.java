package com.sun.xml.txw2;

import javax.xml.namespace.QName;

public abstract interface DatatypeWriter<DT>
{
  public static final DatatypeWriter<?>[] BUILDIN = { new DatatypeWriter()new DatatypeWriter
  {
    public Class<String> getType()
    {
      return String.class;
    }
    
    public void print(String s, NamespaceResolver resolver, StringBuilder buf)
    {
      buf.append(s);
    }
  }, new DatatypeWriter()new DatatypeWriter
  {
    public Class<Integer> getType()
    {
      return Integer.class;
    }
    
    public void print(Integer i, NamespaceResolver resolver, StringBuilder buf)
    {
      buf.append(i);
    }
  }, new DatatypeWriter()new DatatypeWriter
  {
    public Class<Float> getType()
    {
      return Float.class;
    }
    
    public void print(Float f, NamespaceResolver resolver, StringBuilder buf)
    {
      buf.append(f);
    }
  }, new DatatypeWriter()new DatatypeWriter
  {
    public Class<Double> getType()
    {
      return Double.class;
    }
    
    public void print(Double d, NamespaceResolver resolver, StringBuilder buf)
    {
      buf.append(d);
    }
  }, new DatatypeWriter()
  {
    public Class<QName> getType()
    {
      return QName.class;
    }
    
    public void print(QName qn, NamespaceResolver resolver, StringBuilder buf)
    {
      String p = resolver.getPrefix(qn.getNamespaceURI());
      if (p.length() != 0) {
        buf.append(p).append(':');
      }
      buf.append(qn.getLocalPart());
    }
  } };
  
  public abstract Class<DT> getType();
  
  public abstract void print(DT paramDT, NamespaceResolver paramNamespaceResolver, StringBuilder paramStringBuilder);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\txw2\DatatypeWriter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */