package com.sun.xml.xsom.impl.util;

import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSListSimpleType;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.XSUnionSimpleType;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.XSXPath;
import com.sun.xml.xsom.visitor.XSSimpleTypeVisitor;
import com.sun.xml.xsom.visitor.XSTermVisitor;
import com.sun.xml.xsom.visitor.XSVisitor;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import org.xml.sax.Locator;

public class SchemaTreeTraverser
  implements XSVisitor, XSSimpleTypeVisitor
{
  private SchemaTreeModel model;
  private SchemaTreeNode currNode;
  
  public static final class SchemaTreeModel
    extends DefaultTreeModel
  {
    private SchemaTreeModel(SchemaTreeTraverser.SchemaRootNode root)
    {
      super();
    }
    
    public static SchemaTreeModel getInstance()
    {
      SchemaTreeTraverser.SchemaRootNode root = new SchemaTreeTraverser.SchemaRootNode();
      return new SchemaTreeModel(root);
    }
    
    public void addSchemaNode(SchemaTreeTraverser.SchemaTreeNode node)
    {
      ((SchemaTreeTraverser.SchemaRootNode)this.root).add(node);
    }
  }
  
  public static class SchemaTreeNode
    extends DefaultMutableTreeNode
  {
    private String fileName;
    private int lineNumber;
    private String artifactName;
    
    public SchemaTreeNode(String artifactName, Locator locator)
    {
      this.artifactName = artifactName;
      if (locator == null)
      {
        this.fileName = null;
      }
      else
      {
        String filename = locator.getSystemId();
        filename = filename.replaceAll("%20", " ");
        if (filename.startsWith("file:/")) {
          filename = filename.substring(6);
        }
        this.fileName = filename;
        this.lineNumber = (locator.getLineNumber() - 1);
      }
    }
    
    public String getCaption()
    {
      return this.artifactName;
    }
    
    public String getFileName()
    {
      return this.fileName;
    }
    
    public void setFileName(String fileName)
    {
      this.fileName = fileName;
    }
    
    public int getLineNumber()
    {
      return this.lineNumber;
    }
    
    public void setLineNumber(int lineNumber)
    {
      this.lineNumber = lineNumber;
    }
  }
  
  public static class SchemaRootNode
    extends SchemaTreeTraverser.SchemaTreeNode
  {
    public SchemaRootNode()
    {
      super(null);
    }
  }
  
  public static class SchemaTreeCellRenderer
    extends JPanel
    implements TreeCellRenderer
  {
    protected final JLabel iconLabel;
    protected final JLabel nameLabel;
    private boolean isSelected;
    public final Color selectedBackground = new Color(255, 244, 232);
    public final Color selectedForeground = new Color(64, 32, 0);
    public final Font nameFont = new Font("Arial", 1, 12);
    
    public SchemaTreeCellRenderer()
    {
      FlowLayout fl = new FlowLayout(0, 1, 1);
      setLayout(fl);
      this.iconLabel = new JLabel();
      this.iconLabel.setOpaque(false);
      this.iconLabel.setBorder(null);
      add(this.iconLabel);
      
      add(Box.createHorizontalStrut(5));
      
      this.nameLabel = new JLabel();
      this.nameLabel.setOpaque(false);
      this.nameLabel.setBorder(null);
      this.nameLabel.setFont(this.nameFont);
      add(this.nameLabel);
      
      this.isSelected = false;
      
      setOpaque(false);
      setBorder(null);
    }
    
    public final void paintComponent(Graphics g)
    {
      int width = getWidth();
      int height = getHeight();
      if (this.isSelected)
      {
        g.setColor(this.selectedBackground);
        g.fillRect(0, 0, width - 1, height - 1);
        g.setColor(this.selectedForeground);
        g.drawRect(0, 0, width - 1, height - 1);
      }
      super.paintComponent(g);
    }
    
    protected final void setValues(Icon icon, String caption, boolean selected)
    {
      this.iconLabel.setIcon(icon);
      this.nameLabel.setText(caption);
      
      this.isSelected = selected;
      if (selected) {
        this.nameLabel.setForeground(this.selectedForeground);
      } else {
        this.nameLabel.setForeground(Color.black);
      }
    }
    
    public final Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
    {
      if ((value instanceof SchemaTreeTraverser.SchemaTreeNode))
      {
        SchemaTreeTraverser.SchemaTreeNode stn = (SchemaTreeTraverser.SchemaTreeNode)value;
        
        setValues(null, stn.getCaption(), selected);
        return this;
      }
      throw new IllegalStateException("Unknown node");
    }
  }
  
  public SchemaTreeTraverser()
  {
    this.model = SchemaTreeModel.getInstance();
    this.currNode = ((SchemaTreeNode)this.model.getRoot());
  }
  
  public SchemaTreeModel getModel()
  {
    return this.model;
  }
  
  public void visit(XSSchemaSet s)
  {
    for (XSSchema schema : s.getSchemas()) {
      schema(schema);
    }
  }
  
  public void schema(XSSchema s)
  {
    if (s.getTargetNamespace().equals("http://www.w3.org/2001/XMLSchema")) {
      return;
    }
    SchemaTreeNode newNode = new SchemaTreeNode("Schema " + s.getLocator().getSystemId(), s.getLocator());
    
    this.currNode = newNode;
    this.model.addSchemaNode(newNode);
    for (XSAttGroupDecl groupDecl : s.getAttGroupDecls().values()) {
      attGroupDecl(groupDecl);
    }
    for (XSAttributeDecl attrDecl : s.getAttributeDecls().values()) {
      attributeDecl(attrDecl);
    }
    for (XSComplexType complexType : s.getComplexTypes().values()) {
      complexType(complexType);
    }
    for (XSElementDecl elementDecl : s.getElementDecls().values()) {
      elementDecl(elementDecl);
    }
    for (XSModelGroupDecl modelGroupDecl : s.getModelGroupDecls().values()) {
      modelGroupDecl(modelGroupDecl);
    }
    for (XSSimpleType simpleType : s.getSimpleTypes().values()) {
      simpleType(simpleType);
    }
  }
  
  public void attGroupDecl(XSAttGroupDecl decl)
  {
    SchemaTreeNode newNode = new SchemaTreeNode("Attribute group \"" + decl.getName() + "\"", decl.getLocator());
    
    this.currNode.add(newNode);
    this.currNode = newNode;
    
    Iterator itr = decl.iterateAttGroups();
    while (itr.hasNext()) {
      dumpRef((XSAttGroupDecl)itr.next());
    }
    itr = decl.iterateDeclaredAttributeUses();
    while (itr.hasNext()) {
      attributeUse((XSAttributeUse)itr.next());
    }
    this.currNode = ((SchemaTreeNode)this.currNode.getParent());
  }
  
  public void dumpRef(XSAttGroupDecl decl)
  {
    SchemaTreeNode newNode = new SchemaTreeNode("Attribute group ref \"{" + decl.getTargetNamespace() + "}" + decl.getName() + "\"", decl.getLocator());
    
    this.currNode.add(newNode);
  }
  
  public void attributeUse(XSAttributeUse use)
  {
    XSAttributeDecl decl = use.getDecl();
    
    String additionalAtts = "";
    if (use.isRequired()) {
      additionalAtts = additionalAtts + " use=\"required\"";
    }
    if ((use.getFixedValue() != null) && (use.getDecl().getFixedValue() == null)) {
      additionalAtts = additionalAtts + " fixed=\"" + use.getFixedValue() + "\"";
    }
    if ((use.getDefaultValue() != null) && (use.getDecl().getDefaultValue() == null)) {
      additionalAtts = additionalAtts + " default=\"" + use.getDefaultValue() + "\"";
    }
    if (decl.isLocal())
    {
      dump(decl, additionalAtts);
    }
    else
    {
      String str = MessageFormat.format("Attribute ref \"'{'{0}'}'{1}{2}\"", new Object[] { decl.getTargetNamespace(), decl.getName(), additionalAtts });
      
      SchemaTreeNode newNode = new SchemaTreeNode(str, decl.getLocator());
      this.currNode.add(newNode);
    }
  }
  
  public void attributeDecl(XSAttributeDecl decl)
  {
    dump(decl, "");
  }
  
  private void dump(XSAttributeDecl decl, String additionalAtts)
  {
    XSSimpleType type = decl.getType();
    
    String str = MessageFormat.format("Attribute \"{0}\"{1}{2}{3}{4}", new Object[] { decl.getName(), additionalAtts, type.isLocal() ? "" : MessageFormat.format(" type=\"'{'{0}'}'{1}\"", new Object[] { type.getTargetNamespace(), type.getName() }), " fixed=\"" + decl.getFixedValue() + "\"", " default=\"" + decl.getDefaultValue() + "\"" });
    
    SchemaTreeNode newNode = new SchemaTreeNode(str, decl.getLocator());
    this.currNode.add(newNode);
    this.currNode = newNode;
    if (type.isLocal()) {
      simpleType(type);
    }
    this.currNode = ((SchemaTreeNode)this.currNode.getParent());
  }
  
  public void simpleType(XSSimpleType type)
  {
    String str = MessageFormat.format("Simple type {0}", new Object[] { " name=\"" + type.getName() + "\"" });
    
    SchemaTreeNode newNode = new SchemaTreeNode(str, type.getLocator());
    this.currNode.add(newNode);
    this.currNode = newNode;
    
    type.visit(this);
    
    this.currNode = ((SchemaTreeNode)this.currNode.getParent());
  }
  
  public void listSimpleType(XSListSimpleType type)
  {
    XSSimpleType itemType = type.getItemType();
    if (itemType.isLocal())
    {
      SchemaTreeNode newNode = new SchemaTreeNode("List", type.getLocator());
      
      this.currNode.add(newNode);
      this.currNode = newNode;
      simpleType(itemType);
      this.currNode = ((SchemaTreeNode)this.currNode.getParent());
    }
    else
    {
      String str = MessageFormat.format("List itemType=\"'{'{0}'}'{1}\"", new Object[] { itemType.getTargetNamespace(), itemType.getName() });
      
      SchemaTreeNode newNode = new SchemaTreeNode(str, itemType.getLocator());
      
      this.currNode.add(newNode);
    }
  }
  
  public void unionSimpleType(XSUnionSimpleType type)
  {
    int len = type.getMemberSize();
    StringBuffer ref = new StringBuffer();
    for (int i = 0; i < len; i++)
    {
      XSSimpleType member = type.getMember(i);
      if (member.isGlobal()) {
        ref.append(MessageFormat.format(" '{'{0}'}'{1}", new Object[] { member.getTargetNamespace(), member.getName() }));
      }
    }
    String name = "Union memberTypes=\"" + ref + "\"";
    
    SchemaTreeNode newNode = new SchemaTreeNode(name, type.getLocator());
    this.currNode.add(newNode);
    this.currNode = newNode;
    for (int i = 0; i < len; i++)
    {
      XSSimpleType member = type.getMember(i);
      if (member.isLocal()) {
        simpleType(member);
      }
    }
    this.currNode = ((SchemaTreeNode)this.currNode.getParent());
  }
  
  public void restrictionSimpleType(XSRestrictionSimpleType type)
  {
    if (type.getBaseType() == null)
    {
      if (!type.getName().equals("anySimpleType")) {
        throw new InternalError();
      }
      if (!"http://www.w3.org/2001/XMLSchema".equals(type.getTargetNamespace())) {
        throw new InternalError();
      }
      return;
    }
    XSSimpleType baseType = type.getSimpleBaseType();
    
    String str = MessageFormat.format("Restriction {0}", new Object[] { " base=\"{" + baseType.getTargetNamespace() + "}" + baseType.getName() + "\"" });
    
    SchemaTreeNode newNode = new SchemaTreeNode(str, baseType.getLocator());
    this.currNode.add(newNode);
    this.currNode = newNode;
    if (baseType.isLocal()) {
      simpleType(baseType);
    }
    Iterator itr = type.iterateDeclaredFacets();
    while (itr.hasNext()) {
      facet((XSFacet)itr.next());
    }
    this.currNode = ((SchemaTreeNode)this.currNode.getParent());
  }
  
  public void facet(XSFacet facet)
  {
    SchemaTreeNode newNode = new SchemaTreeNode(MessageFormat.format("{0} value=\"{1}\"", new Object[] { facet.getName(), facet.getValue() }), facet.getLocator());
    
    this.currNode.add(newNode);
  }
  
  public void notation(XSNotation notation)
  {
    SchemaTreeNode newNode = new SchemaTreeNode(MessageFormat.format("Notation name='\"0}\" public =\"{1}\" system=\"{2}\"", new Object[] { notation.getName(), notation.getPublicId(), notation.getSystemId() }), notation.getLocator());
    
    this.currNode.add(newNode);
  }
  
  public void complexType(XSComplexType type)
  {
    SchemaTreeNode newNode = new SchemaTreeNode(MessageFormat.format("ComplexType {0}", new Object[] { " name=\"" + type.getName() + "\"" }), type.getLocator());
    
    this.currNode.add(newNode);
    this.currNode = newNode;
    if (type.getContentType().asSimpleType() != null)
    {
      SchemaTreeNode newNode2 = new SchemaTreeNode("Simple content", type.getContentType().getLocator());
      
      this.currNode.add(newNode2);
      this.currNode = newNode2;
      
      XSType baseType = type.getBaseType();
      if (type.getDerivationMethod() == 2)
      {
        String str = MessageFormat.format("Restriction base=\"<{0}>{1}\"", new Object[] { baseType.getTargetNamespace(), baseType.getName() });
        
        SchemaTreeNode newNode3 = new SchemaTreeNode(str, baseType.getLocator());
        
        this.currNode.add(newNode3);
        this.currNode = newNode3;
        
        dumpComplexTypeAttribute(type);
        
        this.currNode = ((SchemaTreeNode)this.currNode.getParent());
      }
      else
      {
        String str = MessageFormat.format("Extension base=\"<{0}>{1}\"", new Object[] { baseType.getTargetNamespace(), baseType.getName() });
        
        SchemaTreeNode newNode3 = new SchemaTreeNode(str, baseType.getLocator());
        
        this.currNode.add(newNode3);
        this.currNode = newNode3;
        if ((type.getTargetNamespace().compareTo(baseType.getTargetNamespace()) == 0) && (type.getName().compareTo(baseType.getName()) == 0))
        {
          SchemaTreeNode newNodeRedefine = new SchemaTreeNode("redefine", type.getLocator());
          
          this.currNode.add(newNodeRedefine);
          this.currNode = newNodeRedefine;
          baseType.visit(this);
          this.currNode = ((SchemaTreeNode)newNodeRedefine.getParent());
        }
        dumpComplexTypeAttribute(type);
        
        this.currNode = ((SchemaTreeNode)this.currNode.getParent());
      }
      this.currNode = ((SchemaTreeNode)this.currNode.getParent());
    }
    else
    {
      SchemaTreeNode newNode2 = new SchemaTreeNode("Complex content", type.getContentType().getLocator());
      
      this.currNode.add(newNode2);
      this.currNode = newNode2;
      
      XSComplexType baseType = type.getBaseType().asComplexType();
      if (type.getDerivationMethod() == 2)
      {
        String str = MessageFormat.format("Restriction base=\"<{0}>{1}\"", new Object[] { baseType.getTargetNamespace(), baseType.getName() });
        
        SchemaTreeNode newNode3 = new SchemaTreeNode(str, baseType.getLocator());
        
        this.currNode.add(newNode3);
        this.currNode = newNode3;
        
        type.getContentType().visit(this);
        dumpComplexTypeAttribute(type);
        
        this.currNode = ((SchemaTreeNode)this.currNode.getParent());
      }
      else
      {
        String str = MessageFormat.format("Extension base=\"'{'{0}'}'{1}\"", new Object[] { baseType.getTargetNamespace(), baseType.getName() });
        
        SchemaTreeNode newNode3 = new SchemaTreeNode(str, baseType.getLocator());
        
        this.currNode.add(newNode3);
        this.currNode = newNode3;
        if ((type.getTargetNamespace().compareTo(baseType.getTargetNamespace()) == 0) && (type.getName().compareTo(baseType.getName()) == 0))
        {
          SchemaTreeNode newNodeRedefine = new SchemaTreeNode("redefine", type.getLocator());
          
          this.currNode.add(newNodeRedefine);
          this.currNode = newNodeRedefine;
          baseType.visit(this);
          this.currNode = ((SchemaTreeNode)newNodeRedefine.getParent());
        }
        type.getExplicitContent().visit(this);
        dumpComplexTypeAttribute(type);
        
        this.currNode = ((SchemaTreeNode)this.currNode.getParent());
      }
      this.currNode = ((SchemaTreeNode)this.currNode.getParent());
    }
    this.currNode = ((SchemaTreeNode)this.currNode.getParent());
  }
  
  private void dumpComplexTypeAttribute(XSComplexType type)
  {
    Iterator itr = type.iterateAttGroups();
    while (itr.hasNext()) {
      dumpRef((XSAttGroupDecl)itr.next());
    }
    itr = type.iterateDeclaredAttributeUses();
    while (itr.hasNext()) {
      attributeUse((XSAttributeUse)itr.next());
    }
  }
  
  public void elementDecl(XSElementDecl decl)
  {
    elementDecl(decl, "");
  }
  
  private void elementDecl(XSElementDecl decl, String extraAtts)
  {
    XSType type = decl.getType();
    
    String str = MessageFormat.format("Element name=\"{0}\"{1}{2}", new Object[] { decl.getName(), " type=\"{" + type.getTargetNamespace() + "}" + type.getName() + "\"", extraAtts });
    
    SchemaTreeNode newNode = new SchemaTreeNode(str, decl.getLocator());
    this.currNode.add(newNode);
    this.currNode = newNode;
    if ((type.isLocal()) && 
      (type.isLocal())) {
      type.visit(this);
    }
    this.currNode = ((SchemaTreeNode)this.currNode.getParent());
  }
  
  public void modelGroupDecl(XSModelGroupDecl decl)
  {
    SchemaTreeNode newNode = new SchemaTreeNode(MessageFormat.format("Group name=\"{0}\"", new Object[] { decl.getName() }), decl.getLocator());
    
    this.currNode.add(newNode);
    this.currNode = newNode;
    
    modelGroup(decl.getModelGroup());
    
    this.currNode = ((SchemaTreeNode)this.currNode.getParent());
  }
  
  public void modelGroup(XSModelGroup group)
  {
    modelGroup(group, "");
  }
  
  private void modelGroup(XSModelGroup group, String extraAtts)
  {
    SchemaTreeNode newNode = new SchemaTreeNode(MessageFormat.format("{0}{1}", new Object[] { group.getCompositor(), extraAtts }), group.getLocator());
    
    this.currNode.add(newNode);
    this.currNode = newNode;
    
    int len = group.getSize();
    for (int i = 0; i < len; i++) {
      particle(group.getChild(i));
    }
    this.currNode = ((SchemaTreeNode)this.currNode.getParent());
  }
  
  public void particle(XSParticle part)
  {
    StringBuffer buf = new StringBuffer();
    
    int i = part.getMaxOccurs();
    if (i == -1) {
      buf.append(" maxOccurs=\"unbounded\"");
    } else if (i != 1) {
      buf.append(" maxOccurs=\"" + i + "\"");
    }
    i = part.getMinOccurs();
    if (i != 1) {
      buf.append(" minOccurs=\"" + i + "\"");
    }
    final String extraAtts = buf.toString();
    
    part.getTerm().visit(new XSTermVisitor()
    {
      public void elementDecl(XSElementDecl decl)
      {
        if (decl.isLocal())
        {
          SchemaTreeTraverser.this.elementDecl(decl, extraAtts);
        }
        else
        {
          SchemaTreeTraverser.SchemaTreeNode newNode = new SchemaTreeTraverser.SchemaTreeNode(MessageFormat.format("Element ref=\"'{'{0}'}'{1}\"{2}", new Object[] { decl.getTargetNamespace(), decl.getName(), extraAtts }), decl.getLocator());
          
          SchemaTreeTraverser.this.currNode.add(newNode);
        }
      }
      
      public void modelGroupDecl(XSModelGroupDecl decl)
      {
        SchemaTreeTraverser.SchemaTreeNode newNode = new SchemaTreeTraverser.SchemaTreeNode(MessageFormat.format("Group ref=\"'{'{0}'}'{1}\"{2}", new Object[] { decl.getTargetNamespace(), decl.getName(), extraAtts }), decl.getLocator());
        
        SchemaTreeTraverser.this.currNode.add(newNode);
      }
      
      public void modelGroup(XSModelGroup group)
      {
        SchemaTreeTraverser.this.modelGroup(group, extraAtts);
      }
      
      public void wildcard(XSWildcard wc)
      {
        SchemaTreeTraverser.this.wildcard(wc, extraAtts);
      }
    });
  }
  
  public void wildcard(XSWildcard wc)
  {
    wildcard(wc, "");
  }
  
  private void wildcard(XSWildcard wc, String extraAtts)
  {
    SchemaTreeNode newNode = new SchemaTreeNode(MessageFormat.format("Any ", new Object[] { extraAtts }), wc.getLocator());
    
    this.currNode.add(newNode);
  }
  
  public void annotation(XSAnnotation ann) {}
  
  public void empty(XSContentType t) {}
  
  public void identityConstraint(XSIdentityConstraint ic) {}
  
  public void xpath(XSXPath xp) {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\util\SchemaTreeTraverser.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */