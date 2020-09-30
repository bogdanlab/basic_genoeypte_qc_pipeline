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

# new prefix for plink files 
new_prefix=$(cat $CONFIG | grep 'new_prefix:' | cut -d':' -f2 | sed 's/^ *//g')

# put merged files in own dir 
qcdir=$outdir/1_merge
prevdir=$outdir/0_orig

mkdir -p $qcdir 

# merge batches from plink files
plink_merge_file=$qcdir/plink_merge_file.txt
echo -n "" > $plink_merge_file

# write plink file names to merge file 
while read file
do
    prefix=$(basename "$file")
    plink_file=$prevdir/$prefix
    echo $plink_file

    echo $plink_file.qc0 >> $plink_merge_file
   
done < $new_files


dt=$(date '+%d/%m/%Y %H:%M:%S');
echo "$dt - Merging new genotypes"

first_file=$(head -n 1 $plink_merge_file)
echo "" > plink_merge_file_short
echo $first_file

plink_merge_file_short=$qcdir/plink_merge_file.short
tail -n +2 $plink_merge_file > $plink_merge_file_short

# merge new files
plink --bfile $first_file --merge-list $plink_merge_file_short --make-bed  --out $qcdir/$new_prefix.qc1

# clean up files 
rm $plink_merge_file
rm $plink_merge_file_short
rm plink_merge_file_short