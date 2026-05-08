package org.antigen.core;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests that immunity CSV output uses burn-in-adjusted time and is only written post-burnin.
 *
 * <p>Bug: writeImmunityOutputs used Parameters.day / 365.0 (absolute) instead of
 * Parameters.getDate() ((day - burnin) / 365.0), so output years were inconsistent with virus tip
 * dates which use getDate().
 */
public class TestSimulationImmunityBurnin {

  private Simulation sim;
  private ByteArrayOutputStream csvOut;
  private PrintStream csvStream;
  private PrintStream rawStream;

  @Before
  public void setUp() {
    Parameters.load();
    Parameters.initialize();
    Parameters.demeCount = 1;
    Parameters.demeNames = new String[] {"test"};
    Parameters.initialNs = new int[] {10};
    Parameters.initialPrR = 0.0;
    Parameters.swapDemography = false;
    Parameters.transcendental = false;
    Parameters.waning = false;
    Parameters.sampleHostImmunity = true;
    Parameters.hostImmunitySamplesPerDeme = new int[] {5};
    Parameters.burnin = 3650;

    csvOut = new ByteArrayOutputStream();
    csvStream = new PrintStream(csvOut);
    rawStream = new PrintStream(new ByteArrayOutputStream());
    sim = new Simulation();
  }

  /**
   * Year in CSV must be 0.0 at the burnin boundary, not burnin/365 (~10.0). Fails before fix
   * because year = Parameters.day / 365.0 would give 10.0.
   */
  @Test
  public void testYearAtBurninBoundaryIsZero() {
    Parameters.day = Parameters.burnin;
    sim.writeImmunityOutputs(csvStream, rawStream, true);
    csvStream.flush();

    double year = parseFirstDataRowYear(csvOut.toString());
    assertEquals("Year at burnin boundary must be 0.0", 0.0, year, 1e-4);
  }

  /**
   * Year in CSV must be 1.0 exactly one year after burnin. Fails before fix because year =
   * Parameters.day / 365.0 would give ~11.0.
   */
  @Test
  public void testYearOneYearPostBurninIsOne() {
    Parameters.day = Parameters.burnin + 365.0;
    sim.writeImmunityOutputs(csvStream, rawStream, true);
    csvStream.flush();

    double year = parseFirstDataRowYear(csvOut.toString());
    assertEquals("Year one year post-burnin must be 1.0", 1.0, year, 1e-4);
  }

  /**
   * Confirms that getDate() and absolute day/365 diverge before burnin, justifying the caller guard
   * added to the simulation loop.
   */
  @Test
  public void testGetDateDiffersFromAbsoluteYearBeforeBurnin() {
    Parameters.day = 0.0;
    double absoluteYear = Parameters.day / 365.0;
    double adjustedYear = Parameters.getDate();

    assertNotEquals(
        "Absolute and adjusted year must differ pre-burnin", absoluteYear, adjustedYear, 1e-4);
    assertTrue("getDate() before burnin must be negative", adjustedYear < 0.0);
  }

  private double parseFirstDataRowYear(String csv) {
    String[] lines = csv.split("\n");
    assertTrue("CSV must have header + at least one data row", lines.length >= 2);
    return Double.parseDouble(lines[1].split(",")[0]);
  }
}
