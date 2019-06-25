// Copyright (C) 2019 BarD Software
package com.bardsoftware.eclipsito.update;

/**
 * @author dbarashev@bardsoftware.com
 */
public class UpdateMetadata implements Comparable<UpdateMetadata> {
  public final String version;
  public final String url;
  public final String description;
  public final String date;
  public final int sizeBytes;

  public UpdateMetadata(String version, String url, String description, String date, int sizeBytes) {
    this.version = version;
    this.url = url;
    this.description = description;
    this.date = date;
    this.sizeBytes = sizeBytes;
  }

  @Override
  public int compareTo(UpdateMetadata that) {
    String[] thisComponents = this.version.split("\\.");
    String[] thatComponents = that.version.split("\\.");
    for (int i = 0; i < Math.min(thisComponents.length, thatComponents.length); i++) {
      var diff = Integer.parseInt(thisComponents[i]) - Integer.parseInt(thatComponents[i]);
      if (diff != 0) {
        return diff;
      }
    }
    return thisComponents.length - thatComponents.length;
  }
}
