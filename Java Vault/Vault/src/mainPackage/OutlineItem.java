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

import java.text.Collator;
import java.util.*;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.eclipse.swt.graphics.RGB;

import fonts.FontList;
import fonts.SWTFont;

public class OutlineItem {
    private String title;

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return title != null ? title : StringLiterals.EmptyString;
    }

    public void setTitle(String title) {
        this.title = title;
        fireTitleChanged(this);
    }

    private String text;

    public String getText() {
        return text != null ? text : StringLiterals.EmptyString;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean hasFont() {
        return rgb != null && fontList != null;
    }

    private FontList fontList;

    public FontList getFontList() {
        return fontList;
    }

    public String getFontListString() {
        String fontListString = null;

        if (fontList != null && fontList.size() > 0) {
            fontListString = FontList.serialize(fontList);
        }

        return fontListString;
    }

    public void setFontList(FontList fontList) {
        this.fontList = fontList;
    }

    public String getFontString() {
        String fontString = null;

        if (fontList != null) {
            final SWTFont font = (SWTFont) fontList.getFont();

            if (font != null) {
                fontString = font.getData();
            }
        }

        return fontString;
    }

    private RGB rgb;

    public RGB getRGB() {
        return rgb;
    }

    public void setRGB(RGB rgb) {
        this.rgb = rgb;
    }

    private String photoPath;

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    private OutlineItem parent;

    private final List<OutlineItem> children;

    private final UUID uuid;

    public UUID getUuid() {
        return uuid;
    }

    private boolean allowScaling;

    public boolean getAllowScaling() {
        return allowScaling;
    }

    public void setAllowScaling(boolean value) {
        allowScaling = value;
    }

    protected IDeltaListener listener = NullDeltaListener.getSoleInstance();

    public void addListener(IDeltaListener listener) {
        this.listener = listener;
    }

    public void removeListener(IDeltaListener listener) {
        if (this.listener.equals(listener)) {
            this.listener = NullDeltaListener.getSoleInstance();
        }
    }

    protected void fireAdd(Object added) {
        listener.add(new DeltaEvent(added));
    }

    protected void fireSwap(Object swapped) {
        listener.swap(new DeltaEvent(swapped));
    }

    protected void fireTitleChanged(Object titleChanged) {
        listener.titleChanged(new DeltaEvent(titleChanged));
    }

    protected void fireIndent(Object indented) {
        listener.indent(new DeltaEvent(indented));
    }

    protected void fireUnindent(Object unindented) {
        listener.unindent(new DeltaEvent(unindented));
    }

    protected void fireRemove(Object removed) {
        listener.remove(new DeltaEvent(removed));
    }

    public OutlineItem() {
        allowScaling = true;

        children = new ArrayList<>();
        uuid = UUID.randomUUID();
    }

    public OutlineItem(OutlineItem node, OutlineItem parent, boolean createNewUUID) {
        allowScaling = node.allowScaling;

        children = new ArrayList<>();

        setTitle(node.getTitle());
        setText(node.getText());
        setPhotoPath(node.getPhotoPath());

        this.uuid = createNewUUID ? UUID.randomUUID() : node.uuid;

        this.parent = parent;

        for (OutlineItem childNode : node.children) {
            OutlineItem newChildNode = new OutlineItem(childNode, this, createNewUUID);
            children.add(newChildNode);
        }
    }

    public void setParent(OutlineItem parent) {
        this.parent = parent;
    }

    public OutlineItem getParent() {
        return parent;
    }

    public OutlineItem getRoot() {
        OutlineItem root = this;

        while (root.parent != null) {
            root = root.parent;
        }

        return root;
    }

    public enum AddDirection {Above, Below}

    public void add(OutlineItem newNode, AddDirection addDirection) {
        newNode.parent = this.parent;

        int index = parent.children.indexOf(this);

        if (addDirection == AddDirection.Below) {
            index++;
        }

        // Add new node after current node.
        parent.addChild(newNode, true, index);
    }

    private void remove() {
        parent.children.remove(this);
    }

    public static void remove(List<OutlineItem> nodesToRemove) {
        // If there are multiple items in the nodesToRemove list, only fire for nodes with unique parents.
        final HashSet<OutlineItem> uniqueParentNodes = new HashSet<>();
        final List<OutlineItem> nodesToFire = nodesToRemove.stream().filter(nodeToRemove -> uniqueParentNodes.add(nodeToRemove.parent)).toList();

        for (OutlineItem nodeToRemove : nodesToRemove) {
            nodeToRemove.remove();
        }

        for (OutlineItem nodeToFire : nodesToFire) {
            nodeToFire.fireRemove(nodeToFire);
        }
    }

    private static void moveOrPaste(boolean move, List<OutlineItem> nodes, OutlineItem targetNode, boolean targetNodeIsExpanded, boolean addAtTop) {
        if (move) {
            remove(nodes);
        }

        if (addAtTop) {
            int index = 0;

            final OutlineItem rootNode = targetNode.getRoot();

            for (OutlineItem node : nodes) {
                rootNode.addChild(node, index++);
            }
        } else {
            if (targetNode.hasChildren() && targetNodeIsExpanded) {
                int index = 0;

                for (OutlineItem node : nodes) {
                    targetNode.addChild(node, index++);
                }
            } else {
                int index = targetNode.parent.children.indexOf(targetNode);

                for (OutlineItem node : nodes) {
                    targetNode.parent.addChild(node, ++index);
                }
            }
        }
    }

    public static void move(List<OutlineItem> nodesToMove, OutlineItem targetNode, boolean targetNodeIsExpanded, boolean addAtTop) {
        moveOrPaste(true, nodesToMove, targetNode, targetNodeIsExpanded, addAtTop);
    }

    public static void paste(List<OutlineItem> nodesToPaste, OutlineItem targetNode, boolean targetNodeIsExpanded, boolean addAtTop) {
        moveOrPaste(false, nodesToPaste, targetNode, targetNodeIsExpanded, addAtTop);
    }

    private void indent() {
        int index = parent.children.indexOf(this) - 1;

        this.remove();
        parent.children.get(index).addChild(this, false);
    }

    public static void swap(OutlineItem parent, int index1, int index2, boolean fireSwap) {
        final OutlineItem temp = parent.children.get(index1);
        parent.children.set(index1, parent.children.get(index2));
        parent.children.set(index2, temp);

        if (fireSwap) {
            parent.fireSwap(parent);
        }
    }

    public static void swap(OutlineItem parent, int index1, int index2) {
        swap(parent, index1, index2, true);
    }

    private static class StableTitleComparitor implements Comparator<OutlineItem> {
        private final Collator collator = Collator.getInstance();

        @Override
        public int compare(OutlineItem item1, OutlineItem item2) {
            // If only the titles are compared, re-sorting the same items could result in itemsMoved being set to true,
            // which could result in the document being saved unnecessarily.

            int result = collator.compare(item1.getTitle(), item2.getTitle());

            if (result == 0)
            {
                result = item1.getUuid().compareTo(item2.getUuid());
            }

            return result;
        }
    }

    public static boolean sort(List<OutlineItem> items) {
        boolean itemsMoved = false;

        final OutlineItem parent = items.getFirst().getParent();

        final List<ImmutablePair<OutlineItem, Integer>> originalItemsWithParentIndex = items
                .stream()
                .map(item -> new ImmutablePair<>(item, parent.getChildren().indexOf(item)))
                .toList();

        items.sort(new StableTitleComparitor());

        for (int i = 0; i < items.size(); i++) {
            final ImmutablePair<OutlineItem, Integer> itemWithParentIndex = originalItemsWithParentIndex.get(i);
            final OutlineItem sortedItem = items.get(i);

            if (itemWithParentIndex.left != sortedItem) {
                parent.getChildren().set(itemWithParentIndex.right, sortedItem);
                itemsMoved = true;
            }
        }

        if (itemsMoved) {
            parent.fireSwap(parent);
        }

        return itemsMoved;
    }

    public static void indent(List<OutlineItem> nodesToIndent) {
        for (OutlineItem nodeToIndent : nodesToIndent) {
            nodeToIndent.indent();
        }

        // Only need to fire the event for the first node, because all of the indented nodes have the same parent.
        final OutlineItem firstNode = nodesToIndent.getFirst();
        firstNode.fireIndent(firstNode);
    }

    private void unindent() {
        final OutlineItem newParent = parent.parent;

        int parentIndex = parent.parent.children.indexOf(parent);

        remove();
        parent.parent.children.add(parentIndex + 1, this);
        this.parent = newParent;
    }

    public static void unindent(List<OutlineItem> nodesToUnindent) {
        for (int i = nodesToUnindent.size() - 1; i >= 0; i--) {
            OutlineItem nodeToUnindent = nodesToUnindent.get(i);

            nodeToUnindent.unindent();
        }

        // Only need to fire the event for the first node, because all of the indented nodes have the same parent.
        OutlineItem firstNode = nodesToUnindent.get(0);
        firstNode.fireUnindent(firstNode);
    }

    private void addChild(OutlineItem childNode, boolean fireEvent, int index) {
        childNode.parent = this;

        if (index == -1) {
            children.add(childNode);
        } else {
            children.add(index, childNode);
        }

        if (fireEvent) {
            fireAdd(childNode);
        }
    }

    public void addChild(OutlineItem childNode) {
        addChild(childNode, true, -1);
    }

    public void addChild(OutlineItem childNode, int index) {
        addChild(childNode, true, index);
    }

    public void addChild(OutlineItem childNode, boolean fireEvent) {
        addChild(childNode, fireEvent, -1);
    }

    public List<OutlineItem> getChildren() {
        return children;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public OutlineItem findNode(UUID uuid) {
        OutlineItem foundNode = null;

        if (uuid.equals(this.uuid)) {
            foundNode = this;
        } else {
            for (OutlineItem childNode : children) {
                OutlineItem tempFoundNode = childNode.findNode(uuid);

                if (tempFoundNode != null) {
                    foundNode = tempFoundNode;
                    break;
                }
            }
        }

        return foundNode;
    }

    public boolean isAncestorOf(OutlineItem node) {
        boolean isAncestor = false;

        OutlineItem currentNode = node;

        while (currentNode != null) {
            if (currentNode.parent == this) {
                isAncestor = true;
                break;
            } else {
                currentNode = currentNode.parent;
            }
        }

        return isAncestor;
    }

    private boolean searchHit(String text, Pattern[] searchPatterns, boolean matchAll) {
        int matches = 0;

        if (text != null) {
            for (Pattern searchPattern : searchPatterns) {
                if (searchPattern.matcher(text).find()) {
                    matches++;

                    if (!matchAll) {
                        break;
                    }
                }
            }
        }

        return ((!matchAll && matches > 0) || (matchAll && matches == searchPatterns.length));
    }

    public List<OutlineItem> search(Pattern[] searchPatterns, boolean matchAll, Search.SearchMode searchMode) {
        final ArrayList<OutlineItem> searchResults = new ArrayList<>();

        boolean hit = false;

        switch (searchMode) {
            case titles: {
                hit = searchHit(getTitle(), searchPatterns, matchAll);
            }
            break;

            case titlesAndText: {
                hit = searchHit(getTitle(), searchPatterns, matchAll) || searchHit(getText(), searchPatterns, matchAll);
            }
            break;
        }

        if (hit) {
            searchResults.add(this);
        }

        for (OutlineItem child : children) {
            searchResults.addAll(child.search(searchPatterns, matchAll, searchMode));
        }

        return searchResults;
    }

    private boolean photoExclusionHit(Pattern[] exclusionPatterns) {
        final boolean matchAll = false;

        return searchHit(getTitle(), exclusionPatterns, matchAll) || searchHit(getText(), exclusionPatterns, matchAll);
    }

    public List<OutlineItem> getPhotos(OutlineItem item, Pattern[] exclusionPatterns) {
        final List<OutlineItem> searchResults = new ArrayList<>();

        boolean exclude = item.photoExclusionHit(exclusionPatterns);

        if (item.photoPath != null && !exclude && PhotoUtils.isPhotoFile(item.photoPath)) {
            searchResults.add(item);
        }

        if (!exclude) {
            item.children.stream().filter(childItem -> !childItem.photoExclusionHit(exclusionPatterns)).forEach(childItem -> searchResults.addAll(getPhotos(childItem, exclusionPatterns)));
        }

        return searchResults;
    }

    public List<OutlineItem> getPhotos(List<OutlineItem> items, Pattern[] exclusionPatterns) {
        final List<OutlineItem> searchResults = new ArrayList<>();

        items.stream().filter(item -> !item.photoExclusionHit(exclusionPatterns)).forEach(item -> searchResults.addAll(getPhotos(item, exclusionPatterns)));

        return searchResults;
    }

    public OutlineItem getNextOutlineItemToSelect() {
        OutlineItem nextSiblingOrParent;

        int index = parent.children.indexOf(this);

        if (index + 1 < parent.children.size()) {
            nextSiblingOrParent = parent.children.get(index + 1);
        } else if (index - 1 >= 0) {
            nextSiblingOrParent = parent.children.get(index - 1);
        } else {
            nextSiblingOrParent = parent;
        }

        return nextSiblingOrParent;
    }
}
