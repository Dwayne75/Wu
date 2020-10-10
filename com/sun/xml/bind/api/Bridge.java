package com.sun.xml.bind.api;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.istack.Pool;
import com.sun.xml.bind.v2.runtime.BridgeContextImpl;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

public abstract class Bridge<T>
{
  protected final JAXBContextImpl context;
  
  protected Bridge(JAXBContextImpl context)
  {
    this.context = context;
  }
  
  @NotNull
  public JAXBRIContext getContext()
  {
    return this.context;
  }
  
  public final void marshal(T object, XMLStreamWriter output)
    throws JAXBException
  {
    marshal(object, output, null);
  }
  
  public final void marshal(T object, XMLStreamWriter output, AttachmentMarshaller am)
    throws JAXBException
  {
    Marshaller m = (Marshaller)this.context.marshallerPool.take();
    m.setAttachmentMarshaller(am);
    marshal(m, object, output);
    m.setAttachmentMarshaller(null);
    this.context.marshallerPool.recycle(m);
  }
  
  public final void marshal(@NotNull BridgeContext context, T object, XMLStreamWriter output)
    throws JAXBException
  {
    marshal(((BridgeContextImpl)context).marshaller, object, output);
  }
  
  public abstract void marshal(@NotNull Marshaller paramMarshaller, T paramT, XMLStreamWriter paramXMLStreamWriter)
    throws JAXBException;
  
  public void marshal(T object, OutputStream output, NamespaceContext nsContext)
    throws JAXBException
  {
    marshal(object, output, nsContext, null);
  }
  
  public void marshal(T object, OutputStream output, NamespaceContext nsContext, AttachmentMarshaller am)
    throws JAXBException
  {
    Marshaller m = (Marshaller)this.context.marshallerPool.take();
    m.setAttachmentMarshaller(am);
    marshal(m, object, output, nsContext);
    m.setAttachmentMarshaller(null);
    this.context.marshallerPool.recycle(m);
  }
  
  public final void marshal(@NotNull BridgeContext context, T object, OutputStream output, NamespaceContext nsContext)
    throws JAXBException
  {
    marshal(((BridgeContextImpl)context).marshaller, object, output, nsContext);
  }
  
  public abstract void marshal(@NotNull Marshaller paramMarshaller, T paramT, OutputStream paramOutputStream, NamespaceContext paramNamespaceContext)
    throws JAXBException;
  
  public final void marshal(T object, Node output)
    throws JAXBException
  {
    Marshaller m = (Marshaller)this.context.marshallerPool.take();
    marshal(m, object, output);
    this.context.marshallerPool.recycle(m);
  }
  
  public final void marshal(@NotNull BridgeContext context, T object, Node output)
    throws JAXBException
  {
    marshal(((BridgeContextImpl)context).marshaller, object, output);
  }
  
  public abstract void marshal(@NotNull Marshaller paramMarshaller, T paramT, Node paramNode)
    throws JAXBException;
  
  public final void marshal(T object, ContentHandler contentHandler)
    throws JAXBException
  {
    marshal(object, contentHandler, null);
  }
  
  public final void marshal(T object, ContentHandler contentHandler, AttachmentMarshaller am)
    throws JAXBException
  {
    Marshaller m = (Marshaller)this.context.marshallerPool.take();
    m.setAttachmentMarshaller(am);
    marshal(m, object, contentHandler);
    m.setAttachmentMarshaller(null);
    this.context.marshallerPool.recycle(m);
  }
  
  public final void marshal(@NotNull BridgeContext context, T object, ContentHandler contentHandler)
    throws JAXBException
  {
    marshal(((BridgeContextImpl)context).marshaller, object, contentHandler);
  }
  
  public abstract void marshal(@NotNull Marshaller paramMarshaller, T paramT, ContentHandler paramContentHandler)
    throws JAXBException;
  
  public final void marshal(T object, Result result)
    throws JAXBException
  {
    Marshaller m = (Marshaller)this.context.marshallerPool.take();
    marshal(m, object, result);
    this.context.marshallerPool.recycle(m);
  }
  
