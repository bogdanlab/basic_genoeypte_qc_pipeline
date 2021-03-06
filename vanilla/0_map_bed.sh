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

# text file with path to new plink files (.map/.ped)
new_files=$(cat $CONFIG | grep 'new_files:' | cut -d':' -f2 | sed 's/^ *//g')

# output directory for qc-ed files 
outdir=$(cat $CONFIG | grep 'outdir:' | cut -d':' -f2 | sed 's/^ *//g')

# make ouptut directory for recoded files 
outdir=$outdir/0_orig
mkdir -p $outdir 

while read plink_file
do
    dt=$(date '+%d/%m/%Y %H:%M:%S');
    echo "$dt - Recoding original plink file: $plink_file"
    prefix=$(basename "$plink_file")

    plink --file "$plink_file" --make-bed  --out $outdir/$prefix.qc0


done < $new_files
