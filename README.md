
# Version of basic genotype QC pipeline from ATLAS, but modified for Hoffman

More detailed notes on each step is here: https://docs.google.com/document/d/1qBDt8qWsCKv4bsZtN-4U5YsriftCxnpnKG55XM9mRWI/edit?usp=sharing# basic_genoeypte_qc_pipeline

# Setup
python3 (I use anaconda)
`fire` library (`pip install fire --user`)

# Running the pipeline

## Setting up the configuration file

Each step can be directly run by just calling a specific configuration file. This makes it straightforward to run multiple analyses with different sets of data and different parameters. 

The confirguation file is a text file with the following fields. Note that the space after the ':' is not needed and will be ignored. The current sample configuration file will run the sample data example. 

```
new_files: sample_data/new_files.txt
outdir: results/
SNP_missing_thresh: 0.05
ind_missing_thresh: 0.05
new_prefix: sample
contaminated_ids: sample_data/remove_samples.txt
king: helper/king/king
mono_snp_thesh: 0.000000001
hwe_thresh: 0.000000000001
flashpca: helper/flashpca_x86-64
long_ld_file: helper/exclusion_regions_hg19.txt
```

Users will typically change the following:

#### "new_files:"

This is a header-less text file, where each line contains the full path to a set of genotype files (with no plink suffixes). Currently the pipeline expects map/ped files, but can be run with plink binary files (bim/bed/fam).

```
$cat sample_data/new_files.txt
sample_data/batch1
sample_data/batch2
```

#### "outdir:"

Full path to the desired output directory. 

#### "new_prefix:"

Name of prefix for all output files. All files associated with the anslysis will be labeled with this string. 

#### "contaminated_ids:"

This is a header-less, space-delimited text file, where each line corresponds to an FID/IID pair that should be EXCLUDED from analyses. This is often samples that have been contaminated, individuals who do not wish to be in the study, etc. Additional columns after the first two FID/IID columns are okay (e.g. fam file format)

```
$cat sample_data/remove_samples.txt
HCB181 1
HCB182 1
HCB183 1
HCB184 1
HCB185 1
```

## Running each step

```
bash vanilla/0_map_bed.sh sample_config 
bash vanilla/1_merge.sh sample_config
bash vanilla/2_contaminated.sh sample_config
bash vanilla/3_sex.sh sample_config
bash vanilla/4_qc.sh sample_config
bash vanilla/5_ibd.sh sample_config
bash vanilla/6_pca.sh sample_config
bash vanilla/7_summary.sh sample_config
```

## Running with plink binary files (fam/bed/bim)

If you have plink binary files instead, you can instead skip step 0 and use `skip_step_0_1.sh`. Also, if you have only one set of plink binary files and do not need to merge multiple batches, you can simply list one batch in the `new_files` field. 

## Example data

We have included a sample dataset from Hapmap in `sample_data` including the relevant configuration file. We also have included the directory of expected outputs in `sample_data/example_results`. 

