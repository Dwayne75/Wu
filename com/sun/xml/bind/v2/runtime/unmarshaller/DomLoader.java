package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import javax.xml.bind.annotation.DomHandler;
import javax.xml.transform.Result;
import javax.xml.transform.sax.TransformerHandler;
import org.xml.sax.SAXException;

public class DomLoader<ResultT extends Result>
  extends Loader
{
  private final DomHandler<?, ResultT> dom;
  
  private final class State
  {
    private final TransformerHandler handler = JAXBContextImpl.createTransformerHandler();
    private final ResultT result;
    int depth = 1;
    
    public State(UnmarshallingContext context)
      throws SAXException
    {
      this.result = DomLoader.this.dom.createUnmarshaller(context);
      
      this.handler.setResult(this.result);
      try
      {
        this.handler.setDocumentLocator(context.getLocator());
        this.handler.startDocument();
        declarePrefixes(context, context.getAllDeclaredPrefixes());
      }
      catch (SAXException e)
      {
        context.handleError(e);
        throw e;
      }
    }
    
    public Object getElement()
    {
      return DomLoader.this.dom.getElement(this.result);
    }
    
    private void declarePrefixes(UnmarshallingContext context, String[] prefixes)
      throws SAXException
    {
      for (int i = prefixes.length - 1; i >= 0; i--)
      {
        String nsUri = context.getNamespaceURI(prefixes[i]);
        if (nsUri == null) {
          throw new IllegalStateException("prefix '" + prefixes[i] + "' isn't bound");
        }
        this.handler.startPrefixMapping(prefixes[i], nsUri);
      }
    }
    
    private void undeclarePrefixes(String[] prefixes)
      throws SAXException
    {
      for (int i = prefixes.length - 1; i >= 0; i--) {
        this.handler.endPrefixMapping(prefixes[i]);
      }
    }
  }
  
  public DomLoader(DomHandler<?, ResultT> dom)
  {
    super(true);
    this.dom = dom;
  }
  
  public void startElement(UnmarshallingContext.State state, TagName ea)
    throws SAXException
  {
    UnmarshallingContext context = state.getContext();
    if (state.target == null) {
      state.target = new State(context);
    }
    DomLoader<ResultT>.State s = (State)state.target;
    try
    {
      s.declarePrefixes(context, context.getNewlyDeclaredPrefixes());
      s.handler.startElement(ea.uri, ea.local, ea.getQname(), ea.atts);
    }
    catch (SAXException e)
    {
      context.handleError(e);
      throw e;
    }
  }
  
  public void childElement(UnmarshallingContext.State state, TagName ea)
    throws SAXException
  {
    state.loader = this;
    DomLoader<ResultT>.State s = (State)state.prev.target;
    s.depth += 1;
    state.target = s;
  }
  
  public void text(UnmarshallingContext.State state, CharSequence text)
    throws SAXException
  {
    if (text.length() == 0) {
      return;
    }
    try
    {
      DomLoader<ResultT>.State s = (State)state.target;
      s.handler.characters(text.toString().toCharArray(), 0, text.length());
    }
    catch (SAXException e)
    {
      state.getContext().handleError(e);
      throw e;
    }
  }
  
  public void leaveElement(UnmarshallingContext.State state, TagName ea)
    throws SAXException
  {
    DomLoader<ResultT>.State s = (State)state.target;
    UnmarshallingContext context = state.getContext();
    try
    {
      s.handler.endElement(ea.uri, ea.local, ea.getQname());
      s.undeclarePrefixes(context.getNewlyDeclaredPrefixes());
    }
    catch (SAXException e)
    {
      context.handleError(e);
      throw e;
    }
    if (--s.depth == 0)
    {
      try
      {
        s.undeclarePrefixes(context.getAllDeclaredPrefixes());
        s.handler.endDocument();
      }
      catch (SAXException e)
      {
        context.handleError(e);
        throw e;
      }
      state.target = s.getElement();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\unmarshaller\DomLoader.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */