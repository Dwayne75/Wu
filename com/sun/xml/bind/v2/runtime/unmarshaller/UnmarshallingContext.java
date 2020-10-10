package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.istack.SAXParseException2;
import com.sun.xml.bind.IDResolver;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.api.ClassResolver;
import com.sun.xml.bind.unmarshaller.InfosetScanner;
import com.sun.xml.bind.v2.ClassFactory;
import com.sun.xml.bind.v2.runtime.AssociationMap;
import com.sun.xml.bind.v2.runtime.Coordinator;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;
import javax.xml.bind.helpers.ValidationEventImpl;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.LocatorImpl;

public final class UnmarshallingContext
  extends Coordinator
  implements NamespaceContext, ValidationEventHandler, ErrorHandler, XmlVisitor, XmlVisitor.TextPredictor
{
  private final State root;
  private State current;
  private static final LocatorEx DUMMY_INSTANCE;
  
  static
  {
    LocatorImpl loc = new LocatorImpl();
    loc.setPublicId(null);
    loc.setSystemId(null);
    loc.setLineNumber(-1);
    loc.setColumnNumber(-1);
    DUMMY_INSTANCE = new LocatorExWrapper(loc);
  }
  
  @NotNull
  private LocatorEx locator = DUMMY_INSTANCE;
  private Object result;
  private JaxBeanInfo expectedType;
  private IDResolver idResolver;
  private boolean isUnmarshalInProgress = true;
  private boolean aborted = false;
  public final UnmarshallerImpl parent;
  private final AssociationMap assoc;
  private boolean isInplaceMode;
  private InfosetScanner scanner;
  private Object currentElement;
  private NamespaceContext environmentNamespaceContext;
  @Nullable
  public ClassResolver classResolver;
  private final Map<Class, Factory> factories;
  private Patcher[] patchers;
  private int patchersLen;
  private String[] nsBind;
  private int nsLen;
  private Scope[] scopes;
  private int scopeTop;
  
  public final class State
  {
    public Loader loader;
    public Receiver receiver;
    public Intercepter intercepter;
    public Object target;
    public Object backup;
    private int numNsDecl;
    public String elementDefaultValue;
    public final State prev;
    private State next;
    
    public UnmarshallingContext getContext()
    {
      return UnmarshallingContext.this;
    }
    
    private State(State prev)
    {
      this.prev = prev;
      if (prev != null) {
        prev.next = this;
      }
    }
    
    private void push()
    {
      if (this.next == null) {
        UnmarshallingContext.this.allocateMoreStates();
      }
      State n = this.next;
      n.numNsDecl = UnmarshallingContext.this.nsLen;
      UnmarshallingContext.this.current = n;
    }
    
    private void pop()
    {
      assert (this.prev != null);
      this.loader = null;
      this.receiver = null;
      this.intercepter = null;
      this.elementDefaultValue = null;
      this.target = null;
      UnmarshallingContext.this.current = this.prev;
    }
  }
  
  private static class Factory
  {
    private final Object factorInstance;
    private final Method method;
    
    public Factory(Object factorInstance, Method method)
    {
      this.factorInstance = factorInstance;
      this.method = method;
    }
    
    public Object createInstance()
      throws SAXException
    {
      try
      {
        return this.method.invoke(this.factorInstance, new Object[0]);
      }
      catch (IllegalAccessException e)
      {
        UnmarshallingContext.getInstance().handleError(e, false);
      }
      catch (InvocationTargetException e)
      {
        UnmarshallingContext.getInstance().handleError(e, false);
      }
      return null;
    }
  }
  
  public UnmarshallingContext(UnmarshallerImpl _parent, AssociationMap assoc)
  {
    this.factories = new HashMap();
    
    this.patchers = null;
    this.patchersLen = 0;
    
    this.nsBind = new String[16];
    this.nsLen = 0;
    
    this.scopes = new Scope[16];
    
    this.scopeTop = 0;
    for (int i = 0; i < this.scopes.length; i++) {
      this.scopes[i] = new Scope(this);
    }
    this.parent = _parent;
    this.assoc = assoc;
    this.root = (this.current = new State(null, null));
    allocateMoreStates();
  }
  
  public void reset(InfosetScanner scanner, boolean isInplaceMode, JaxBeanInfo expectedType, IDResolver idResolver)
  {
    this.scanner = scanner;
    this.isInplaceMode = isInplaceMode;
    this.expectedType = expectedType;
    this.idResolver = idResolver;
  }
  
  public JAXBContextImpl getJAXBContext()
  {
    return this.parent.context;
  }
  
  public State getCurrentState()
  {
    return this.current;
  }
  
  public Loader selectRootLoader(State state, TagName tag)
    throws SAXException
  {
    try
    {
      Loader l = getJAXBContext().selectRootLoader(state, tag);
      if (l != null) {
        return l;
      }
      if (this.classResolver != null)
      {
        Class<?> clazz = this.classResolver.resolveElementName(tag.uri, tag.local);
        if (clazz != null)
        {
          JAXBContextImpl enhanced = getJAXBContext().createAugmented(clazz);
          JaxBeanInfo<?> bi = enhanced.getBeanInfo(clazz);
          return bi.getLoader(enhanced, true);
        }
      }
    }
    catch (RuntimeException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      handleError(e);
    }
    return null;
  }
  
  private void allocateMoreStates()
  {
    assert (this.current.next == null);
    
    State s = this.current;
    for (int i = 0; i < 8; i++) {
      s = new State(s, null);
    }
  }
  
  public void setFactories(Object factoryInstances)
  {
    this.factories.clear();
    if (factoryInstances == null) {
      return;
    }
    if ((factoryInstances instanceof Object[])) {
      for (Object factory : (Object[])factoryInstances) {
        addFactory(factory);
      }
    } else {
      addFactory(factoryInstances);
    }
  }
  
  private void addFactory(Object factory)
  {
    for (Method m : factory.getClass().getMethods()) {
      if (m.getName().startsWith("create")) {
        if (m.getParameterTypes().length <= 0)
        {
          Class type = m.getReturnType();
          
          this.factories.put(type, new Factory(factory, m));
        }
      }
    }
  }
  
  public void startDocument(LocatorEx locator, NamespaceContext nsContext)
    throws SAXException
  {
    if (locator != null) {
      this.locator = locator;
    }
    this.environmentNamespaceContext = nsContext;
    
    this.result = null;
    this.current = this.root;
    
    this.patchersLen = 0;
    this.aborted = false;
    this.isUnmarshalInProgress = true;
    this.nsLen = 0;
    
    setThreadAffinity();
    if (this.expectedType != null) {
      this.root.loader = EXPECTED_TYPE_ROOT_LOADER;
    } else {
      this.root.loader = DEFAULT_ROOT_LOADER;
    }
    this.idResolver.startDocument(this);
  }
  
  public void startElement(TagName tagName)
    throws SAXException
  {
    pushCoordinator();
    try
    {
      _startElement(tagName);
    }
    finally
    {
      popCoordinator();
    }
  }
  
  private void _startElement(TagName tagName)
    throws SAXException
  {
    if (this.assoc != null) {
      this.currentElement = this.scanner.getCurrentElement();
    }
    Loader h = this.current.loader;
    this.current.push();
    
    h.childElement(this.current, tagName);
    assert (this.current.loader != null);
    
    this.current.loader.startElement(this.current, tagName);
  }
  
  public void text(CharSequence pcdata)
    throws SAXException
  {
    State cur = this.current;
    pushCoordinator();
    try
    {
      if ((cur.elementDefaultValue != null) && 
        (pcdata.length() == 0)) {
        pcdata = cur.elementDefaultValue;
      }
      cur.loader.text(cur, pcdata);
    }
    finally
    {
      popCoordinator();
    }
  }
  
  public final void endElement(TagName tagName)
    throws SAXException
  {
    pushCoordinator();
    try
    {
      State child = this.current;
      
      child.loader.leaveElement(child, tagName);
      
      Object target = child.target;
      Receiver recv = child.receiver;
      Intercepter intercepter = child.intercepter;
      child.pop();
      if (intercepter != null) {
        target = intercepter.intercept(this.current, target);
      }
      if (recv != null) {
        recv.receive(this.current, target);
      }
    }
    finally
    {
      popCoordinator();
    }
  }
  
  public void endDocument()
    throws SAXException
  {
    runPatchers();
    this.idResolver.endDocument();
    
    this.isUnmarshalInProgress = false;
    this.currentElement = null;
    this.locator = DUMMY_INSTANCE;
    this.environmentNamespaceContext = null;
    
    assert (this.root == this.current);
    
    resetThreadAffinity();
  }
  
  @Deprecated
  public boolean expectText()
  {
    return this.current.loader.expectText;
  }
  
  @Deprecated
  public XmlVisitor.TextPredictor getPredictor()
  {
    return this;
  }
  
  public UnmarshallingContext getContext()
  {
    return this;
  }
  
  public Object getResult()
    throws UnmarshalException
  {
    if (this.isUnmarshalInProgress) {
      throw new IllegalStateException();
    }
    if (!this.aborted) {
      return this.result;
    }
    throw new UnmarshalException((String)null);
  }
  
  public Object createInstance(Class<?> clazz)
    throws SAXException
  {
    if (!this.factories.isEmpty())
    {
      Factory factory = (Factory)this.factories.get(clazz);
      if (factory != null) {
        return factory.createInstance();
      }
    }
    return ClassFactory.create(clazz);
  }
  
  public Object createInstance(JaxBeanInfo beanInfo)
    throws SAXException
  {
    if (!this.factories.isEmpty())
    {
      Factory factory = (Factory)this.factories.get(beanInfo.jaxbType);
      if (factory != null) {
        return factory.createInstance();
      }
    }
    try
    {
      return beanInfo.createInstance(this);
    }
    catch (IllegalAccessException e)
    {
      Loader.reportError("Unable to create an instance of " + beanInfo.jaxbType.getName(), e, false);
    }
    catch (InvocationTargetException e)
    {
      Loader.reportError("Unable to create an instance of " + beanInfo.jaxbType.getName(), e, false);
    }
    catch (InstantiationException e)
    {
      Loader.reportError("Unable to create an instance of " + beanInfo.jaxbType.getName(), e, false);
    }
    return null;
  }
  
  public void handleEvent(ValidationEvent event, boolean canRecover)
    throws SAXException
  {
    ValidationEventHandler eventHandler = this.parent.getEventHandler();
    
    boolean recover = eventHandler.handleEvent(event);
    if (!recover) {
      this.aborted = true;
    }
    if ((!canRecover) || (!recover)) {
      throw new SAXParseException2(event.getMessage(), this.locator, new UnmarshalException(event.getMessage(), event.getLinkedException()));
    }
  }
  
  public boolean handleEvent(ValidationEvent event)
  {
    try
    {
      boolean recover = this.parent.getEventHandler().handleEvent(event);
      if (!recover) {
        this.aborted = true;
      }
      return recover;
    }
    catch (RuntimeException re) {}
    return false;
  }
  
  public void handleError(Exception e)
    throws SAXException
  {
    handleError(e, true);
  }
  
  public void handleError(Exception e, boolean canRecover)
    throws SAXException
  {
    handleEvent(new ValidationEventImpl(1, e.getMessage(), this.locator.getLocation(), e), canRecover);
  }
  
  public void handleError(String msg)
  {
    handleEvent(new ValidationEventImpl(1, msg, this.locator.getLocation()));
  }
  
  protected ValidationEventLocator getLocation()
  {
    return this.locator.getLocation();
  }
  
  public LocatorEx getLocator()
  {
    return this.locator;
  }
  
  public void errorUnresolvedIDREF(Object bean, String idref, LocatorEx loc)
    throws SAXException
  {
    handleEvent(new ValidationEventImpl(1, Messages.UNRESOLVED_IDREF.format(new Object[] { idref }), loc.getLocation()), true);
  }
  
  public void addPatcher(Patcher job)
  {
    if (this.patchers == null) {
      this.patchers = new Patcher[32];
    }
    if (this.patchers.length == this.patchersLen)
    {
      Patcher[] buf = new Patcher[this.patchersLen * 2];
      System.arraycopy(this.patchers, 0, buf, 0, this.patchersLen);
      this.patchers = buf;
    }
    this.patchers[(this.patchersLen++)] = job;
  }
  
  private void runPatchers()
    throws SAXException
  {
    if (this.patchers != null) {
      for (int i = 0; i < this.patchersLen; i++)
      {
        this.patchers[i].run();
        this.patchers[i] = null;
      }
    }
  }
  
  public String addToIdTable(String id)
    throws SAXException
  {
    Object o = this.current.target;
    if (o == null) {
      o = this.current.prev.target;
    }
    this.idResolver.bind(id, o);
    return id;
  }
  
  public Callable getObjectFromId(String id, Class targetType)
    throws SAXException
  {
    return this.idResolver.resolve(id, targetType);
  }
  
  public void startPrefixMapping(String prefix, String uri)
  {
    if (this.nsBind.length == this.nsLen)
    {
      String[] n = new String[this.nsLen * 2];
      System.arraycopy(this.nsBind, 0, n, 0, this.nsLen);
      this.nsBind = n;
    }
    this.nsBind[(this.nsLen++)] = prefix;
    this.nsBind[(this.nsLen++)] = uri;
  }
  
  public void endPrefixMapping(String prefix)
  {
    this.nsLen -= 2;
  }
  
  private String resolveNamespacePrefix(String prefix)
  {
    if (prefix.equals("xml")) {
      return "http://www.w3.org/XML/1998/namespace";
    }
    for (int i = this.nsLen - 2; i >= 0; i -= 2) {
      if (prefix.equals(this.nsBind[i])) {
        return this.nsBind[(i + 1)];
      }
    }
    if (this.environmentNamespaceContext != null) {
      return this.environmentNamespaceContext.getNamespaceURI(prefix.intern());
    }
    if (prefix.equals("")) {
      return "";
    }
    return null;
  }
  
  public String[] getNewlyDeclaredPrefixes()
  {
    return getPrefixList(this.current.prev.numNsDecl);
  }
  
  public String[] getAllDeclaredPrefixes()
  {
    return getPrefixList(0);
  }
  
  private String[] getPrefixList(int startIndex)
  {
    int size = (this.current.numNsDecl - startIndex) / 2;
    String[] r = new String[size];
    for (int i = 0; i < r.length; i++) {
      r[i] = this.nsBind[(startIndex + i * 2)];
    }
    return r;
  }
  
  public Iterator<String> getPrefixes(String uri)
  {
    return Collections.unmodifiableList(getAllPrefixesInList(uri)).iterator();
  }
  
  private List<String> getAllPrefixesInList(String uri)
  {
    List<String> a = new ArrayList();
    if (uri == null) {
      throw new IllegalArgumentException();
    }
    if (uri.equals("http://www.w3.org/XML/1998/namespace"))
    {
      a.add("xml");
      return a;
    }
    if (uri.equals("http://www.w3.org/2000/xmlns/"))
    {
      a.add("xmlns");
      return a;
    }
    for (int i = this.nsLen - 2; i >= 0; i -= 2) {
      if ((uri.equals(this.nsBind[(i + 1)])) && 
        (getNamespaceURI(this.nsBind[i]).equals(this.nsBind[(i + 1)]))) {
        a.add(this.nsBind[i]);
      }
    }
    return a;
  }
  
  public String getPrefix(String uri)
  {
    if (uri == null) {
      throw new IllegalArgumentException();
    }
    if (uri.equals("http://www.w3.org/XML/1998/namespace")) {
      return "xml";
    }
    if (uri.equals("http://www.w3.org/2000/xmlns/")) {
      return "xmlns";
    }
    for (int i = this.nsLen - 2; i >= 0; i -= 2) {
      if ((uri.equals(this.nsBind[(i + 1)])) && 
        (getNamespaceURI(this.nsBind[i]).equals(this.nsBind[(i + 1)]))) {
        return this.nsBind[i];
      }
    }
    if (this.environmentNamespaceContext != null) {
      return this.environmentNamespaceContext.getPrefix(uri);
    }
    return null;
  }
  
  public String getNamespaceURI(String prefix)
  {
    if (prefix == null) {
      throw new IllegalArgumentException();
    }
    if (prefix.equals("xmlns")) {
      return "http://www.w3.org/2000/xmlns/";
    }
    return resolveNamespacePrefix(prefix);
  }
  
  public void startScope(int frameSize)
  {
    this.scopeTop += frameSize;
    if (this.scopeTop >= this.scopes.length)
    {
      Scope[] s = new Scope[Math.max(this.scopeTop + 1, this.scopes.length * 2)];
      System.arraycopy(this.scopes, 0, s, 0, this.scopes.length);
      for (int i = this.scopes.length; i < s.length; i++) {
        s[i] = new Scope(this);
      }
      this.scopes = s;
    }
  }
  
  public void endScope(int frameSize)
    throws SAXException
  {
    try
    {
      for (; frameSize > 0; this.scopeTop -= 1)
      {
        this.scopes[this.scopeTop].finish();frameSize--;
      }
    }
    catch (AccessorException e)
    {
      handleError(e);
      for (; frameSize > 0; frameSize--) {
        this.scopes[(this.scopeTop--)] = new Scope(this);
      }
    }
  }
  
  public Scope getScope(int offset)
  {
    return this.scopes[(this.scopeTop - offset)];
  }
  
  private static final Loader DEFAULT_ROOT_LOADER = new DefaultRootLoader(null);
  private static final Loader EXPECTED_TYPE_ROOT_LOADER = new ExpectedTypeRootLoader(null);
  
  private static final class DefaultRootLoader
    extends Loader
    implements Receiver
  {
    public void childElement(UnmarshallingContext.State state, TagName ea)
      throws SAXException
    {
      Loader loader = state.getContext().selectRootLoader(state, ea);
      if (loader != null)
      {
        state.loader = loader;
        state.receiver = this;
        return;
      }
      JaxBeanInfo beanInfo = XsiTypeLoader.parseXsiType(state, ea, null);
      if (beanInfo == null)
      {
        reportUnexpectedChildElement(ea, false);
        return;
      }
      state.loader = beanInfo.getLoader(null, false);
      state.prev.backup = new JAXBElement(ea.createQName(), Object.class, null);
      state.receiver = this;
    }
    
    public Collection<QName> getExpectedChildElements()
    {
      return UnmarshallingContext.getInstance().getJAXBContext().getValidRootNames();
    }
    
    public void receive(UnmarshallingContext.State state, Object o)
    {
      if (state.backup != null)
      {
        ((JAXBElement)state.backup).setValue(o);
        o = state.backup;
      }
      state.getContext().result = o;
    }
  }
  
  private static final class ExpectedTypeRootLoader
    extends Loader
    implements Receiver
  {
    public void childElement(UnmarshallingContext.State state, TagName ea)
    {
      UnmarshallingContext context = state.getContext();
      
      QName qn = new QName(ea.uri, ea.local);
      state.prev.target = new JAXBElement(qn, context.expectedType.jaxbType, null, null);
      state.receiver = this;
      
      state.loader = new XsiNilLoader(context.expectedType.getLoader(null, true));
    }
    
    public void receive(UnmarshallingContext.State state, Object o)
    {
      JAXBElement e = (JAXBElement)state.target;
      e.setValue(o);
      state.getContext().recordOuterPeer(e);
      state.getContext().result = e;
    }
  }
  
  public void recordInnerPeer(Object innerPeer)
  {
    if (this.assoc != null) {
      this.assoc.addInner(this.currentElement, innerPeer);
    }
  }
  
  public Object getInnerPeer()
  {
    if ((this.assoc != null) && (this.isInplaceMode)) {
      return this.assoc.getInnerPeer(this.currentElement);
    }
    return null;
  }
  
  public void recordOuterPeer(Object outerPeer)
  {
    if (this.assoc != null) {
      this.assoc.addOuter(this.currentElement, outerPeer);
    }
  }
  
  public Object getOuterPeer()
  {
    if ((this.assoc != null) && (this.isInplaceMode)) {
      return this.assoc.getOuterPeer(this.currentElement);
    }
    return null;
  }
  
  public String getXMIMEContentType()
  {
    Object t = this.current.target;
    if (t == null) {
      return null;
    }
    return getJAXBContext().getXMIMEContentType(t);
  }
  
  public static UnmarshallingContext getInstance()
  {
    return (UnmarshallingContext)Coordinator._getInstance();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\unmarshaller\UnmarshallingContext.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */