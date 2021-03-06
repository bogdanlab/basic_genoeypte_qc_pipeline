#!/bin/bash

CONFIG=$1

if [ -z "$CONFIG" ]
then
      echo "CONFIG file now found...looking for file 'config'"
      CONFIG=config
      if [[ -f "$CONFIG" ]] 
      then
	  echo "Using config file found in immediate directory."
      else
	  echo "Could not find any config file. Exiting."
	  exit 1 
      fi
fi

source /u/local/Modules/default/init/modules.sh
module load plink
module load python/anaconda3

# text file with path to new plink files (.map/.ped)
new_files=$(cat $CONFIG | grep 'new_files:' | cut -d':' -f2 | sed 's/^ *//g')

# new prefix for plink files
new_prefix=$(cat $CONFIG | grep 'new_prefix:' | cut -d':' -f2 | sed 's/^ *//g')

# output directory for qc-ed files 
outdir=$(cat $CONFIG | grep 'outdir:' | cut -d':' -f2 | sed 's/^ *//g')

# SNP missing threshold 
SNP_missing_thresh=$(cat $CONFIG | grep 'SNP_missing_thresh:' | cut -d':' -f2 | sed 's/^ *//g')

# Individuals missing threshold 
ind_missing_thresh=$(cat $CONFIG | grep 'ind_missing_thresh:' | cut -d':' -f2 | sed 's/^ *//g')

# Mono-snp threshold
mono_thres=$(cat $CONFIG | grep "mono_snp_thesh" | cut -d':' -f2 | sed 's/^ *//g')

# hwe thresh
hwe_thresh=$(cat $CONFIG | grep 'hwe_thresh:' | cut -d':' -f2 | sed 's/^ *//g')

# make ouptut directory for recoded files 
qcdir=$outdir/4_qc
prevdir=$outdir/2_uncontaminated

mkdir -p $qcdir 

plink_file=$prevdir/$new_prefix

dt=$(date '+%d/%m/%Y %H:%M:%S');
echo "$dt - Removing unmapped SNPs, high missing rate SNPs/individuals from: $plink_file"

# QC1: remove unmapped SNPs (in chr 0)
plink --bfile $plink_file.qc2 --not-chr 0 --make-bed --out $qcdir/$new_prefix.qc4a

# QC2: flag monomorphic SNPs
plink --bfile $qcdir/$new_prefix.qc4a --freq --out $qcdir/$new_prefix.qc4b

# change inconsitent tabs to spaces
cat $qcdir/$new_prefix.qc4b.frq | tr -s " " > $qcdir/$new_prefix.qc4b.frq_tmp
mv $qcdir/$new_prefix.qc4b.frq_tmp $qcdir/$new_prefix.qc4b.frq

# get list of SNPs that don't pass monoallelic threshold
python helper/qc_helper.py mono_snps $CONFIG

# QC3: remove SNPs with high missing rate
plink --bfile $qcdir/$new_prefix.qc4a --geno $SNP_missing_thresh  --make-bed --out $qcdir/$new_prefix.qc4c

# QC3: remove individuals with high missing rate
plink --bfile $qcdir/$new_prefix.qc4c --mind $ind_missing_thresh --make-bed --out $qcdir/$new_prefix.qc4d

# QC6: Distribution of HWE pvalues
plink --bfile $qcdir/$new_prefix.qc4d --hardy --out $qcdir/$new_prefix.qc4e

# change inconsitent tabs to spaces
cat $qcdir/$new_prefix.qc4e.hwe | tr -s " " > $qcdir/$new_prefix.qc4e.hwe_tmp
mv $qcdir/$new_prefix.qc4e.hwe_tmp $qcdir/$new_prefix.qc4e.hwe

python helper/qc_helper.py hwe_hist $CONFIG

# QC7: AT/CG count
python helper/qc_helper.py flip_count $CONFIG

tail -n +2 $qcdir/$new_prefix.qc4f.flip_count | cut  -f2 > $qcdir/$new_prefix.qc4f.flip_count_remove
plink --bfile $qcdir/$new_prefix.qc4d --exclude $qcdir/$new_prefix.qc4f.flip_count_remove --make-bed --out $qcdir/$new_prefix.qc4f
