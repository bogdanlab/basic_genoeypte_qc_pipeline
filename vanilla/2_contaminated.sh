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

# output directory for qc-ed files 
outdir=$(cat $CONFIG | grep 'outdir:' | cut -d':' -f2 | sed 's/^ *//g')

# new prefix for plink files 
new_prefix=$(cat $CONFIG | grep 'new_prefix:' | cut -d':' -f2 | sed 's/^ *//g')

# text file with contaminated ids
contaminated_ids=$(cat $CONFIG | grep 'contaminated_ids:' | cut -d':' -f2 | sed 's/^ *//g')

# put uncontaminated files in qc_1 dir 
qcdir=$outdir/2_uncontaminated
prevdir=$outdir/1_merge

mkdir -p $qcdir 

# text file with path to new plink files (.map/.ped)
new_files=$(cat $CONFIG | grep 'new_files:' | cut -d':' -f2 | sed 's/^ *//g')

dt=$(date '+%d/%m/%Y %H:%M:%S');
echo "$dt - Removing contanimated samples"

# remove contaminated ids
plink --bfile $prevdir/$new_prefix.qc1 --remove $contaminated_ids  --make-bed --out $qcdir/$new_prefix.qc2 

