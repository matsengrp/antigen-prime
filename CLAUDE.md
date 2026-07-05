# CLAUDE.md - Project Context for Antigen

## Project Overview
Antigen is an epidemiological simulation framework written in Java that models virus evolution and population dynamics. The project simulates SIR (Susceptible-Infected-Recovered) models with genetic/phenotypic evolution of pathogens.

## Development Workflow

When implementing new features or changes:
1. **Draft Phase**: Create a markdown file to outline the implementation plan
2. **Issue Creation**: Once the plan is finalized, create a GitHub issue using the markdown content
3. **Branch Naming**: Create a new branch following the convention: `<issue-number>-<brief-feature-description>`
   - Example: `42-add-new-visualization`
   - Example: `15-fix-data-pipeline-bug`

This workflow ensures proper documentation and tracking of all changes.

## Maven Project Structure
```
/ (root directory)
├── pom.xml - Maven configuration
├── src/
│   ├── main/
│   │   ├── java/org/antigen/
│   │   │   ├── Antigen.java - Main entry point
│   │   │   ├── core/
│   │   │   │   ├── Simulation.java - Main simulation logic
│   │   │   │   ├── Parameters.java - Configuration parameters
│   │   │   │   └── Random.java - Random number generation
│   │   │   ├── host/
│   │   │   │   ├── Host.java - Individual host modeling
│   │   │   │   └── HostPopulation.java - Population dynamics
│   │   │   ├── virus/
│   │   │   │   ├── Virus.java - Virus entities
│   │   │   │   ├── VirusTree.java - Phylogenetic tree tracking
│   │   │   │   └── Biology.java - Biological constants and utilities
│   │   │   ├── phenotype/
│   │   │   │   ├── Phenotype.java - Interface for phenotype models
│   │   │   │   ├── PhenotypeFactory.java - Creates phenotype instances
│   │   │   │   ├── GeometricPhenotype.java
│   │   │   │   ├── GeometricPhenotype3D.java
│   │   │   │   ├── GeometricPhenotype10D.java
│   │   │   │   └── GeometricSeqPhenotype.java
│   │   │   └── analysis/
│   │   │       └── SimplePCA.java - PCA analysis utilities
│   │   └── resources/
│   │       ├── parameters.yml - Main parameter file
│   │       ├── codon_table.txt - Genetic code reference
│   │       ├── input/ - Additional parameter files
│   │       └── lib/
│   │           └── classmexer.jar - Memory profiling library
│   └── test/
│       └── java/org/antigen/phenotype/
│           └── TestGeometricSeqPhenotype.java - JUnit tests
├── target/ - Maven build output
├── output/, example/ - Simulation output directories
└── Python scripts - Analysis utilities
```

## Build and Run Commands
```bash
# Compile and package using Maven
mvn clean compile

# Run tests
mvn test

# Create JAR with dependencies
mvn package

# This creates two JARs:
# - target/antigen-prime.jar (complete executable with all dependencies)
# - target/antigen-prime-no-dependencies.jar (classes only, requires classpath)

# Run simulation (from root directory)
java -jar target/antigen-prime.jar

# With memory allocation
java -Xmx10G -jar target/antigen-prime.jar

# Run with specific parameters file
java -Xmx10G -jar target/antigen-prime.jar parameters.yml

# For development - compile and run directly
mvn compile exec:java -Dexec.mainClass="org.antigen.Antigen"
```

## Key Configuration
- `src/main/resources/parameters.yml` - Main parameter file (YAML format)
- `src/main/resources/codon_table.txt` - Genetic code reference
- `src/main/resources/input/` - Additional parameter files
- `pom.xml` - Maven build configuration and dependencies

## Output Files Generated
- `*.trees` - Phylogenetic tree structure
- `*.tips` - Tree tip information
- `*.branches` - Branch information
- `*.immunity` - Population immunity states
- `*.histories` - Host infection histories
- `*.timeseries` - Epidemiological dynamics
- `out.summary` - Summary statistics

## External Dependencies (Maven managed)
- SnakeYAML - YAML parsing (managed by Maven)
- CERN Colt library - Scientific computing (managed by Maven) 
- JUnit - Unit testing framework (managed by Maven)
- Classmexer - Memory profiling (bundled in `src/main/resources/lib/` and installed as local Maven dependency)

## Python Scripts
- `clustering.py` - Analysis scripts
- `output_csv.py` - Convert output to CSV
- `setup.py` - Setup utilities

## Project Status
- ✅ Proper Maven directory structure implemented
- ✅ Source and compiled files separated
- ✅ Maven build system configured
- ✅ Dependencies managed via Maven
- ✅ Clear package organization by functionality
- ✅ JUnit test framework integrated

## Documentation Status
- Currently restructuring documentation (see `documentation-restructuring-plan.md`)
- Main documentation in `README.md`
- Additional description in `description.md`

