/*
Copyright 2020 BarD Software s.r.o

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
package com.bardsoftware.eclipsito.update;

import java.io.File;

/**
 * Checks the integrity of the downloaded data file, e.g. by verifying its PGP signature
 * against the available public key.
 *
 * It is assumed that the client which issues installUpdate request knows the metadata which
 * are required to run the verification.
 *
 * @author dbarashev@bardsoftware.com
 */
public interface UpdateIntegrityChecker {
  /**
   * @param dataFile the data file to be verified
   * @return true if verification is successful, false otherwise
   */
  boolean verify(File dataFile);
}
