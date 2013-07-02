package org.dawnsci.common.widgets.tree;

import javax.measure.quantity.Quantity;

import org.dawnsci.common.widgets.Activator;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

public class NodeLabelProvider extends ColumnLabelProvider {

	private int icolumn;

	public NodeLabelProvider(int icolumn) {
		this.icolumn = icolumn;
	}


	@Override
	public String getText(Object element) {
		
		final String ret = new String();
		if (!(element instanceof LabelNode)) {
			return ret;
		}
		if (element instanceof NumericNode) {
			getText(ret, (NumericNode<?>)element);
			
		} else  if (element instanceof ObjectNode) {
			final ObjectNode on = (ObjectNode)element;
			if (on.isSubClass()) return ret;
			getText(ret, on);
		}
		return ret;
	}
	
	private String getText(String ret, ObjectNode node) {
		
		switch(icolumn) {
		
		case 1: // Default
			return ret = ret + node.getValue()+"";
			
		case 2: // Value
			return ret = ret + node.getValue()+"";
			 
		case 3: // Unit
			//return ret.append("-", StyledString.QUALIFIER_STYLER);

		}
		return ret;
	}


	private String getText(String ret, NumericNode<? extends Quantity> node) {
		
		switch(icolumn) {
		
		case 1: // Default
			return ret = ret + node.getDefaultValue(true);
			
		case 2: // Value
			if (node.isNaN()) {
				if (node.isEditable()) {
					ret = ret + "N/A";
					ret = ret +" *";
				} else {
					ret = ret + "N/A";
				}
			    return ret;
			}
			if (node.isEditable()) {
				ret = ret + node.getValue(true);
				ret = ret +" *";
			} else {
				ret = ret + node.getValue(true);
			}
			return ret;
			
		case 3: // Unit
			if (node.isEditable()) {
				return ret = ret + node.getUnit().toString();
			} else {
				return ret = ret +node.getUnit().toString();
			}

		}
		return ret;
	}


	@Override
	public String getToolTipText(Object element) {
		
		if (!(element instanceof LabelNode)) return super.getToolTipText(element);
		
		LabelNode ln = (LabelNode)element;
		StringBuilder buf = new StringBuilder();
//		buf.append("'");
//		buf.append(ln.getPath());
//		buf.append("'\n");
		
		if (ln.getTooltip()!=null) {
			buf.append(ln.getTooltip());
			buf.append("\n");
		}
		
		if (ln.isEditable()) buf.append(" Click to edit the value or the units.\n");

		buf.append(" Right click to copy or reset value.");
		
		return buf.toString();
	}

	public int getToolTipDisplayDelayTime(Object object) {
		return 500;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
	 */
	public Color getBackground(Object element) {
		if (element instanceof ColorNode && (icolumn==1 || icolumn==2)) {
			return ((ColorNode)element).getColor();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
	 */
	public Color getForeground(Object element) {
		if (element instanceof ColorNode  && (icolumn==1 || icolumn==2)) { // Value
			return ((ColorNode)element).getColor();
		}
		return null;
	}

	private Image ticked, unticked;
	public Image getImage(Object element) {
		if (element instanceof BooleanNode  && (icolumn==1 || icolumn==2)) { // Value
			if (ticked==null)   ticked   = Activator.getImage("icons/ticked.png");
			if (unticked==null) unticked = Activator.getImage("icons/unticked.gif");
			return ((BooleanNode)element).isValue() 
				   ? ticked
				   : unticked;
		}
		return null;
	}

	public void dispose() {
		if (ticked!=null)   ticked.dispose();
		if (unticked!=null) unticked.dispose();
	}
}
