package org.antigen.host;

public class ImmunitySummary {

  private final double[] centroid;
  private final double naiveFraction;
  private final int totalSampled;
  private final int experiencedHosts;

  public ImmunitySummary(
      double[] centroid, double naiveFraction, int totalSampled, int experiencedHosts) {
    this.centroid = centroid;
    this.naiveFraction = naiveFraction;
    this.totalSampled = totalSampled;
    this.experiencedHosts = experiencedHosts;
  }

  public double[] getCentroid() {
    return centroid.clone();
  }

  public double getNaiveFraction() {
    return naiveFraction;
  }

  public int getTotalSampled() {
    return totalSampled;
  }

  public int getExperiencedHosts() {
    return experiencedHosts;
  }

  public boolean hasValidCentroid() {
    return !Double.isNaN(centroid[0]) && !Double.isNaN(centroid[1]);
  }
}
