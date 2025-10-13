/*
  Vault 3
  (C) Copyright 2025, Eric Bergman-Terrell
  
  This file is part of Vault 3.

  Vault 3 is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  Vault 3 is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with Vault 3.  If not, see <http://www.gnu.org/licenses/>.
*/

package mainPackage;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

public class VaultTreeContentProvider implements ITreeContentProvider, IDeltaListener {
	@Override
	public void add(DeltaEvent event) {
		final OutlineItem parentNode = ((OutlineItem) event.receiver()).getParent();
		Globals.getLogger().info("VaultTreeContentProvider.addChild");
		treeViewer.refresh(parentNode, false);
	}

	@Override
	public void swap(DeltaEvent event) {
		final OutlineItem node = ((OutlineItem) event.receiver()).getParent();
		Globals.getLogger().info("VaultTreeContentProvider.swap");
		treeViewer.refresh(node, false);
	}
	
	@Override
	public void titleChanged(DeltaEvent event) {
		final OutlineItem node = ((OutlineItem) event.receiver()).getParent();
		Globals.getLogger().info("VaultTreeContentProvider.titleChanged");
		treeViewer.refresh(node, true);
	}
	
	@Override
	public void indent(DeltaEvent event) {
		final OutlineItem node = ((OutlineItem) event.receiver()).getParent().getParent();
		Globals.getLogger().info("VaultTreeContentProvider.indent");
		treeViewer.refresh(node, false);
	}

	@Override
	public void unindent(DeltaEvent event) {
		final OutlineItem node = ((OutlineItem) event.receiver());
		Globals.getLogger().info("VaultTreeContentProvider.unindent");
		treeViewer.refresh(node.getParent().getParent(), false);
	}

	@Override
	public void remove(DeltaEvent event) {
		final OutlineItem parentNode = ((OutlineItem) event.receiver()).getParent();
		Globals.getLogger().info("VaultTreeContentProvider.remove");
		treeViewer.refresh(parentNode, false);
	}

	private TreeViewer treeViewer;
	
	public VaultTreeContentProvider() {
	}
	
	@Override
	public Object[] getChildren(Object obj) {
		OutlineItem outlineItem = (OutlineItem) obj;

		return outlineItem.getChildren().toArray();
	}

	@Override
	public Object getParent(Object obj) {
		final OutlineItem outlineItem = (OutlineItem) obj;
		
		return outlineItem.getParent();
	}

	@Override
	public boolean hasChildren(Object obj) {
		final OutlineItem outlineItem = (OutlineItem) obj;
		
		return outlineItem.hasChildren();
	}

	@Override
	public Object[] getElements(Object obj) {
		final OutlineItem outlineItem = (OutlineItem) obj;

		return outlineItem.getChildren().toArray();
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		treeViewer = (TreeViewer) viewer;
		
		if (oldInput != null) {
			removeListenerFrom((OutlineItem) oldInput);
		}
		
		if (newInput != null) {
			addListenerTo((OutlineItem) newInput);
		}
	}

	protected void addListenerTo(OutlineItem item) {
		item.addListener(this);

		item.getChildren().forEach(this::addListenerTo);
	}
	
	protected void removeListenerFrom(OutlineItem item) {
		item.removeListener(this);

		item.getChildren().forEach(this::removeListenerFrom);
	}
}