  public final void marshal(@NotNull BridgeContext context, T object, Result result)
    throws JAXBException
  {
    marshal(((BridgeContextImpl)context).marshaller, object, result);
  }
  
  public abstract void marshal(@NotNull Marshaller paramMarshaller, T paramT, Result paramResult)
    throws JAXBException;
  
  private T exit(T r, Unmarshaller u)
  {
    u.setAttachmentUnmarshaller(null);
    this.context.unmarshallerPool.recycle(u);
    return r;
  }
  
  @NotNull
  public final T unmarshal(@NotNull XMLStreamReader in)
    throws JAXBException
  {
    return (T)unmarshal(in, null);
  }
  
  @NotNull
  public final T unmarshal(@NotNull XMLStreamReader in, @Nullable AttachmentUnmarshaller au)
    throws JAXBException
  {
    Unmarshaller u = (Unmarshaller)this.context.unmarshallerPool.take();
    u.setAttachmentUnmarshaller(au);
    return (T)exit(unmarshal(u, in), u);
  }
  
  @NotNull
  public final T unmarshal(@NotNull BridgeContext context, @NotNull XMLStreamReader in)
    throws JAXBException
  {
    return (T)unmarshal(((BridgeContextImpl)context).unmarshaller, in);
  }
  
  @NotNull
  public abstract T unmarshal(@NotNull Unmarshaller paramUnmarshaller, @NotNull XMLStreamReader paramXMLStreamReader)
    throws JAXBException;
  
  @NotNull
  public final T unmarshal(@NotNull Source in)
    throws JAXBException
  {
    return (T)unmarshal(in, null);
  }
  
  @NotNull
  public final T unmarshal(@NotNull Source in, @Nullable AttachmentUnmarshaller au)
    throws JAXBException
  {
    Unmarshaller u = (Unmarshaller)this.context.unmarshallerPool.take();
    u.setAttachmentUnmarshaller(au);
    return (T)exit(unmarshal(u, in), u);
  }
  
  @NotNull
  public final T unmarshal(@NotNull BridgeContext context, @NotNull Source in)
    throws JAXBException
  {
    return (T)unmarshal(((BridgeContextImpl)context).unmarshaller, in);
  }
  
  @NotNull
  public abstract T unmarshal(@NotNull Unmarshaller paramUnmarshaller, @NotNull Source paramSource)
    throws JAXBException;
  
  @NotNull
  public final T unmarshal(@NotNull InputStream in)
    throws JAXBException
  {
    Unmarshaller u = (Unmarshaller)this.context.unmarshallerPool.take();
    return (T)exit(unmarshal(u, in), u);
  }
  
  @NotNull
  public final T unmarshal(@NotNull BridgeContext context, @NotNull InputStream in)
    throws JAXBException
  {
    return (T)unmarshal(((BridgeContextImpl)context).unmarshaller, in);
  }
  
  @NotNull
  public abstract T unmarshal(@NotNull Unmarshaller paramUnmarshaller, @NotNull InputStream paramInputStream)
    throws JAXBException;
  
  @NotNull
  public final T unmarshal(@NotNull Node n)
    throws JAXBException
  {
    return (T)unmarshal(n, null);
  }
  
  @NotNull
  public final T unmarshal(@NotNull Node n, @Nullable AttachmentUnmarshaller au)
    throws JAXBException
  {
    Unmarshaller u = (Unmarshaller)this.context.unmarshallerPool.take();
    u.setAttachmentUnmarshaller(au);
    return (T)exit(unmarshal(u, n), u);
  }
  
  @NotNull
  public final T unmarshal(@NotNull BridgeContext context, @NotNull Node n)
    throws JAXBException
  {
    return (T)unmarshal(((BridgeContextImpl)context).unmarshaller, n);
  }
  
  @NotNull
  public abstract T unmarshal(@NotNull Unmarshaller paramUnmarshaller, @NotNull Node paramNode)
    throws JAXBException;
  
  public abstract TypeReference getTypeReference();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\api\Bridge.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */