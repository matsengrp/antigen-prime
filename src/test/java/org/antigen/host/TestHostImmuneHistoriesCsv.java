package org.antigen.host;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import org.antigen.core.Parameters;
import org.antigen.phenotype.GeometricPhenotype;
import org.antigen.phenotype.Phenotype;
import org.junit.Before;
import org.junit.Test;

/** Tests for HostPopulation.printHostImmuneHistoriesCsv. */
public class TestHostImmuneHistoriesCsv {

  private HostPopulation population;
  private static final String HEADER = "year,deme,host_id,infection_index,ag1,ag2,naive_fraction";

  @Before
  public void setUp() {
    Parameters.load();
    Parameters.initialize();
    Parameters.demeCount = 1;
    Parameters.demeNames = new String[] {"north"};
    Parameters.initialNs = new int[] {100};
    Parameters.initialPrR = 0.0;
    Parameters.swapDemography = false;
    Parameters.transcendental = false;
    Parameters.waning = false;
    population = new HostPopulation(0);
  }

  private String capture(List<Host> hosts, double year, double naiveFraction, boolean writeHeader) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    population.printHostImmuneHistoriesCsv(ps, hosts, year, naiveFraction, writeHeader);
    ps.flush();
    return baos.toString();
  }

  @Test
  public void testHeaderWrittenWhenRequested() {
    List<Host> hosts = List.of();
    String out = capture(hosts, 5.0, 1.0, true);
    assertTrue("header must be first line", out.startsWith(HEADER));
  }

  @Test
  public void testHeaderOmittedWhenNotRequested() {
    List<Host> hosts = List.of();
    String out = capture(hosts, 5.0, 1.0, false);
    assertFalse("no header when writeHeader=false", out.contains(HEADER));
    assertEquals("output must be empty", "", out.trim());
  }

  @Test
  public void testAllNaiveProducesNoDataRows() {
    Host h1 = new Host();
    Host h2 = new Host();
    List<Host> hosts = Arrays.asList(h1, h2);
    String out = capture(hosts, 5.0, 1.0, true);
    String[] lines = out.trim().split("\\r?\\n");
    // only the header line
    assertEquals(1, lines.length);
    assertEquals(HEADER, lines[0]);
  }

  @Test
  public void testSingleExperiencedHostSingleInfection() {
    Host h = new Host();
    h.addToHistory(new GeometricPhenotype(1.23, 4.56));
    List<Host> hosts = List.of(h);
    String out = capture(hosts, 5.0, 0.12, true);
    String[] lines = out.trim().split("\\r?\\n");
    assertEquals(2, lines.length); // header + 1 data row
    String row = lines[1];
    String[] cols = row.split(",");
    assertEquals(7, cols.length);
    assertEquals("5.0000", cols[0]);
    assertEquals("north", cols[1]);
    assertEquals("0", cols[2]); // host_id
    assertEquals("0", cols[3]); // infection_index
    assertEquals(1.23, Double.parseDouble(cols[4]), 1e-5);
    assertEquals(4.56, Double.parseDouble(cols[5]), 1e-5);
    assertEquals(0.12, Double.parseDouble(cols[6]), 1e-4);
  }

  @Test
  public void testSingleExperiencedHostMultipleInfections() {
    Host h = new Host();
    h.addToHistory(new GeometricPhenotype(1.0, 2.0));
    h.addToHistory(new GeometricPhenotype(3.0, 4.0));
    List<Host> hosts = List.of(h);
    String out = capture(hosts, 6.0, 0.0, true);
    String[] lines = out.trim().split("\\r?\\n");
    assertEquals(3, lines.length); // header + 2 rows
    // infection_index 0
    assertEquals("0", lines[1].split(",")[3]);
    assertEquals(1.0, Double.parseDouble(lines[1].split(",")[4]), 1e-5);
    // infection_index 1
    assertEquals("1", lines[2].split(",")[3]);
    assertEquals(3.0, Double.parseDouble(lines[2].split(",")[4]), 1e-5);
  }

  @Test
  public void testMixedNaiveAndExperiencedHostIds() {
    Host naive = new Host();
    Host experienced = new Host();
    experienced.addToHistory(new GeometricPhenotype(2.0, 5.0));
    // naive at index 0, experienced at index 1 → host_id for experienced row must be 1
    List<Host> hosts = Arrays.asList(naive, experienced);
    String out = capture(hosts, 5.0, 0.5, true);
    String[] lines = out.trim().split("\\r?\\n");
    assertEquals(2, lines.length); // header + 1 row for experienced host
    assertEquals("1", lines[1].split(",")[2]); // host_id = 1 (naive was 0)
  }

  @Test
  public void testNaiveFractionRepeatedPerRow() {
    Host h1 = new Host();
    Host h2 = new Host();
    h1.addToHistory(new GeometricPhenotype(0.0, 0.0));
    h2.addToHistory(new GeometricPhenotype(1.0, 1.0));
    List<Host> hosts = Arrays.asList(h1, h2);
    double naiveFraction = 0.333;
    String out = capture(hosts, 7.0, naiveFraction, true);
    String[] lines = out.trim().split("\\r?\\n");
    assertEquals(3, lines.length);
    assertEquals(naiveFraction, Double.parseDouble(lines[1].split(",")[6]), 1e-4);
    assertEquals(naiveFraction, Double.parseDouble(lines[2].split(",")[6]), 1e-4);
  }

  @Test
  public void testContactRateNotInOutput() {
    Host h = new Host();
    h.addToHistory(new GeometricPhenotype(1.0, 2.0));
    String out = capture(List.of(h), 5.0, 0.0, true);
    assertFalse("contactRate must not appear in CSV output", out.contains("contactRate"));
  }

  @Test
  public void testMultipleHostsCorrectHostIds() {
    Host h0 = new Host();
    Host h1 = new Host();
    Host h2 = new Host();
    h0.addToHistory(new GeometricPhenotype(0.0, 0.0));
    h1.addToHistory(new GeometricPhenotype(1.0, 1.0));
    h2.addToHistory(new GeometricPhenotype(2.0, 2.0));
    List<Host> hosts = Arrays.asList(h0, h1, h2);
    String out = capture(hosts, 5.0, 0.0, false);
    String[] lines = out.trim().split("\\r?\\n");
    assertEquals(3, lines.length);
    assertEquals("0", lines[0].split(",")[2]);
    assertEquals("1", lines[1].split(",")[2]);
    assertEquals("2", lines[2].split(",")[2]);
  }

  @Test(expected = IllegalStateException.class)
  public void testBoundsGuardThrowsOnSubDimensionalPhenotype() {
    // A phenotype returning fewer than 2 coordinates must fail loudly, not silently truncate.
    Phenotype zeroD =
        new Phenotype() {
          public double riskOfInfection(Phenotype[] h) { return 0; }
          public Phenotype mutate() { return this; }
          public double distance(Phenotype p) { return 0; }
          public double[] getCoordinates() { return new double[0]; }
          public String toString() { return ""; }
        };
    Host h = new Host();
    h.addToHistory(zeroD);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    population.printHostImmuneHistoriesCsv(new PrintStream(baos), List.of(h), 5.0, 0.0, false);
  }
}
