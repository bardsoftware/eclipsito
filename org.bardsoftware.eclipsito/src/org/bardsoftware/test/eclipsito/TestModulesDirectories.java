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

import org.bardsoftware.eclipsito.Boot;

public class TestModulesDirectories extends TestsEclipsitoBase {


  public void testModulesPathsSimple() {
    String modules = "/plugins:~/.gantproject.d/plugins";
    String[] pathArray = Boot.getModulesPaths(modules);

    assertEquals(pathArray.length, 2);
    assertEquals(pathArray[0], "/plugins");
    assertEquals(pathArray[1], System.getProperty("user.home") + "/.gantproject.d/plugins");
  }

  public void testModulesPathsReplaceOnlyFirst() {
    String modules = "/plugins:~/.gantproject.d/~/plugins";
    String[] pathArray = Boot.getModulesPaths(modules);

    assertEquals(pathArray.length, 2);
    assertEquals(pathArray[0], "/plugins");
    assertEquals(pathArray[1], System.getProperty("user.home") + "/.gantproject.d/~/plugins");
  }

  public void testModulesPathsNoReplace() {
    String modules = "/~plugins/test:/test~/.gantproject.d/~/plugins";
    String[] pathArray = Boot.getModulesPaths(modules);

    assertEquals(pathArray.length, 2);
    assertEquals(pathArray[0], "/~plugins/test");
    assertEquals(pathArray[1], "/test~/.gantproject.d/~/plugins");
  }
}
