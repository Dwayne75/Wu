package com.sun.javaws.ui;

import com.sun.javaws.cache.Cache;
import com.sun.javaws.cache.DiskCacheEntry;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

class CacheTable
  extends JTable
{
  private static final TableCellRenderer _defaultRenderer = new DefaultTableCellRenderer();
  private static final int MIN_ROW_HEIGHT = 36;
  private boolean _system;
  private int _filter = 0;
  
  public CacheTable(CacheViewer paramCacheViewer, boolean paramBoolean)
  {
    this._system = paramBoolean;
    setShowGrid(false);
    setIntercellSpacing(new Dimension(0, 0));
    setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    int i = getRowHeight();
    if (i < 36) {
      setRowHeight(36);
    }
    setPreferredScrollableViewportSize(new Dimension(640, 280));
    addMouseListener(new MouseAdapter()
    {
      private final CacheViewer val$cv;
      
      public void mousePressed(MouseEvent paramAnonymousMouseEvent)
      {
        if (paramAnonymousMouseEvent.isPopupTrigger())
        {
          int i = paramAnonymousMouseEvent.getY();
          int j = i / CacheTable.this.getRowHeight();
          
          CacheTable.this.getSelectionModel().clearSelection();
          CacheTable.this.getSelectionModel().addSelectionInterval(j, j);
          
          this.val$cv.popupApplicationMenu(CacheTable.this, paramAnonymousMouseEvent.getX(), i);
        }
      }
      
      public void mouseReleased(MouseEvent paramAnonymousMouseEvent)
      {
        if (paramAnonymousMouseEvent.isPopupTrigger())
        {
          int i = paramAnonymousMouseEvent.getY();
          int j = i / CacheTable.this.getRowHeight();
          
          CacheTable.this.getSelectionModel().clearSelection();
          CacheTable.this.getSelectionModel().addSelectionInterval(j, j);
          
          this.val$cv.popupApplicationMenu(CacheTable.this, paramAnonymousMouseEvent.getX(), i);
        }
      }
      
      public void mouseClicked(MouseEvent paramAnonymousMouseEvent)
      {
        Point localPoint = paramAnonymousMouseEvent.getPoint();
        if ((paramAnonymousMouseEvent.getClickCount() == 2) && 
          (paramAnonymousMouseEvent.getButton() == 1))
        {
          int i = CacheTable.this.getColumnModel().getColumnIndexAtX(localPoint.x);
          if (i < 3) {
            this.val$cv.launchApplication();
          }
        }
      }
    });
    reset();
  }
  
  public void setFilter(int paramInt)
  {
    if (paramInt != this._filter)
    {
      this._filter = paramInt;
      reset();
    }
  }
  
  public void reset()
  {
    TableModel localTableModel = getModel();
    if ((localTableModel instanceof CacheTableModel)) {
      ((CacheTableModel)localTableModel).removeMouseListenerFromHeaderInTable(this);
    }
    CacheTableModel localCacheTableModel = new CacheTableModel(this._system, this._filter);
    setModel(localCacheTableModel);
    for (int i = 0; i < getModel().getColumnCount(); i++)
    {
      TableColumn localTableColumn = getColumnModel().getColumn(i);
      localTableColumn.setHeaderRenderer(new CacheTableHeaderRenderer(null));
      int j = localCacheTableModel.getPreferredWidth(i);
      localTableColumn.setPreferredWidth(j);
      localTableColumn.setMinWidth(j);
    }
    setDefaultRenderer(JLabel.class, localCacheTableModel);
    localCacheTableModel.addMouseListenerToHeaderInTable(this);
  }
  
  public CacheObject getCacheObject(int paramInt)
  {
    return ((CacheTableModel)getModel()).getCacheObject(paramInt);
  }
  
  public String[] getAllHrefs()
  {
    ArrayList localArrayList = new ArrayList();
    TableModel localTableModel = getModel();
    if ((localTableModel instanceof CacheTableModel)) {
      for (int i = 0; i < localTableModel.getRowCount(); i++)
      {
        String str = ((CacheTableModel)localTableModel).getRowHref(i);
        if (str != null) {
          localArrayList.add(str);
        }
      }
    }
    return (String[])localArrayList.toArray(new String[0]);
  }
  
  private class CacheTableHeaderRenderer
    extends DefaultTableCellRenderer
  {
    CacheTableHeaderRenderer(CacheTable.1 param1)
    {
      this();
    }
    
    public Component getTableCellRendererComponent(JTable paramJTable, Object paramObject, boolean paramBoolean1, boolean paramBoolean2, int paramInt1, int paramInt2)
    {
      if (paramJTable != null)
      {
        localObject = paramJTable.getTableHeader();
        if (localObject != null)
        {
          setForeground(((JTableHeader)localObject).getForeground());
          setBackground(((JTableHeader)localObject).getBackground());
          setFont(((JTableHeader)localObject).getFont());
        }
      }
      setText(paramObject == null ? "" : paramObject.toString());
      setBorder(UIManager.getBorder("TableHeader.cellBorder"));
      setHorizontalAlignment(0);
      Object localObject = CacheObject.getHeaderToolTipText(paramInt2);
      if ((localObject != null) && (((String)localObject).length() > 0)) {
        setToolTipText((String)localObject);
      }
      return this;
    }
    
    private CacheTableHeaderRenderer() {}
  }
  
  private class CacheTableModel
    extends AbstractTableModel
    implements TableCellRenderer
  {
    private boolean _system;
    private CacheObject[] _rows;
    private int _filter;
    private int _sortColumn;
    private boolean _sortAscending;
    private MouseListener _mouseListener = null;
    
    public CacheTableModel(boolean paramBoolean, int paramInt)
    {
      this._system = paramBoolean;
      this._filter = paramInt;
      this._rows = new CacheObject[0];
      this._sortColumn = -1;
      this._sortAscending = true;
      refresh();
      fireTableDataChanged();
    }
    
    public Component getTableCellRendererComponent(JTable paramJTable, Object paramObject, boolean paramBoolean1, boolean paramBoolean2, int paramInt1, int paramInt2)
    {
      if ((paramObject instanceof Component))
      {
        Component localComponent = (Component)paramObject;
        if (paramBoolean1)
        {
          localComponent.setForeground(paramJTable.getSelectionForeground());
          localComponent.setBackground(paramJTable.getSelectionBackground());
        }
        else
        {
          localComponent.setForeground(paramJTable.getForeground());
          localComponent.setBackground(paramJTable.getBackground());
        }
        CacheObject.hasFocus(localComponent, paramBoolean2);
        return localComponent;
      }
      return CacheTable._defaultRenderer.getTableCellRendererComponent(paramJTable, paramObject, paramBoolean1, paramBoolean2, paramInt1, paramInt2);
    }
    
    public void refresh()
    {
      ArrayList localArrayList = new ArrayList();
      Iterator localIterator = Cache.getJnlpCacheEntries(this._system);
      while (localIterator.hasNext())
      {
        CacheObject localCacheObject = new CacheObject((DiskCacheEntry)localIterator.next(), this);
        if ((localCacheObject.inFilter(this._filter)) && (localCacheObject.getLaunchDesc() != null)) {
          localArrayList.add(localCacheObject);
        }
      }
      this._rows = ((CacheObject[])localArrayList.toArray(new CacheObject[0]));
      if (this._sortColumn != -1) {
        sort();
      }
    }
    
    CacheObject getCacheObject(int paramInt)
    {
      return this._rows[paramInt];
    }
    
    public Object getValueAt(int paramInt1, int paramInt2)
    {
      return this._rows[paramInt1].getObject(paramInt2);
    }
    
    public int getRowCount()
    {
      return this._rows.length;
    }
    
    public String getRowHref(int paramInt)
    {
      return this._rows[paramInt].getHref();
    }
    
    public int getColumnCount()
    {
      return CacheObject.getColumnCount();
    }
    
    public boolean isCellEditable(int paramInt1, int paramInt2)
    {
      return this._rows[paramInt1].isEditable(paramInt2);
    }
    
    public Class getColumnClass(int paramInt)
    {
      return CacheObject.getClass(paramInt);
    }
    
    public String getColumnName(int paramInt)
    {
      return CacheObject.getColumnName(paramInt);
    }
    
    public void setValueAt(Object paramObject, int paramInt1, int paramInt2)
    {
      this._rows[paramInt1].setValue(paramInt2, paramObject);
    }
    
    public int getPreferredWidth(int paramInt)
    {
      return CacheObject.getPreferredWidth(paramInt);
    }
    
    public void removeMouseListenerFromHeaderInTable(JTable paramJTable)
    {
      if (this._mouseListener != null) {
        paramJTable.getTableHeader().removeMouseListener(this._mouseListener);
      }
    }
    
    public void addMouseListenerToHeaderInTable(JTable paramJTable)
    {
      JTable localJTable = paramJTable;
      localJTable.setColumnSelectionAllowed(false);
      ListSelectionModel localListSelectionModel = localJTable.getSelectionModel();
      this._mouseListener = new MouseAdapter()
      {
        private final JTable val$tableView;
        private final ListSelectionModel val$lsm;
        
        public void mouseClicked(MouseEvent paramAnonymousMouseEvent)
        {
          TableColumnModel localTableColumnModel = this.val$tableView.getColumnModel();
          int i = localTableColumnModel.getColumnIndexAtX(paramAnonymousMouseEvent.getX());
          int j = this.val$lsm.getMinSelectionIndex();
          this.val$lsm.clearSelection();
          int k = this.val$tableView.convertColumnIndexToModel(i);
          if ((paramAnonymousMouseEvent.getClickCount() == 1) && (k >= 0))
          {
            int m = paramAnonymousMouseEvent.getModifiers() & 0x1;
            
            CacheTable.CacheTableModel.this._sortAscending = (m == 0);
            CacheTable.CacheTableModel.this._sortColumn = k;
            CacheTable.CacheTableModel.this.runSort(this.val$lsm, j);
          }
        }
      };
      localJTable.getTableHeader().addMouseListener(this._mouseListener);
    }
    
    public void sort()
    {
      int i = 0;
      int j;
      int k;
      CacheObject localCacheObject;
      if (this._sortAscending) {
        for (j = 0; j < getRowCount(); j++) {
          for (k = j + 1; k < getRowCount(); k++) {
            if (this._rows[j].compareColumns(this._rows[k], this._sortColumn) > 0)
            {
              i = 1;
              localCacheObject = this._rows[j];
              this._rows[j] = this._rows[k];
              this._rows[k] = localCacheObject;
            }
          }
        }
      } else {
        for (j = 0; j < getRowCount(); j++) {
          for (k = j + 1; k < getRowCount(); k++) {
            if (this._rows[k].compareColumns(this._rows[j], this._sortColumn) > 0)
            {
              i = 1;
              localCacheObject = this._rows[j];
              this._rows[j] = this._rows[k];
              this._rows[k] = localCacheObject;
            }
          }
        }
      }
      if (i != 0) {
        fireTableDataChanged();
      }
    }
    
    private void runSort(ListSelectionModel paramListSelectionModel, int paramInt)
    {
      if (CacheViewer.getStatus() != 4) {
        new Thread(new Runnable()
        {
          private final int val$selected;
          private final ListSelectionModel val$lsm;
          
          public void run()
          {
            CacheViewer.setStatus(4);
            try
            {
              CacheObject localCacheObject = null;
              if (this.val$selected >= 0) {
                localCacheObject = CacheTable.CacheTableModel.this._rows[this.val$selected];
              }
              CacheTable.CacheTableModel.this.sort();
              if (localCacheObject != null) {
                for (int i = 0; i < CacheTable.CacheTableModel.this._rows.length; i++) {
                  if (CacheTable.CacheTableModel.this._rows[i] == localCacheObject)
                  {
                    this.val$lsm.addSelectionInterval(i, i);
                    break;
                  }
                }
              }
            }
            finally
            {
              CacheViewer.setStatus(0);
            }
          }
        }).start();
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\ui\CacheTable.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */