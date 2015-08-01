
    Ext.define('Ext.ux.RowExpanderPlus', {
        extend: 'Ext.grid.plugin.RowExpander',
        alias: 'plugin.rowexpanderplus',
            
        expandOnlyOne: true,
                        
        lastExpandedRowIdx: null,
                                
        // Overwrite RowExpander.toggleRow(rowIdx)
        toggleRow: function(rowIdx) {
          var row = Ext.get(this.view.getNode(rowIdx));
          if (row.hasCls(this.rowCollapsedCls)) {
            if (this.lastExpandedRowIdx!=null&&this.expandOnlyOne == true) {
              this.collapseRow(this.lastExpandedRowIdx);
            }
            this.expandRow(rowIdx);
            this.lastExpandedRowIdx = rowIdx;
          } else {
            this.collapseRow(rowIdx);
            this.lastExpandedRowIdx = null;
          }
        },
        expandRow: function(rowIdx) {
          var view = this.view,
            rowNode = view.getNode(rowIdx),
            row = Ext.get(rowNode),
            nextBd = Ext.get(row).down(this.rowBodyTrSelector),
            record = view.getRecord(rowNode);
          row.removeCls(this.rowCollapsedCls);
          nextBd.removeCls(this.rowBodyHiddenCls);
          this.recordsExpanded[record.internalId] = true;
          view.refreshSize();
          view.fireEvent('expandbody', rowNode, record, nextBd.dom);
        },
        collapseRow: function(rowIdx) {
          var view = this.view,
            rowNode = view.getNode(rowIdx),
            row = Ext.get(rowNode),
            nextBd = Ext.get(row).down(this.rowBodyTrSelector),
            record = view.getRecord(rowNode);
          row.addCls(this.rowCollapsedCls);
          nextBd.addCls(this.rowBodyHiddenCls);
          this.recordsExpanded[record.internalId] = false;
          view.refreshSize();
          view.fireEvent('collapsebody', rowNode, record, nextBd.dom);
        },
        collapseLastRow: function() {
          this.collapseRow(this.lastExpandedRowIdx);
        }
    });
