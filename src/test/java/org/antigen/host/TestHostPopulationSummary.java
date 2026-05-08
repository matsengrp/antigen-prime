package org.antigen.host;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import org.antigen.core.Parameters;
import org.antigen.phenotype.GeometricPhenotype;
import org.junit.Before;
import org.junit.Test;

public class TestHostPopulationSummary {

  private HostPopulation population;

  @Before
  public void setUp() {
    Parameters.load();
    Parameters.initialize();
    Parameters.demeCount = 1;
    Parameters.demeNames = new String[] {"test"};
    Parameters.initialNs = new int[] {100};
    Parameters.initialPrR = 0.0;
    Parameters.swapDemography = false;
    Parameters.transcendental = false;
    Parameters.waning = false;
    population = new HostPopulation(0);
  }

  @Test
  public void testAllNaivePopulation() {
    ImmunitySummary summary = population.getPopulationImmunitySummary(10);

    assertEquals(1.0, summary.getNaiveFraction(), 1e-10);
    assertEquals(0, summary.getExperiencedHosts());
    assertEquals(10, summary.getTotalSampled());
    assertFalse(summary.hasValidCentroid());
    assertTrue(Double.isNaN(summary.getCentroid()[0]));
    assertTrue(Double.isNaN(summary.getCentroid()[1]));
  }

  @Test
  public void testExactCentroid() {
    Host h1 = new Host();
    Host h2 = new Host();
    Host h3 = new Host();
    h1.addToHistory(new GeometricPhenotype(0.0, 0.0));
    h2.addToHistory(new GeometricPhenotype(2.0, 4.0));
    h3.addToHistory(new GeometricPhenotype(4.0, 8.0));

    List<Host> sampled = Arrays.asList(h1, h2, h3);
    ImmunitySummary summary = population.getPopulationImmunitySummary(sampled);

    assertEquals(0.0, summary.getNaiveFraction(), 1e-10);
    assertEquals(3, summary.getExperiencedHosts());
    assertTrue(summary.hasValidCentroid());
    assertEquals(2.0, summary.getCentroid()[0], 1e-10); // (0+2+4)/3
    assertEquals(4.0, summary.getCentroid()[1], 1e-10); // (0+4+8)/3
  }

  @Test
  public void testExactCentroidWithNaiveHosts() {
    Host h1 = new Host();
    Host h2 = new Host();
    Host h3 = new Host(); // naive
    h1.addToHistory(new GeometricPhenotype(1.0, 2.0));
    h2.addToHistory(new GeometricPhenotype(3.0, 6.0));

    List<Host> sampled = Arrays.asList(h1, h2, h3);
    ImmunitySummary summary = population.getPopulationImmunitySummary(sampled);

    assertEquals(1.0 / 3.0, summary.getNaiveFraction(), 1e-10);
    assertEquals(2, summary.getExperiencedHosts());
    assertTrue(summary.hasValidCentroid());
    assertEquals(2.0, summary.getCentroid()[0], 1e-10); // (1+3)/2
    assertEquals(4.0, summary.getCentroid()[1], 1e-10); // (2+6)/2
  }

  @Test
  public void testImmunitySummaryProperties() {
    ImmunitySummary summary = new ImmunitySummary(new double[] {1.0, 2.0}, 0.3, 100, 70);

    assertEquals(1.0, summary.getCentroid()[0], 1e-10);
    assertEquals(2.0, summary.getCentroid()[1], 1e-10);
    assertEquals(0.3, summary.getNaiveFraction(), 1e-10);
    assertEquals(100, summary.getTotalSampled());
    assertEquals(70, summary.getExperiencedHosts());
    assertTrue(summary.hasValidCentroid());
  }

  @Test
  public void testNaNHandling() {
    ImmunitySummary summary =
        new ImmunitySummary(new double[] {Double.NaN, Double.NaN}, 1.0, 50, 0);

    assertFalse(summary.hasValidCentroid());
    assertTrue(Double.isNaN(summary.getCentroid()[0]));
    assertTrue(Double.isNaN(summary.getCentroid()[1]));
  }

  @Test
  public void testEmptySampleList() {
    ImmunitySummary summary = population.getPopulationImmunitySummary(List.of());

    assertEquals(0, summary.getTotalSampled());
    assertEquals(0, summary.getExperiencedHosts());
    assertTrue(Double.isNaN(summary.getCentroid()[0]));
  }
}
