# Output Analysis Guide

antigen-prime generates several output files that capture different aspects of the simulation. This guide explains what each file contains, how to interpret the data, and how to perform common analyses.

## Output File Overview

| File             | Content                   | Primary Use                                                                 |
| ---------------- | ------------------------- | --------------------------------------------------------------------------- |
| `out.timeseries` | Epidemic curves over time | Extracting case counts, attack rates, and other epidemic summary statistics |
| `out.summary`    | Summary statistics        | Overall epidemic characteristics                                            |
| `out.tips`       | Sampled virus data        | Assessing antigenic evolution, post-simulation variant assignment           |
| `out.branches`   | Tree branch structure     | Phylogenetic tree reconstruction with full node metadata                    |
| `out.fasta`      | Virus sequences           | Sequence analysis, alignment, molecular evolution (GeometricSeq only)       |
| `out.tree`       | Newick tree format        | Tree visualization in standard phylogenetic software                        |
| `out.immunity`   | Final immunity landscape  | Population-level immunity snapshot at simulation end (optional)             |
| `out.histories`  | Host immune histories     | Raw per-host immune coordinates for sampled hosts at each sampling interval (optional) |
| `out.histories.csv` | Population immunity centroids | Per-deme average antigenic position of most-recent infections (optional) |

## Core Output Files

### 1. Timeseries Data (`out.timeseries`)

**Purpose:** Tracks epidemic dynamics over time with global and per-deme breakdown

**Format:** Tab-separated values with columns:
```
date  diversity  tmrca  netau  serialInterval  antigenicDiversity  totalN  totalS  totalI  totalR  totalCases  [per-deme columns]
```

**Column Descriptions:**
- `date`: Simulation date in years (calculated from days post-burnin)
- `diversity`: Mean genetic diversity of circulating viruses
- `tmrca`: Time to most recent common ancestor (halved)
- `netau`: Effective population size × generation time
- `serialInterval`: Mean time between successive infections
- `antigenicDiversity`: Mean diversity in antigenic phenotype space
- `totalN`: Total population size across all demes
- `totalS`: Total susceptible individuals
- `totalI`: Total infected individuals  
- `totalR`: Total recovered individuals
- `totalCases`: Total new cases this timestep

**Per-deme columns** (repeated for each deme with deme name prefix):
- `{demeName}Diversity`: Genetic diversity in this deme
- `{demeName}Tmrca`: TMRCA for this deme
- `{demeName}Netau`: Ne×tau for this deme
- `{demeName}SerialInterval`: Serial interval for this deme
- `{demeName}AntigenicDiversity`: Antigenic diversity in this deme
- `{demeName}N`: Population size
- `{demeName}S`: Susceptible count
- `{demeName}I`: Infected count
- `{demeName}R`: Recovered count
- `{demeName}Cases`: New cases


### 2. Summary Statistics (`out.summary`)

**Purpose:** Mean values of key metrics over the simulation period (post-burnin)

**Format:** Two-column tab-separated values:
```
parameter	full
endDate	5.4795
diversity	0.3421
tmrca	1.2345
...
```

**Content includes:**
- `endDate`: Final simulation date in years
- `diversity`: Mean genetic diversity
- `tmrca`: Mean time to most recent common ancestor
- `netau`: Mean effective population size × generation time
- `serialInterval`: Mean serial interval
- `antigenicDiversity`: Mean antigenic diversity
- `N`: Mean total population size
- `S`: Mean susceptible count
- `I`: Mean infected count
- `R`: Mean recovered count
- `cases`: Mean new cases per timestep
- `sideBranchRate`: Mutation rate on side branches (appended)
- `trunkRate`: Mutation rate on trunk lineage (appended)
- `mkRatio`: McDonald-Kreitman ratio (trunk rate / side branch rate) (appended)

### 3. Virus Sample Data (`out.tips`)

**Purpose:** Sample of viruses collected throughout simulation for phylogenetic analysis

**Format:** Tab-separated with columns:
```
sample  date  deme  traitA  traitB  [additional phenotype traits]
```

**Column Descriptions:**
- `sample`: Unique virus identifier
- `date`: Collection date (simulation day)
- `deme`: Geographic origin
- `traitA`, `traitB`: Antigenic phenotype coordinates
- Additional columns for higher-dimensional phenotypes or sequence data


### 4. Phylogenetic Structure (`out.branches`)

**Purpose:** Complete phylogenetic tree structure with detailed metadata for each branch

**Format:** Tab-separated with two virus objects and coverage count per line:
```
{child_virus_object}    {parent_virus_object}    coverage_count
```

Each virus object contains: `{name, birth_time, fitness, trunk_flag, tip_flag, marked_flag, deme, layout, phenotype}`


### 5. Sequence Data (`out.fasta`)

**Purpose:** Virus sequences in standard FASTA format (only for GeometricSeqPhenotype)

**Format:** Standard FASTA with headers containing metadata:
```
>seq0|12.3456|0.9876
ATGCGATCGATCGATCG...
>seq1|12.4567|0.9765
ATGCGATCGATCGATCG...
```

Header format: `>seq{number}|{birth_time}|{fitness}`

## Optional Output Files

### Host Immunity (`out.immunity`)

**Enabled by:** `immunityReconstruction: true`

**Purpose:** Snapshot of population immunity landscape at simulation end

**Format:** CSV grid of infection risk values across phenotype space
- Rows represent x-coordinates in phenotype space
- Columns represent y-coordinates in phenotype space  
- Values are average infection risks (0-1) based on population's immune histories
- Grid samples phenotype space every 0.5 units

### Host Histories (`out.histories` and `out.histories.csv`)

**Enabled by:** `sampleHostImmunity: true`

**Purpose:** Both files are written on the same schedule (`printHostImmunityStep`) from the **same sampled hosts**, so they form a coherent pair — the raw histories are the exact dataset used to compute the centroids.

**`out.histories`** — raw per-host coordinates: for each sampling interval, prints the contact rate and the full immune history coordinates of every sampled host, grouped by deme.

**`out.histories.csv`** — per-deme and global centroids: CSV with columns `year,deme,ag1,ag2,naive_fraction,experienced_hosts`. One row per deme plus a `global` aggregate row. See the [immunity centroids guide](immunity-centroids.md) for full details.

**Number of hosts sampled per deme** is set by `hostImmunitySamplesPerDeme`. Demes with a value of 0 are skipped.

## Interpretation Guidelines

### What Results Mean

**High diversity:** Active evolution, multiple co-circulating strains
**Low diversity:** Genetic bottlenecks, recent population expansion
**Oscillating prevalence:** Seasonal dynamics, antigenic cycling
**Rapid antigenic evolution:** Strong immune pressure, immune escape

### Common Pitfalls

1. **Burn-in effects:** Always exclude burn-in period from analysis
2. **Stochastic noise:** Use multiple replicates for robust conclusions
3. **Parameter sensitivity:** Test key results across parameter ranges
4. **Scale effects:** Results may depend on population size and timestep
