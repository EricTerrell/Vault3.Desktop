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

import mainPackage.OutlineItem;
import org.junit.Assert;
import org.junit.Test;

public class OutlineItemTests extends BaseTests {
    @Test
    public void testSort() {
        final var rootOutlineItem = new OutlineItem();

        final var child1 = new OutlineItem();
        child1.setTitle("ZZZ");

        final var child2 = new OutlineItem();
        child2.setTitle("MMM");

        final var child3 = new OutlineItem();
        child3.setTitle("AAA");

        rootOutlineItem.addChild(child1);
        rootOutlineItem.addChild(child2);
        rootOutlineItem.addChild(child3);

        OutlineItem.sort(rootOutlineItem.getChildren());

        Assert.assertEquals(child3.getTitle(), rootOutlineItem.getChildren().getFirst().getTitle());
        Assert.assertEquals(child2.getTitle(), rootOutlineItem.getChildren().get(1).getTitle());
        Assert.assertEquals(child1.getTitle(), rootOutlineItem.getChildren().get(2).getTitle());
    }
}
