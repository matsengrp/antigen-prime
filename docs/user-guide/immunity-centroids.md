# Host Immunity Centroids

antigen-prime periodically samples host population immunity and outputs centroids representing the average antigenic position of recent infections. These centroids enable downstream fitness calculations for benchmarking growth-advantage models.

## Overview

Each host maintains an immune history from previous infections. The immunity centroid captures the average antigenic position of the population's most recent infections, providing a summary of population-level immunity at each time point.

## Enabling Centroid Output

| Parameter | Default | Description |
|-----------|---------|-------------|
| `sampleHostImmunity` | false | Enable periodic immunity sampling |
| `printHostImmunityStep` | 365 | Sampling interval in days |
| `hostImmunitySamplesPerDeme` | [10000, 10000, 10000] | Hosts sampled per deme |

## Output Files

When `sampleHostImmunity: true`, antigen-prime writes two files on each `printHostImmunityStep` interval from the **same sampled hosts**:

- **`out.histories.csv`** — per-deme and global centroids (described below)
- **`out.histories`** — full immune history coordinates for every sampled host, grouped by deme; useful for reviewing the raw data behind the centroids

### `out.histories.csv` columns

| Column | Description |
|--------|-------------|
| `year` | Simulation time in years |
| `deme` | Geographic deme name or "global" for global aggregate |
| `ag1` | Centroid x-coordinate in antigenic space |
| `ag2` | Centroid y-coordinate in antigenic space |
| `naive_fraction` | Fraction of sampled hosts with no immune history |
| `experienced_hosts` | Count of hosts with at least one prior infection |

Example output:
```csv
year,deme,ag1,ag2,naive_fraction,experienced_hosts
0.0000,north,-6.000000,0.000000,0.4981,5019
0.0000,tropics,-6.000000,0.000000,0.4968,5032
0.0000,south,-6.000000,0.000000,0.5070,4930
0.0000,global,-6.000000,0.000000,0.5006,14981
1.0000,north,-2.537124,-0.014044,0.2117,7883
```

## Centroid Calculation

For each deme at each sampling time:

1. Sample `n` random hosts from the population
2. For each host with immune history, extract coordinates of **most recent infection**
3. Compute centroid as average of these coordinates:

$$\bar{c}_t = \frac{1}{n_{exp}} \sum_{i=1}^{n_{exp}} h_i^{(\text{recent})}$$

Where:
- $n_{exp}$ = number of experienced (non-naive) hosts
- $h_i^{(\text{recent})}$ = antigenic coordinates of host $i$'s most recent infection

The global (`global`) centroid is computed as a weighted average across demes, weighted by number of experienced hosts.

## Using Centroids for Fitness Calculation

Virus fitness can be approximated using distance to the population immunity centroid:

$$\text{fitness}(v) = \min(1, ||v - \bar{c}_t|| \times \sigma)$$

Where:
- $v$ = virus antigenic coordinates (ag1, ag2)
- $\bar{c}_t$ = population immunity centroid at time $t$
- $\sigma$ = `smithConversion` parameter (default 0.1)

Viruses antigenically distant from the centroid have higher fitness (higher infection risk), reflecting immune escape.

## Example Configuration

```yaml
# Sample 10,000 hosts per deme annually
sampleHostImmunity: true
printHostImmunityStep: 365
hostImmunitySamplesPerDeme: [10000, 10000, 10000]

# Cross-immunity parameters (for fitness calculation)
smithConversion: 0.1
homologousImmunity: 0.05
```

## Implementation

Key source files:

- `src/main/java/org/antigen/core/Simulation.java` - `printPopulationImmunityCentroids()`
- `src/main/java/org/antigen/host/HostPopulation.java` - `getPopulationImmunitySummary()`
- `src/main/java/org/antigen/host/Host.java` - `getImmunityCoordinatesCentroid()`
- `src/main/java/org/antigen/host/ImmunitySummary.java` - Data class for centroid results

## References

- Smith DJ et al. (2004). Mapping the antigenic and genetic evolution of influenza virus. Science.
- Luksza M, Lassig M. (2014). A predictive fitness model for influenza. Nature.
