package com.sun.xml.xsom.impl.scd;

import com.sun.xml.xsom.XSAttContainer;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSListSimpleType;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroup.Compositor;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract interface Axis<T extends XSComponent>
{
  public static final Axis<XSSchema> ROOT = new Axis()
  {
    public Iterator<XSSchema> iterator(XSComponent contextNode)
    {
      return contextNode.getRoot().iterateSchema();
    }
    
    public Iterator<XSSchema> iterator(Iterator<? extends XSComponent> contextNodes)
    {
      if (!contextNodes.hasNext()) {
        return Iterators.empty();
      }
      return iterator((XSComponent)contextNodes.next());
    }
    
    public boolean isModelGroup()
    {
      return false;
    }
    
    public String toString()
    {
      return "root::";
    }
  };
  public static final Axis<XSComponent> INTERMEDIATE_SKIP = new AbstractAxisImpl()
  {
    public Iterator<XSComponent> elementDecl(XSElementDecl decl)
    {
      XSComplexType ct = decl.getType().asComplexType();
      if (ct == null) {
        return empty();
      }
      return new Iterators.Union(singleton(ct), complexType(ct));
    }
    
    public Iterator<XSComponent> modelGroupDecl(XSModelGroupDecl decl)
    {
      return descendants(decl.getModelGroup());
    }
    
    public Iterator<XSComponent> particle(XSParticle particle)
    {
      return descendants(particle.getTerm().asModelGroup());
    }
    
    private Iterator<XSComponent> descendants(XSModelGroup mg)
    {
      List<XSComponent> r = new ArrayList();
      visit(mg, r);
      return r.iterator();
    }
    
    private void visit(XSModelGroup mg, List<XSComponent> r)
    {
      r.add(mg);
      for (XSParticle p : mg)
      {
        XSModelGroup child = p.getTerm().asModelGroup();
        if (child != null) {
          visit(child, r);
        }
      }
    }
    
    public String toString()
    {
      return "(intermediateSkip)";
    }
  };
  public static final Axis<XSComponent> DESCENDANTS = new Axis()
  {
    public Iterator<XSComponent> iterator(XSComponent contextNode)
    {
      return new Visitor().iterator(contextNode);
    }
    
    public Iterator<XSComponent> iterator(Iterator<? extends XSComponent> contextNodes)
    {
      return new Visitor().iterator(contextNodes);
    }
    
    public boolean isModelGroup()
    {
      return false;
    }
    
    final class Visitor
      extends AbstractAxisImpl<XSComponent>
    {
      private final Set<XSComponent> visited;
      
      Visitor()
      {
        this.visited = new HashSet();
      }
      
      final class Recursion
        extends Iterators.Map<XSComponent, XSComponent>
      {
        public Recursion()
        {
          super();
        }
        
        protected Iterator<XSComponent> apply(XSComponent u)
        {
          return Axis.DESCENDANTS.iterator(u);
        }
      }
      
      public Iterator<XSComponent> schema(XSSchema schema)
      {
        if (this.visited.add(schema)) {
          return ret(schema, new Recursion(schema.iterateElementDecls()));
        }
        return empty();
      }
      
      public Iterator<XSComponent> elementDecl(XSElementDecl decl)
      {
        if (this.visited.add(decl)) {
          return ret(decl, iterator(decl.getType()));
        }
        return empty();
      }
      
      public Iterator<XSComponent> simpleType(XSSimpleType type)
      {
        if (this.visited.add(type)) {
          return ret(type, FACET.iterator(type));
        }
        return empty();
      }
      
      public Iterator<XSComponent> complexType(XSComplexType type)
      {
        if (this.visited.add(type)) {
          return ret(type, iterator(type.getContentType()));
        }
        return empty();
      }
      
      public Iterator<XSComponent> particle(XSParticle particle)
      {
        if (this.visited.add(particle)) {
          return ret(particle, iterator(particle.getTerm()));
        }
        return empty();
      }
      
      public Iterator<XSComponent> modelGroupDecl(XSModelGroupDecl decl)
      {
        if (this.visited.add(decl)) {
          return ret(decl, iterator(decl.getModelGroup()));
        }
        return empty();
      }
      
      public Iterator<XSComponent> modelGroup(XSModelGroup group)
      {
        if (this.visited.add(group)) {
          return ret(group, new Recursion(group.iterator()));
        }
        return empty();
      }
      
      public Iterator<XSComponent> attGroupDecl(XSAttGroupDecl decl)
      {
        if (this.visited.add(decl)) {
          return ret(decl, new Recursion(decl.iterateAttributeUses()));
        }
        return empty();
      }
      
      public Iterator<XSComponent> attributeUse(XSAttributeUse use)
      {
        if (this.visited.add(use)) {
          return ret(use, iterator(use.getDecl()));
        }
        return empty();
      }
      
      public Iterator<XSComponent> attributeDecl(XSAttributeDecl decl)
      {
        if (this.visited.add(decl)) {
          return ret(decl, iterator(decl.getType()));
        }
        return empty();
      }
      
      private Iterator<XSComponent> ret(XSComponent one, Iterator<? extends XSComponent> rest)
      {
        return union(singleton(one), rest);
      }
    }
    
    public String toString()
    {
      return "/";
    }
  };
  public static final Axis<XSSchema> X_SCHEMA = new Axis()
  {
    public Iterator<XSSchema> iterator(XSComponent contextNode)
    {
      return Iterators.singleton(contextNode.getOwnerSchema());
    }
    
    public Iterator<XSSchema> iterator(Iterator<? extends XSComponent> contextNodes)
    {
      new Iterators.Adapter(contextNodes)
      {
        protected XSSchema filter(XSComponent u)
        {
          return u.getOwnerSchema();
        }
      };
    }
    
    public boolean isModelGroup()
    {
      return false;
    }
    
    public String toString()
    {
      return "x-schema::";
    }
  };
  public static final Axis<XSElementDecl> SUBSTITUTION_GROUP = new AbstractAxisImpl()
  {
    public Iterator<XSElementDecl> elementDecl(XSElementDecl decl)
    {
      return singleton(decl.getSubstAffiliation());
    }
    
    public String toString()
    {
      return "substitutionGroup::";
    }
  };
  public static final Axis<XSAttributeDecl> ATTRIBUTE = new AbstractAxisImpl()
  {
    public Iterator<XSAttributeDecl> complexType(XSComplexType type)
    {
      return attributeHolder(type);
    }
    
    public Iterator<XSAttributeDecl> attGroupDecl(XSAttGroupDecl decl)
    {
      return attributeHolder(decl);
    }
    
    private Iterator<XSAttributeDecl> attributeHolder(XSAttContainer atts)
    {
      new Iterators.Adapter(atts.iterateAttributeUses())
      {
        protected XSAttributeDecl filter(XSAttributeUse u)
        {
          return u.getDecl();
        }
      };
    }
    
    public Iterator<XSAttributeDecl> schema(XSSchema schema)
    {
      return schema.iterateAttributeDecls();
    }
    
    public String toString()
    {
      return "@";
    }
  };
  public static final Axis<XSElementDecl> ELEMENT = new AbstractAxisImpl()
  {
    public Iterator<XSElementDecl> particle(XSParticle particle)
    {
      return singleton(particle.getTerm().asElementDecl());
    }
    
    public Iterator<XSElementDecl> schema(XSSchema schema)
    {
      return schema.iterateElementDecls();
    }
    
    public Iterator<XSElementDecl> modelGroupDecl(XSModelGroupDecl decl)
    {
      return modelGroup(decl.getModelGroup());
    }
    
    public String getName()
    {
      return "";
    }
    
    public String toString()
    {
      return "element::";
    }
  };
  public static final Axis<XSType> TYPE_DEFINITION = new AbstractAxisImpl()
  {
    public Iterator<XSType> schema(XSSchema schema)
    {
      return schema.iterateTypes();
    }
    
    public Iterator<XSType> attributeDecl(XSAttributeDecl decl)
    {
      return singleton(decl.getType());
    }
    
    public Iterator<XSType> elementDecl(XSElementDecl decl)
    {
      return singleton(decl.getType());
    }
    
    public String toString()
    {
      return "~";
    }
  };
  public static final Axis<XSType> BASETYPE = new AbstractAxisImpl()
  {
    public Iterator<XSType> simpleType(XSSimpleType type)
    {
      return singleton(type.getBaseType());
    }
    
    public Iterator<XSType> complexType(XSComplexType type)
    {
      return singleton(type.getBaseType());
    }
    
    public String toString()
    {
      return "baseType::";
    }
  };
  public static final Axis<XSSimpleType> PRIMITIVE_TYPE = new AbstractAxisImpl()
  {
    public Iterator<XSSimpleType> simpleType(XSSimpleType type)
    {
      return singleton(type.getPrimitiveType());
    }
    
    public String toString()
    {
      return "primitiveType::";
    }
  };
  public static final Axis<XSSimpleType> ITEM_TYPE = new AbstractAxisImpl()
  {
    public Iterator<XSSimpleType> simpleType(XSSimpleType type)
    {
      XSListSimpleType baseList = type.getBaseListType();
      if (baseList == null) {
        return empty();
      }
      return singleton(baseList.getItemType());
    }
    
    public String toString()
    {
      return "itemType::";
    }
  };
  public static final Axis<XSSimpleType> MEMBER_TYPE = new AbstractAxisImpl()
  {
    public Iterator<XSSimpleType> simpleType(XSSimpleType type)
    {
      XSUnionSimpleType baseUnion = type.getBaseUnionType();
      if (baseUnion == null) {
        return empty();
      }
      return baseUnion.iterator();
    }
    
    public String toString()
    {
      return "memberType::";
    }
  };
  public static final Axis<XSComponent> SCOPE = new AbstractAxisImpl()
  {
    public Iterator<XSComponent> complexType(XSComplexType type)
    {
      return singleton(type.getScope());
    }
    
    public String toString()
    {
      return "scope::";
    }
  };
  public static final Axis<XSAttGroupDecl> ATTRIBUTE_GROUP = new AbstractAxisImpl()
  {
    public Iterator<XSAttGroupDecl> schema(XSSchema schema)
    {
      return schema.iterateAttGroupDecls();
    }
    
    public String toString()
    {
      return "attributeGroup::";
    }
  };
  public static final Axis<XSModelGroupDecl> MODEL_GROUP_DECL = new AbstractAxisImpl()
  {
    public Iterator<XSModelGroupDecl> schema(XSSchema schema)
    {
      return schema.iterateModelGroupDecls();
    }
    
    public Iterator<XSModelGroupDecl> particle(XSParticle particle)
    {
      return singleton(particle.getTerm().asModelGroupDecl());
    }
    
    public String toString()
    {
      return "group::";
    }
  };
  public static final Axis<XSIdentityConstraint> IDENTITY_CONSTRAINT = new AbstractAxisImpl()
  {
    public Iterator<XSIdentityConstraint> elementDecl(XSElementDecl decl)
    {
      return decl.getIdentityConstraints().iterator();
    }
    
    public Iterator<XSIdentityConstraint> schema(XSSchema schema)
    {
      return super.schema(schema);
    }
    
    public String toString()
    {
      return "identityConstraint::";
    }
  };
  public static final Axis<XSIdentityConstraint> REFERENCED_KEY = new AbstractAxisImpl()
  {
    public Iterator<XSIdentityConstraint> identityConstraint(XSIdentityConstraint decl)
    {
      return singleton(decl.getReferencedKey());
    }
    
    public String toString()
    {
      return "key::";
    }
  };
  public static final Axis<XSNotation> NOTATION = new AbstractAxisImpl()
  {
    public Iterator<XSNotation> schema(XSSchema schema)
    {
      return schema.iterateNotations();
    }
    
    public String toString()
    {
      return "notation::";
    }
  };
  public static final Axis<XSWildcard> WILDCARD = new AbstractAxisImpl()
  {
    public Iterator<XSWildcard> particle(XSParticle particle)
    {
      return singleton(particle.getTerm().asWildcard());
    }
    
    public String toString()
    {
      return "any::";
    }
  };
  public static final Axis<XSWildcard> ATTRIBUTE_WILDCARD = new AbstractAxisImpl()
  {
    public Iterator<XSWildcard> complexType(XSComplexType type)
    {
      return singleton(type.getAttributeWildcard());
    }
    
    public Iterator<XSWildcard> attGroupDecl(XSAttGroupDecl decl)
    {
      return singleton(decl.getAttributeWildcard());
    }
    
    public String toString()
    {
      return "anyAttribute::";
    }
  };
  public static final Axis<XSFacet> FACET = new AbstractAxisImpl()
  {
    public Iterator<XSFacet> simpleType(XSSimpleType type)
    {
      XSRestrictionSimpleType r = type.asRestriction();
      if (r != null) {
        return r.iterateDeclaredFacets();
      }
      return empty();
    }
    
    public String toString()
    {
      return "facet::";
    }
  };
  public static final Axis<XSModelGroup> MODELGROUP_ALL = new ModelGroupAxis(XSModelGroup.Compositor.ALL);
  public static final Axis<XSModelGroup> MODELGROUP_CHOICE = new ModelGroupAxis(XSModelGroup.Compositor.CHOICE);
  public static final Axis<XSModelGroup> MODELGROUP_SEQUENCE = new ModelGroupAxis(XSModelGroup.Compositor.SEQUENCE);
  public static final Axis<XSModelGroup> MODELGROUP_ANY = new ModelGroupAxis(null);
  
  public abstract Iterator<T> iterator(XSComponent paramXSComponent);
  
  public abstract Iterator<T> iterator(Iterator<? extends XSComponent> paramIterator);
  
  public abstract boolean isModelGroup();
  
  public static final class ModelGroupAxis
    extends AbstractAxisImpl<XSModelGroup>
  {
    private final XSModelGroup.Compositor compositor;
    
    ModelGroupAxis(XSModelGroup.Compositor compositor)
    {
      this.compositor = compositor;
    }
    
    public boolean isModelGroup()
    {
      return true;
    }
    
    public Iterator<XSModelGroup> particle(XSParticle particle)
    {
      return filter(particle.getTerm().asModelGroup());
    }
    
    public Iterator<XSModelGroup> modelGroupDecl(XSModelGroupDecl decl)
    {
      return filter(decl.getModelGroup());
    }
    
    private Iterator<XSModelGroup> filter(XSModelGroup mg)
    {
      if (mg == null) {
        return empty();
      }
      if ((mg.getCompositor() == this.compositor) || (this.compositor == null)) {
        return singleton(mg);
      }
      return empty();
    }
    
    public String toString()
    {
      if (this.compositor == null) {
        return "model::*";
      }
      return "model::" + this.compositor;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\scd\Axis.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */