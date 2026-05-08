package org.antigen.host;

import static org.junit.Assert.*;

import org.antigen.core.Parameters;
import org.antigen.virus.Virus;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests that the burnin guard in distributeContacts() correctly suppresses fitness computation
 * during burnin and permits it post-burnin.
 */
public class TestHostPopulationBurninGuard {

  @Before
  public void setUp() {
    Parameters.load();
    Parameters.initialize();
    // Single deme, small population for speed
    Parameters.demeCount = 1;
    Parameters.demeNames = new String[] {"test"};
    Parameters.initialNs = new int[] {10};
    Parameters.initialDeme = 1;
    Parameters.initialI = 1;
    Parameters.initialPrR = 0.0;
    Parameters.swapDemography = false;
    Parameters.transcendental = false;
    Parameters.waning = false;
    Parameters.fitnessSampleSize = 5;
    Parameters.deltaT = 1.0;
    // High beta guarantees Poisson contact draw >> 0 so distributeContacts() runs its body
    Parameters.beta = 10000.0;
  }

  /** Fitness must NOT be set on a virus during burnin. */
  @Test
  public void testFitnessNotSetDuringBurnin() {
    Parameters.day = 0.0;
    Parameters.burnin = 100;

    HostPopulation hp = new HostPopulation(0);
    Virus infector = hp.getRandomInfection();
    assertNotNull(infector);
    assertEquals(0.0, infector.getFitness(), 0.0);

    hp.recordContacts();
    hp.distributeContacts();

    assertEquals("Fitness should not be assigned during burnin", 0.0, infector.getFitness(), 0.0);
  }

  /** Fitness MUST be set on a virus post-burnin (day == burnin boundary). */
  @Test
  public void testFitnessSetPostBurnin() {
    Parameters.burnin = 0;
    Parameters.day = 0.0; // day >= burnin, so guard passes

    HostPopulation hp = new HostPopulation(0);
    Virus infector = hp.getRandomInfection();
    assertNotNull(infector);
    assertEquals(0.0, infector.getFitness(), 0.0);

    hp.recordContacts();
    hp.distributeContacts();

    assertTrue("Fitness should be assigned post-burnin", infector.getFitness() > 0.0);
  }
}
