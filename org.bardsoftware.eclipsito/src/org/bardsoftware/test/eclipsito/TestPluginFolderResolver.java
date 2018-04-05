/*
Copyright 2018 Oleksii Lapinskyi, BarD Software s.r.o

This file is part of GanttProject, an opensource project management tool.

GanttProject is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

GanttProject is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with GanttProject.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.bardsoftware.test.eclipsito;

import org.bardsoftware.impl.eclipsito.BootImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TestPluginFolderResolver extends TestsEclipsitoBase {

  private BootImpl boot;
  File folder1, folder2, folder3;

  public void setUp() {
    boot = new BootImpl();

    folder1 = new File("modules/pluginFolders/directory1");
    folder2 = new File("modules/pluginFolders/directory2");
    folder3 = new File("modules/pluginFolders/directoryEmpty");
  }

  public void testSelectHighestVersion() {
    List<File> list = new ArrayList<>();
    list.add(folder1);
    list.add(folder2);

    File result = boot.getVersionDir(list);

    assertNotNull(result);
    assertEquals(result.getPath(), "modules/pluginFolders/directory2/3.0.1");
  }

  public void testSelectWithEmptyFolder() {
    List<File> list = new ArrayList<>();
    list.add(folder1);
    list.add(folder3);

    File result = boot.getVersionDir(list);

    assertNotNull(result);
    assertEquals(result.getPath(), "modules/pluginFolders/directory1/3.0.0");
  }

  public void testSelectOnlyEmptyFolders() {
    List<File> list = new ArrayList<>();
    list.add(folder3);
    list.add(folder3);

    try {
      File result = boot.getVersionDir(list);
    } catch (AssertionError e) {
      assertEquals(e.getMessage(), "No plugin folder found");
    }
  }

  public void testNoFolders() {
    File result = boot.getVersionDir(new ArrayList<>());

    assertNull(result);
  }

}
