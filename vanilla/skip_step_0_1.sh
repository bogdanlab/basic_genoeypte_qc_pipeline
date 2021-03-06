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

# text file with path to plink files that have ALREADY been converted to map/ped format
new_files=$(cat $CONFIG | grep 'new_files:' | cut -d':' -f2 | sed 's/^ *//g')

# output directory for qc-ed files
outdir=$(cat $CONFIG | grep 'outdir:' | cut -d':' -f2 | sed 's/^ *//g')

# make output dir for original files (but leave it empty)
origdir=$outdir/0_orig
mkdir -p $origdir

# merge files 

# new prefix for plink files
new_prefix=$(cat $CONFIG | grep 'new_prefix:' | cut -d':' -f2 | sed 's/^ *//g')

# put merged files in own dir
qcdir=$outdir/1_merge

mkdir -p $qcdir

# merge new files
plink --merge-list $new_files  --make-bed  --out $qcdir/$new_prefix.qc1

