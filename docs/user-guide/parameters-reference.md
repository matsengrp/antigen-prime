# Parameters Reference Guide

This guide explains all parameters available in antigen-prime, organized by functional category. Parameters are configured in the `src/main/resources/parameters.yml` file, which overrides default values defined in the code.

## Simulation Control Parameters

### Basic Simulation Settings

| Parameter | Default | Description |
|-----------|---------|-------------|
| `burnin` | 0 | Days before logging output and fitness computation (allows system to reach equilibrium) |
| `endDay` | 5000 | Total number of days to simulate |
| `deltaT` | 0.1 | Time step size in days (0.1 = 2.4 hours per step) |
| `printStep` | 10 | Output frequency - write to timeseries every N days |
| `repeatSim` | true | Whether to repeat simulation until endDay is reached if population dies out |

**Usage Notes:**
- Use `burnin > 0` to exclude initial transient dynamics from output; internal-node fitness (`getAverageRisk`) is also skipped during burnin, reducing computation cost
- When `burnin == 0` fitness computation proceeds immediately (identical to pre-burnin behavior)
- Host immunity sampling (`sampleHostImmunity`) also respects `burnin`: no immunity output is written until `day >= burnin`, and the `year` column in `out.histories.csv` is burn-in-adjusted (`year = 0.0` at end of burnin)
- Smaller `deltaT` gives more accurate results but increases computation time
- `printStep` affects file size - smaller values create larger output files

### Output Control

| Parameter | Default | Description |
|-----------|---------|-------------|
| `outPath` | "output/" | Directory path for output files |
| `outPrefix` | "run-" | Prefix for all output filenames |
| `reducedOutput` | false | Output only summary and timeseries (minimal files) |
| `detailedOutput` | false | Include detailed host and virus files (enables checkpointing) |
| `restartFromCheckpoint` | false | Load population state from previous detailed output |

### Memory and Performance

| Parameter | Default | Description |
|-----------|---------|-------------|
| `memoryProfiling` | false | Enable memory usage tracking (requires classmexer.jar) |

## Population Structure Parameters

### Metapopulation Settings

| Parameter | Default | Description |
|-----------|---------|-------------|
| `demeCount` | 3 | Number of geographic demes (populations) |
| `demeNames` | ["north", "tropics", "south"] | Names for each deme |
| `initialNs` | [1000000, 1000000, 1000000] | Initial population size for each deme |

### Migration Between Demes

| Parameter | Default | Description |
|-----------|---------|-------------|
| `betweenDemePro` | 0.0005 | Fraction of contacts that occur between demes |

**Example:** With `betweenDemePro = 0.0005`, contacts between demes occur at 0.05% the rate of within-deme contacts.

## Host Demographics

### Birth and Death Rates

| Parameter | Default | Description |
|-----------|---------|-------------|
| `birthRate` | 0.000091 | Births per individual per day (≈30 year lifespan) |
| `deathRate` | 0.000091 | Deaths per individual per day (≈30 year lifespan) |
| `swapDemography` | true | Maintain constant population size by balancing births/deaths |

**Lifespan Calculation:** `1/deathRate` days = lifespan. Default 0.000091 = ~30 years.

## Epidemiological Parameters

### Transmission Dynamics

| Parameter | Default | Description |
|-----------|---------|-------------|
| `beta` | 0.36 | Contact rate - contacts per individual per day |
| `nu` | 0.2 | Recovery rate - recoveries per individual per day |

**Key Relationships:**
- **Infectious period**: `1/nu` days (default: 5 days)
- **Basic reproduction number (R₀)**: Approximately `beta/nu` in naive population

### Initial Conditions  

| Parameter | Default | Description |
|-----------|---------|-------------|
| `initialI` | 10 | Number of initially infected individuals |
| `initialDeme` | 2 | Index of deme where infection starts (1-indexed) |
| `initialPrR` | 0.5 | Initial proportion of population with immunity |

### Transcendental Immunity (Optional)

| Parameter | Default | Description |
|-----------|---------|-------------|
| `transcendental` | false | Include general recovered class (non-strain-specific immunity) |
| `immunityLoss` | 0.01 | Rate of immunity loss (R→S per individual per day) |
| `initialPrT` | 0.1 | Initial fraction in general recovered class |

## Seasonal Transmission

### Seasonal Patterns by Deme

| Parameter | Default | Description |
|-----------|---------|-------------|
| `demeBaselines` | [1, 1, 1] | Baseline transmission multiplier for each deme |
| `demeAmplitudes` | [0.1, 0, 0.1] | Seasonal amplitude for each deme |
| `demeOffsets` | [0, 0, 0.5] | Seasonal phase offset (fraction of year) |

**Seasonal Formula:**
```
Effective beta = beta × [baseline + amplitude × cos(2π × year + 2π × offset)]
```

**Example:** Northern and southern demes have opposite seasonal patterns (offset = 0 vs 0.5).

## Virus Evolution Parameters

### Phenotype Models

| Parameter | Default | Description |
|-----------|---------|-------------|
| `phenotypeSpace` | "geometric" | Phenotype model: "geometric", "geometric3d", "geometric10d", "geometricSeq" |
| `muPhenotype` | 0.005 | Mutation rate per virus per day |

### Immune Dynamics

| Parameter | Default | Description |
|-----------|---------|-------------|
| `waning` | false | Allow immunity to wane over time |
| `waningRate` | 0.01 | Rate of losing random immune memory per day |

