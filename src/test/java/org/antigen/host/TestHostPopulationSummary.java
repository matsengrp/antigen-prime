package org.antigen.host;

import static org.junit.Assert.*;

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
  public void testMixedPopulation() {
    for (int i = 0; i < 5; i++) {
      Host host = population.getRandomHost();
      host.addToHistory(new GeometricPhenotype(i * 1.0, i * 2.0));
    }

    ImmunitySummary summary = population.getPopulationImmunitySummary(20);

    assertTrue(summary.getNaiveFraction() > 0.0);
    assertTrue(summary.getNaiveFraction() < 1.0);
    assertTrue(summary.getExperiencedHosts() > 0);
    assertEquals(20, summary.getTotalSampled());
    if (summary.hasValidCentroid()) {
      assertFalse(Double.isNaN(summary.getCentroid()[0]));
      assertFalse(Double.isNaN(summary.getCentroid()[1]));
    }
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
}