## Notes
- Project uses phenotype abstraction for different evolution models
- Memory management is critical for large simulations
- JUnit test framework configured - tests located in `src/test/java/`
- Java 11 compatible for deployment on remote systems

---

## Repository Ecosystem

This repo is one of three that work together. Sibling repos live at `../antigen-experiments` and `../antigen-forecasting`.

| Repo | Role |
|------|------|
| **antigen-prime** (this repo) | Simulation engine — produces all raw output files |
| **antigen-experiments** | Run drivers, SLURM submission, and processing scripts that reduce raw outputs |
| **antigen-forecasting** | Downstream analysis — variant assignment, growth rate modeling, forecasting |

**Data flow:** `antigen-prime` → `antigen-experiments` → `antigen-forecasting`

---

### Output File Contracts

#### `run-out.tips` — sampled viral tips (CSV, `output/` subdir, `geometricSeq` phenotype)

| Column | Type | Description |
|--------|------|-------------|
| `name` | string | Unique virus identifier |
| `year` | double | Birth year (burn-in-adjusted) |
| `trunk` | int | 1 if on phylogenetic trunk, else 0 |
| `tip` | int | 1 if a sampled tip, else 0 |
| `mark` | int | 1 if marked for analysis window |
| `location` | int | Deme index (0-based; maps to `demeNames` in parameters) |
| `layout` | double | Vertical layout position for tree visualization |
| `nucleotideSequence` | string | Full nucleotide sequence |
| `ag1` | double | First antigenic coordinate |
| `ag2` | double | Second antigenic coordinate |
| `epitopeMutationCount` | int | Cumulative epitope mutations from root |
| `nonepitopeMutationCount` | int | Cumulative non-epitope mutations from root |
| `lowEpitopeMutationCount` | int | Mutations at low-fitness epitope sites |
| `highEpitopeMutationCount` | int | Mutations at high-fitness epitope sites |
| `fitness` | double | Virus fitness score |
| `averageInfectionRisk` | double | Mean infection risk across sampled hosts |
| `probSusceptible` | double | Fraction of population susceptible |
| `demeSeasonality` | double | Seasonal multiplier for virus's deme at sampling time |

#### `out.timeseries` — epidemiological time series (TSV)

Global columns, then per-deme columns repeated for each deme (prefixed by deme name, e.g. `northN`).

| Column | Type | Description |
|--------|------|-------------|
| `date` | double | Simulation day (burn-in-adjusted year) |
| `diversity` | double | Global phylogenetic diversity |
| `tmrca` | double | Time to most recent common ancestor (years) |
| `netau` | double | Effective population size × generation time |
| `serialInterval` | double | Mean serial interval (days) |
| `antigenicDiversity` | double | Mean pairwise antigenic distance among tips |
| `totalN/S/I/R` | int | Global host counts |
| `totalCases` | int | New infections since last print step |
| `{deme}N/S/I/R/Cases` | int | Per-deme equivalents of the above |
| `{deme}Diversity/Tmrca/…` | double | Per-deme equivalents of the phylogenetic stats |

#### `out.summary` — run-level summary statistics (TSV, key-value format)

| Parameter | Type | Description |
|-----------|------|-------------|
| `endDate` | double | Final simulation year |
| `diversity` | double | Time-averaged phylogenetic diversity |
| `tmrca` | double | Time-averaged TMRCA |
| `netau` | double | Time-averaged Ne×tau |
| `serialInterval` | double | Time-averaged serial interval |
| `antigenicDiversity` | double | Time-averaged antigenic diversity |
| `N/S/I/R` | double | Time-averaged host compartment sizes |
| `cases` | double | Time-averaged new infections per step |

#### `out.histories.csv` — population immunity centroids (CSV)

| Column | Type | Description |
|--------|------|-------------|
| `year` | double | Burn-in-adjusted snapshot year |
| `deme` | string | Deme name (or `"global"`) |
| `ag1` | double | Mean ag1 of experienced hosts (centroid) |
| `ag2` | double | Mean ag2 of experienced hosts (centroid) |
| `naive_fraction` | double | Fraction of sampled hosts with no immune history |
| `experienced_hosts` | int | Number of sampled hosts with ≥1 infection |

#### `out.histories.raw.csv` — per-host per-infection records (CSV)

Written every `printHostImmunityStep` days after burnin. Processed by `antigen-experiments/scripts/subsample_histories.py`.

| Column | Type | Description |
|--------|------|-------------|
| `year` | double | Burn-in-adjusted snapshot year |
| `deme` | string | Deme name |
| `host_id` | int | Sequential host index within this snapshot/deme block (resets each snapshot) |
| `infection_index` | int | Position in immune history (0 = oldest infection) |
| `ag1` | double | Antigenic coordinate 1 of this infection |
| `ag2` | double | Antigenic coordinate 2 of this infection |
| `naive_fraction` | double | Fraction of sampled hosts with empty immune history (repeated per row) |