### Geometric Phenotype Parameters

| Parameter | Default | Description |
|-----------|---------|-------------|
| `smithConversion` | 0.1 | Multiplier to convert antigenic distance to immunity |
| `homologousImmunity` | 0.05 | Immunity against identical virus |
| `initialTraitA` | -6 | Initial virus position in dimension 1 |

### Mutation Step Sizes

| Parameter | Default | Description |
|-----------|---------|-------------|
| `meanStep` | 0.3 | Mean mutation step size for non-epitope sites |
| `sdStep` | 0.3 | Standard deviation of mutation steps for non-epitope sites |
| `mut2D` | false | Allow mutations in full 360° arc (vs. 1D only) |
| `fixedStep` | false | Use fixed step size (ignore `sdStep`) |

## Sequence-Based Phenotype Parameters

### Sequence Configuration

| Parameter | Default | Description |
|-----------|---------|-------------|
| `startingSequence` | "startingSequence.fasta" | FASTA file with initial virus sequence |
| `epitopeSites` | "epitopeSites.txt" | File listing epitope sites (1-indexed) |

### Mutation Parameters

| Parameter | Default | Description |
|-----------|---------|-------------|
| `meanStepEpitope` | 0.3 | Mean mutation step size for epitope sites |
| `sdStepEpitope` | 0.3 | Standard deviation for epitope mutations |
| `transitionTransversionRatio` | 5.0 | Bias toward transitions vs transversions |
| `nonEpitopeAcceptance` | 1.0 | Probability of accepting non-epitope mutations |
| `epitopeAcceptance` | 1.0 | Probability of accepting epitope mutations |

### Advanced Epitope Options

| Parameter | Default | Description |
|-----------|---------|-------------|
| `proportionHighSites` | 0.2 | Fraction of epitope sites with high mutation rates |
| `meanStepEpitopeLow` | 0.3 | Mean step size for "low" epitope sites |
| `sdStepEpitopeLow` | 0.3 | Standard deviation for "low" epitope sites |
| `meanStepEpitopeHigh` | 0.3 | Mean step size for "high" epitope sites |
| `sdStepEpitopeHigh` | 0.3 | Standard deviation for "high" epitope sites |

### DMS Integration (Optional)

| Parameter | Default | Description |
|-----------|---------|-------------|
| `DMSFile` | null | CSV file with Deep Mutational Scanning fitness data |

**DMS File Format:** Must have 21 columns (site number + 20 amino acid preferences) with one row per amino acid site.

## Sampling and Analysis Parameters

### Virus Sampling

| Parameter | Default | Description |
|-----------|---------|-------------|
| `tipSamplingRate` | 0.0002 | Samples collected per deme per day |
| `tipSamplesPerDeme` | 1000 | Maximum samples stored per deme |
| `tipSamplingProportional` | true | Sample proportional to prevalence vs. uniform |
| `treeProportion` | 0.1 | Fraction of samples used in phylogenetic reconstruction |

### Diversity Analysis

| Parameter | Default | Description |
|-----------|---------|-------------|
| `diversitySamplingCount` | 1000 | Samples for calculating diversity statistics |
| `netauWindow` | 100 | Window size (days) for Ne×τ calculation |
| `yearsFromMK` | 1.0 | Time window for Muller-Kreitman analysis |
| `pcaSamples` | false | Apply PCA rotation to virus tree |

### Host Immunity Sampling

| Parameter | Default | Description |
|-----------|---------|-------------|
| `sampleHostImmunity` | false | Record host immunity throughout simulation |
| `printHostImmunityStep` | 100 | Frequency of immunity sampling (days) |
| `hostImmunitySamplesPerDeme` | [100, 100, 100] | Number of hosts sampled per deme |
| `fitnessSampleSize` | 10000 | Hosts sampled for fitness calculations |

### Memory Analysis

| Parameter | Default | Description |
|-----------|---------|-------------|
| `immunityReconstruction` | false | Output detailed immunity reconstruction |

## Parameter Configuration Tips

### Common Research Scenarios

**Short-term epidemic (seasonal flu)**:
```yaml
endDay: 365
burnin: 50
beta: 0.4
nu: 0.2
```

**Long-term evolution study**:
```yaml
endDay: 7300  # 20 years
burnin: 365   # 1 year
muPhenotype: 0.01
```

**Large population study**:
```yaml
initialNs: [10000000, 5000000, 10000000]  # 25M total
tipSamplingRate: 0.00001  # Reduce sampling
```

### Parameter Relationships

**Memory Usage:** ∝ `sum(initialNs)` × average immune history length
**Computation Time:** ∝ `endDay/deltaT` × `sum(initialNs)`
**Output Size:** ∝ `endDay/printStep` × sampling rates

### Validation Checks

The model validates parameters on startup:
- Sequence length must be multiple of 3
- Epitope sites must be ≤ sequence length / 3
- DMS data rows must match amino acid sequence length
- No stop codons except at sequence end

### Performance Recommendations

**For faster simulations:**
- Increase `deltaT` (0.2-0.5)
- Increase `printStep` 
- Reduce sampling rates
- Use smaller populations

**For higher accuracy:**
- Decrease `deltaT` (0.05-0.1)
- Increase `diversitySamplingCount`
- Use longer `burnin` periods