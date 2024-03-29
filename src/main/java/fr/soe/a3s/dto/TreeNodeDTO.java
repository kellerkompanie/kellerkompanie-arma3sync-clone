package fr.soe.a3s.dto;

public interface TreeNodeDTO extends java.lang.Comparable{

	public String getName();

	public void setName(String name);

	@Override
	public String toString();

	public boolean isLeaf();

	public boolean isSelected();

	public void setSelected(boolean value);

	public TreeDirectoryDTO getParent();

	public void setParent(TreeDirectoryDTO treeDirectory);
}
