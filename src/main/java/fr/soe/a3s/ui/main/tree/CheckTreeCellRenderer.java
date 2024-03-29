package fr.soe.a3s.ui.main.tree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import fr.soe.a3s.dto.TreeDirectoryDTO;
import fr.soe.a3s.dto.TreeLeafDTO;
import fr.soe.a3s.dto.TreeNodeDTO;

public class CheckTreeCellRenderer extends JPanel implements TreeCellRenderer {
	private final TreeCellRenderer delegate;
	private final JCheckBox checkBox = new JCheckBox();

	public CheckTreeCellRenderer(TreeCellRenderer delegate) {
		this.delegate = delegate;
		setLayout(new BorderLayout());
		setOpaque(false);
		checkBox.setOpaque(false);
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		Component renderer = delegate.getTreeCellRendererComponent(tree, value,
				selected, expanded, leaf, row, hasFocus);

		TreePath path = tree.getPathForRow(row);
		if (path != null) {
			TreeNodeDTO treeNodeDTO = (TreeNodeDTO) value;
			// Missing and duplicated selected addons
			renderer.setBackground(UIManager.getColor("Tree.textBackground"));
			if (treeNodeDTO.isLeaf()) {
				TreeLeafDTO leafDTO = (TreeLeafDTO) treeNodeDTO;
				if (leafDTO.isMissing() || leafDTO.isDuplicatedSelection()) {
					renderer.setForeground(Color.RED);
				}
			}
			// Selected addons
			if (treeNodeDTO.isSelected()) {
				checkBox.setSelected(Boolean.TRUE);
			} else {
				checkBox.setSelected(Boolean.FALSE);
			}

			checkBox.setIcon(null);
			// Partially complete folder
			if (!treeNodeDTO.isLeaf() && treeNodeDTO.getParent() != null) {
				TreeDirectoryDTO treeDirectoryDTO = (TreeDirectoryDTO) treeNodeDTO;
				int nbNodes = treeDirectoryDTO.getList().size();
				int nbSelectedNodes = 0;
				for (TreeNodeDTO t : treeDirectoryDTO.getList()) {
					if (!t.isSelected()) {
						nbSelectedNodes++;
					}
				}
				if (nbNodes != 0 && nbSelectedNodes != 0
						&& nbSelectedNodes < nbNodes) {
					checkBox.setSelected(Boolean.FALSE);
					checkBox.setIcon(new IndeterminateIcon());
				}
			}
		}

		removeAll();
		add(checkBox, BorderLayout.WEST);
		add(renderer, BorderLayout.CENTER);
		return this;
	}
}
