package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import java.util.Collection;
import java.util.Collections;
import javax.xml.bind.Unmarshaller.Listener;
import javax.xml.bind.helpers.ValidationEventImpl;
import javax.xml.namespace.QName;
import org.xml.sax.SAXException;

public abstract class Loader
{
  protected boolean expectText;
  
  protected Loader(boolean expectText)
  {
    this.expectText = expectText;
  }
  
  protected Loader() {}
  
  public void startElement(UnmarshallingContext.State state, TagName ea)
    throws SAXException
  {}
  
  public void childElement(UnmarshallingContext.State state, TagName ea)
    throws SAXException
  {
    reportUnexpectedChildElement(ea, true);
    state.loader = Discarder.INSTANCE;
    state.receiver = null;
  }
  
  protected final void reportUnexpectedChildElement(TagName ea, boolean canRecover)
    throws SAXException
  {
    if ((canRecover) && (!UnmarshallingContext.getInstance().parent.hasEventHandler())) {
      return;
    }
    if ((ea.uri != ea.uri.intern()) || (ea.local != ea.local.intern())) {
      reportError(Messages.UNINTERNED_STRINGS.format(new Object[0]), canRecover);
    } else {
      reportError(Messages.UNEXPECTED_ELEMENT.format(new Object[] { ea.uri, ea.local, computeExpectedElements() }), canRecover);
    }
  }
  
  public Collection<QName> getExpectedChildElements()
  {
    return Collections.emptyList();
  }
  
  public void text(UnmarshallingContext.State state, CharSequence text)
    throws SAXException
  {
    text = text.toString().replace('\r', ' ').replace('\n', ' ').replace('\t', ' ').trim();
    reportError(Messages.UNEXPECTED_TEXT.format(new Object[] { text }), true);
  }
  
  public final boolean expectText()
  {
    return this.expectText;
  }
  
  public void leaveElement(UnmarshallingContext.State state, TagName ea)
    throws SAXException
  {}
  
  private String computeExpectedElements()
  {
    StringBuilder r = new StringBuilder();
    for (QName n : getExpectedChildElements())
    {
      if (r.length() != 0) {
        r.append(',');
      }
      r.append("<{").append(n.getNamespaceURI()).append('}').append(n.getLocalPart()).append('>');
    }
    if (r.length() == 0) {
      return "(none)";
    }
    return r.toString();
  }
  
  protected final void fireBeforeUnmarshal(JaxBeanInfo beanInfo, Object child, UnmarshallingContext.State state)
    throws SAXException
  {
    if (beanInfo.lookForLifecycleMethods())
    {
      UnmarshallingContext context = state.getContext();
      Unmarshaller.Listener listener = context.parent.getListener();
      if (beanInfo.hasBeforeUnmarshalMethod()) {
        beanInfo.invokeBeforeUnmarshalMethod(context.parent, child, state.prev.target);
      }
      if (listener != null) {
        listener.beforeUnmarshal(child, state.prev.target);
      }
    }
  }
  
  protected final void fireAfterUnmarshal(JaxBeanInfo beanInfo, Object child, UnmarshallingContext.State state)
    throws SAXException
  {
    if (beanInfo.lookForLifecycleMethods())
    {
      UnmarshallingContext context = state.getContext();
      Unmarshaller.Listener listener = context.parent.getListener();
      if (beanInfo.hasAfterUnmarshalMethod()) {
        beanInfo.invokeAfterUnmarshalMethod(context.parent, child, state.target);
      }
      if (listener != null) {
        listener.afterUnmarshal(child, state.target);
      }
    }
  }
  
  protected static void handleGenericException(Exception e)
    throws SAXException
  {
    handleGenericException(e, false);
  }
  
  public static void handleGenericException(Exception e, boolean canRecover)
    throws SAXException
  {
    reportError(e.getMessage(), e, canRecover);
  }
  
  protected static void reportError(String msg, boolean canRecover)
    throws SAXException
  {
    reportError(msg, null, canRecover);
  }
  
  public static void reportError(String msg, Exception nested, boolean canRecover)
    throws SAXException
  {
    UnmarshallingContext context = UnmarshallingContext.getInstance();
    context.handleEvent(new ValidationEventImpl(canRecover ? 1 : 2, msg, context.getLocator().getLocation(), nested), canRecover);
  }
  
  protected static void handleParseConversionException(UnmarshallingContext.State state, Exception e)
    throws SAXException
  {
    state.getContext().handleError(e);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\unmarshaller\Loader.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */