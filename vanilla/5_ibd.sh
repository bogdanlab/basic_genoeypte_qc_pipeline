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

# path to king exectable
king=$(cat $CONFIG | grep 'king:' | cut -d':' -f2 | sed 's/^ *//g')

# output directory for qc-ed files 
outdir=$(cat $CONFIG | grep 'outdir:' | cut -d':' -f2 | sed 's/^ *//g')

# new prefix for plink files 
new_prefix=$(cat $CONFIG | grep 'new_prefix:' | cut -d':' -f2 | sed 's/^ *//g')

# put ibd results in qc folder  
qcdir=$outdir/5_ibd
prevdir=$outdir/4_qc

mkdir -p $qcdir 

dt=$(date '+%d/%m/%Y %H:%M:%S');
echo "$dt - Identifying duplicate indivduals"

# find 1st and 2nd degree relatives: kin0 file 
$king -b  $prevdir/$new_prefix.qc4f.bed --related --degree 2 --prefix $qcdir/$new_prefix.qc5 --prefix  $qcdir/$new_prefix.qc5

# calculate duplicated individuals 
$king -b $prevdir/$new_prefix.qc4f.bed --duplicate --prefix $qcdir/$new_prefix.qc5

# find missingness rate of duplicate individuals 
plink --bfile $prevdir/$new_prefix.qc4f --missing --out $qcdir/$new_prefix.qc5

# change inconsitent tabs to spaces 
cat $qcdir/$new_prefix.qc5.imiss | tr -s " " > $qcdir/$new_prefix.qc5.imiss_tmp
mv $qcdir/$new_prefix.qc5.imiss_tmp $qcdir/$new_prefix.qc5.imiss 

sed -i 's/^ *//' $qcdir/$new_prefix.qc5.imiss

# find individual with higher missing rate
python helper/qc_helper.py duplicate_missingness $CONFIG

# remove duplicate individual with higher missing rate 
plink --bfile $prevdir/$new_prefix.qc4f --remove $qcdir/$new_prefix.qc5.dups_remove  --make-bed --out $qcdir/$new_prefix.qc5
