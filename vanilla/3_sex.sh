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

# put hwe results in new folder  
qcdir=$outdir/3_sexcheck
prevdir=$outdir/2_uncontaminated

mkdir -p $qcdir 

dt=$(date '+%d/%m/%Y %H:%M:%S');

# sex check
plink --bim $prevdir/$new_prefix.qc2.bim --bed $prevdir/$new_prefix.qc2.bed --fam $prevdir/$new_prefix.qc2.fam --check-sex  --out $qcdir/$new_prefix.qc3 
