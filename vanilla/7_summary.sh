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
module load python/anaconda3

# output directory for qc-ed files 
outdir=$(cat $CONFIG | grep 'outdir:' | cut -d':' -f2 | sed 's/^ *//g')

# new prefix for plink files 
new_prefix=$(cat $CONFIG | grep 'new_prefix:' | cut -d':' -f2 | sed 's/^ *//g')

# text file with path to new plink files (.map/.ped)
new_files=$(cat $CONFIG | grep 'new_files:' | cut -d':' -f2 | sed 's/^ *//g')

qcdir=$outdir/7_summary

mkdir -p $qcdir

summary_file=$qcdir/$new_prefix.qc.summary

dt=$(date '+%d/%m/%Y %H:%M:%S');

# summary file header
echo -n "" > $summary_file
echo "QC Name: $new_prefix" >> $summary_file
echo "Date: $dt" >> $summary_file 

# list of batches 
n_batches=$(wc -l $new_files | cut -d' ' -f1)
echo "Number of batches: $n_batches" >> $summary_file
echo "List of batches: $new_files" >> $summary_file 

# original number of inds and SNPs
qcdir=$outdir/1_merge
n_inds=$(wc -l $qcdir/$new_prefix.qc1.fam | cut -d' ' -f1)
n_snps=$(wc -l $qcdir/$new_prefix.qc1.bim | cut -d' ' -f1)

echo "Number of original inds: $n_inds" >> $summary_file
echo "Number of original SNPs: $n_snps" >> $summary_file

# contaminated individuals 
qcdir=$outdir/2_uncontaminated
n_inds=$(wc -l $qcdir/$new_prefix.qc2.fam | cut -d' ' -f1)
n_snps=$(wc -l $qcdir/$new_prefix.qc2.bim | cut -d' ' -f1)
contaminated_ids=$(cat $CONFIG | grep 'contaminated_ids:' | cut -d':' -f2 | sed 's/^ *//g')
n_contaminated=$(wc -l $contaminated_ids | cut -d' ' -f1)

echo "Number of contaminated inds: $n_contaminated" >> $summary_file 
echo "List of contaminated ids: $contaminated_ids" >> $summary_file
echo "Number of inds after removing contaminated inds: $n_inds" >> $summary_file 
echo "Number of SNPs after removing contaminated inds: $n_snps" >> $summary_file

# unmapped SNPs
qcdir=$outdir/4_qc
n_snps=$(wc -l $qcdir/$new_prefix.qc4a.bim | cut -d' ' -f1)
n_inds=$(wc -l $qcdir/$new_prefix.qc4a.fam | cut -d' ' -f1)

echo "Number of inds after removing unmapped SNPs: $n_inds" >> $summary_file
echo "Number of SNPs after removing unmapped SNPs: $n_snps" >> $summary_file 

# high misisng rate inds and SNPs
n_snps=$(wc -l $qcdir/$new_prefix.qc4c.bim |cut -d' ' -f1)
n_inds=$(wc -l $qcdir/$new_prefix.qc4d.fam | cut -d' ' -f1 )

echo "Number of inds after removing high missing rate inds: $n_inds" >> $summary_file 
echo "Number of SNPs after remvoing high missing rate SNPs: $n_snps" >> $summary_file 

# monomorphic SNPs

n_snps=$(tail -n +1 $qcdir/$new_prefix.qc4b.mono | wc -l)

echo "Number of monomorphic SNPs (not removed): $n_snps"  >> $summary_file

n_snps=$(tail -n +1 $qcdir/$new_prefix.qc4e.hwe_filter_fail | wc -l)

echo "Number of SNPs after HWE SNP filter (not removed): $n_snps" >> $summary_file

# AT/CG count 
flip_count_file=$qcdir/$new_prefix.qc4f.flip_count
n_flip_snps=$(tail -n +2 $flip_count_file | wc -l | cut -d' ' -f1)
echo "Number of strand flipped SNPs: $n_flip_snps" >> $summary_file 
echo "List of strand flipped SNPs: $flip_count_file" >> $summary_file

# duplicate individuals 
qcdir=$outdir/5_ibd
kin_file=$qcdir/$new_prefix.qc5.kin0

if test ! -f "$kin_file"; then
    echo "Related individuals file not produced...check for possible errors from KING if this is not expected" >> $summary_file

else

    n_MZ=$(cat $kin_file | grep "Dup/MZ" | wc -l)
    n_PO=$(cat $kin_file | grep "PO" | wc -l)
    n_FS=$(cat $kin_file  | grep "FS" | wc -l)
    n_2nd=$(cat $kin_file | grep "2nd" | wc -l)

    echo "Number Duplicates/Twins: $n_MZ" >> $summary_file
    echo "Number of Parent-Offspring: $n_PO" >> $summary_file 
    echo "Number of first-degree relatives: $n_FS" >> $summary_file
    echo "Number of 2nd-degree relatives: $n_2nd" >> $summary_file 

fi

# PCA outliers 
qcdir=$outdir/6_pca
pca_outliers=$qcdir/$new_prefix.pca_outlier
n_pca_outliers=$(wc -l $pca_outliers | cut -d' ' -f1)
pcs_file=$qcdir/$new_prefix.pcs 
pve_file=$qcdir/$new_prefix.pve 
val_file=$qcdir/$new_prefix.val
vec_file=$qcdir/$new_prefix.vec

echo "Number of outliers from PCA: $n_pca_outliers" >> $summary_file
echo "Prop variance explained: $pcs_file" >> $summary_file 
echo "Proj PCA file: $pcs_file" >> $summary_file 
echo "Eigenvector file: $vec_file" >> $summary_file 
echo "Eigenvalue file: $val_file" >> $summary_file 
echo "PCA plot figures: $qcdir" >> $summary_file 

# Sex check 
if test -f "$FILE"; then
    qcdir=$outdir/3_sexcheck

    sexcheck_file=$qcdir/$new_prefix.qc3.sexcheck

    sed -i 's/^ *//' $sexcheck_file

    n_male=$(awk '{print $4}' $sexcheck_file | grep 1 | wc -l)
    n_female=$(awk '{print $4}' $sexcheck_file | grep 2 | wc -l)
    n_unknown=$(awk '{print $4}' $sexcheck_file | grep 0 | wc -l)

    echo "Sexcheck file: $sexcheck_file" >> $summary_file 
    echo "Number of males: $n_male" >> $summary_file
    echo "Number of females: $n_female" >> $summary_file
    echo "Number of unknown: $n_unknown" >> $summary_file

else
    echo "Sexcheck file: no sexcheck file exists...check if sex chromosomes are present in data" >> $summary_file

fi

qc_plink_files=$outdir/5_ibd/$new_prefix.qc5
echo "Final qc plink files (fam/bed/bim): $qc_plink_files" >> $summary_file

echo "Final summary file: $summary_file"
