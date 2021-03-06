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

# path to flashpca exectable
pca=$(cat $CONFIG | grep 'flashpca:' | cut -d':' -f2 | sed 's/^ *//g')

# path to king exectable
king=$(cat $CONFIG | grep 'king:' | cut -d':' -f2 | sed 's/^ *//g')

# output directory for qc-ed files 
outdir=$(cat $CONFIG | grep 'outdir:' | cut -d':' -f2 | sed 's/^ *//g')

# new prefix for plink files 
new_prefix=$(cat $CONFIG | grep 'new_prefix:' | cut -d':' -f2 | sed 's/^ *//g')

# long-range ld file
long_ld_file=$(cat $CONFIG | grep 'long_ld_file'  | cut -d':' -f2 | sed 's/^ *//g')

qcdir=$outdir/6_pca
prevdir=$outdir/5_ibd

mkdir -p $qcdir 

dt=$(date '+%d/%m/%Y %H:%M:%S');
echo "$dt - LD pruning before running PCA"

# filter to only unrelated individuals
$king -b $prevdir/$new_prefix.qc5.bed --unrelated --degree 2 --prefix $qcdir/$new_prefix.qc6.

plink --bfile $prevdir/$new_prefix.qc5 --remove $qcdir/$new_prefix.qc6.unrelated_toberemoved.txt --make-bed --out $qcdir/$new_prefix.qc6


# first ld pruning and remove long-range ld
plink --bfile $qcdir/$new_prefix.qc6  --indep 200 5 1.3 --exclude range $long_ld_file --out $qcdir/$new_prefix.firstld --allow-no-sex

plink --bfile $qcdir/$new_prefix.qc6 --extract  $qcdir/$new_prefix.firstld.prune.in --make-bed --out $qcdir/$new_prefix.firstld.pruned 


# second ld pruning 
plink --bfile  $qcdir/$new_prefix.firstld.pruned --indep-pairwise 100 5 0.1 --out  $qcdir/$new_prefix.secondld --allow-no-sex

plink --bfile $qcdir/$new_prefix.firstld.pruned  --extract  $qcdir/$new_prefix.secondld.prune.in --make-bed --out $qcdir/$new_prefix.secondld.pruned 


dt=$(date '+%d/%m/%Y %H:%M:%S');
echo "$dt - Running PCA"

# run flashpca
./$pca --bfile $qcdir/$new_prefix.secondld.pruned --outvec ${qcdir}/$new_prefix.vec --outpve ${qcdir}/$new_prefix.pve --outval ${qcdir}/$new_prefix.val --outpc ${qcdir}/$new_prefix.pcs

# identify outliers from the projection
python helper/qc_helper.py pca_outliers $CONFIG

# make pca plots 
python helper/qc_helper.py pca_plots $CONFIG 

