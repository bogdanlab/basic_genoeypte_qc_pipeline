import fire
import pandas as pd 
import numpy as np
import os
from datetime import datetime
import matplotlib.pyplot as plt
plt.switch_backend('agg')

def pretty_print(s, f):
    f.write(s)
    f.write("\n")
    print(s)


def mono_snps(config):
    # process config file
    
    config_df=pd.read_csv(config, sep=':', header=None)
    config_df = config_df.set_index(0)
    config_df = config_df.T
    new_prefix = config_df['new_prefix'].values[0].replace(" ", "")
    outdir = config_df['outdir'].values[0].replace(" ", "")
    mono_thresh = float(config_df['mono_snp_thesh'].values[0].replace(" ", ""))
    
    qc_dir=os.path.join(outdir, '4_qc')
    frq_file = os.path.join(qc_dir, new_prefix + '.qc4b.frq')
    
    # filter by threshold
    frq_df = pd.read_csv(frq_file, sep=' ')
    filter_df = frq_df.loc[frq_df['MAF'] < mono_thresh]
    
    outfile = os.path.join(qc_dir, new_prefix + '.qc4b.mono')
    if filter_df.shape[0] == 0:
        print("No monomorphic SNPs found")
        pd.DataFrame({}).to_csv(outfile, index=False)
    else:
        filter_df.to_csv(outfile, index=False)
    
    
def hwe_hist(config):
    
    # process config file
    config_df=pd.read_csv(config, sep=':', header=None)
    config_df = config_df.set_index(0)
    config_df = config_df.T
    new_prefix = config_df['new_prefix'].values[0].replace(" ", "")
    outdir = config_df['outdir'].values[0].replace(" ", "")
    hwe_thresh = float(config_df['hwe_thresh'].values[0].replace(" ", ""))

    qc_dir=os.path.join(outdir, '4_qc')
    hwe_file = os.path.join(qc_dir, new_prefix + '.qc4e.hwe')

    hwe_df = pd.read_csv(hwe_file, sep=' ')

    plt.hist(hwe_df['P'].values, bins=20)
    plt.xlim(0,1)
    plt.xlabel('p-value')
    plt.title('HWE test')
    outfile=os.path.join(qc_dir, new_prefix + 'qc4e.hwe.pdf')
    plt.savefig(outfile)
    
    # make list of SNPs that don't pass pvalue threshold
    hwe_filter = hwe_df.loc[hwe_df['P'] <= hwe_thresh]
    hwe_filter_file =  os.path.join(qc_dir, new_prefix + '.qc4e.hwe_filter_fail')
    hwe_filter.to_csv(hwe_filter_file, index=False)
    
def sex_check(config):
    # process config file
    config_df=pd.read_csv(config, sep=':', header=None)
    config_df = config_df.set_index(0)
    config_df = config_df.T
    new_prefix = config_df['new_prefix'].values[0].replace(" ", "")
    outdir = config_df['outdir'].values[0].replace(" ", "")
    ddr_sex_file = config_df['sex_file'].values[0].replace(" ", "")

    fam_sex_file = os.path.join(outdir, os.path.join('3_sexcheck', new_prefix + '.qc3.fam' ))

    sex_df = pd.read_csv(ddr_sex_file, sep=',')
    sex_df.columns = ['SEX', 'IID']

    # read in fam file from ibd step 

    fam_file = os.path.join(outdir, os.path.join('2_uncontaminated', new_prefix + '.qc2.fam'))
    fam_df = pd.read_csv(fam_file, sep=' ')
    fam_df.columns = ['FID', 'IID', 'MID', 'PID', 'SEX', 'PHENO']
    fam_df.drop(['SEX'], axis=1, inplace=True)

    fam_sex_df = fam_df.merge(sex_df, how='left', on='IID')

    fam_sex_df['SEX'] = fam_sex_df['SEX'].fillna(-9)
    fam_sex_df['SEX'] = fam_sex_df['SEX'].astype(int)

    fam_sex_df = fam_sex_df[['FID','IID', 'MID', 'PID', 'SEX', 'PHENO']]

    fam_sex_df.to_csv(fam_sex_file, header=False, index=False, sep=' ')


# plot pcs 
def pca_plots(config):
    # process config file
    config_df=pd.read_csv(config, sep=':', header=None)
    config_df = config_df.set_index(0)
    config_df = config_df.T
    new_prefix = config_df['new_prefix'].values[0].replace(" ", "")
    outdir = config_df['outdir'].values[0].replace(" ", "")

    pcs_file = os.path.join(outdir, os.path.join('6_pca', new_prefix + '.pcs'))
    pcs_df = pd.read_csv(pcs_file, sep='\t')

    for i in range(1,10):
        plt.scatter(pcs_df['PC' + str(i)], pcs_df['PC' + str(i+1)])
        plt.xlabel("PC" + str(i))
        plt.ylabel("PC" + str(i+1))
        pc_plot_file = os.path.join(outdir, os.path.join('6_pca', new_prefix+'.pc%d_pc%d.pdf' % (i, i+1)))
        plt.savefig(pc_plot_file)
        plt.close()

    
def duplicate_missingness(config):
    
    # process config file
    config_df=pd.read_csv(config, sep=':', header=None)
    config_df = config_df.set_index(0)
    config_df = config_df.T
    new_prefix = config_df['new_prefix'].values[0].replace(" ", "")
    outdir = config_df['outdir'].values[0].replace(" ", "")

    # output from king 
    king_dups_file = os.path.join(outdir, os.path.join("5_ibd", new_prefix + '.qc5.con'))
    imiss_file = os.path.join(outdir, os.path.join("5_ibd", new_prefix + '.qc5.imiss'))
    plink_remove_file = os.path.join(outdir, os.path.join("5_ibd", new_prefix + '.qc5.dups_remove'))

    dups_df = pd.read_csv(king_dups_file, sep='\t')
    dups_df = dups_df[['FID1', 'ID1', 'FID2', 'ID2']]

    missing_df = pd.read_csv(imiss_file, sep=" ")
    missing_df = missing_df[['FID', 'IID', 'F_MISS']]

    # loop through each pair of individuals

    remove_iids = [] 

    for index, row in dups_df.iterrows():

        iid1 = row['ID1']
        iid2 = row['ID2']

        # get missing rate for both
        mr_1 = missing_df.loc[missing_df['IID'] == iid1, 'F_MISS']
        mr_2 = missing_df.loc[missing_df['IID'] == iid2, 'F_MISS']

        if len(mr_1.index) != 1 or len(mr_2.index) != 1:
            print("ERROR: iid not in data or multiple matching iids!")

        # record patient with higher missing rate
        mr_1 = mr_1.values[0]
        mr_2 = mr_2.values[0]

        print("Asessing missingness between: %s, %s" % (iid1, iid2))
        print("Missing rates: %.4g, %.4g" % (mr_1, mr_2))

        if mr_2 > mr_1:
            # remove 2nd patient
            remove_iids.append(iid2)
            print("Removing %s" % iid2)
        else: # first patient has higher missing rate
            # remove first patient
            remove_iids.append(iid1)
            print("Removing %s" % iid1)

    plink_df = missing_df.loc[missing_df['IID'].isin(remove_iids)]

    plink_df = plink_df[['FID', 'IID']]

    plink_df.to_csv(plink_remove_file, header=None, index=False, sep=' ')



def flip_count(config):
    # process config file
    config_df=pd.read_csv(config, sep=':', header=None)
    config_df = config_df.set_index(0)
    config_df = config_df.T
    new_prefix = config_df['new_prefix'].values[0].replace(" ", "")
    outdir = config_df['outdir'].values[0].replace(" ", "")
    
    # read in bim file
    qc_dir = os.path.join(outdir, '4_qc')
    bim_file = os.path.join(qc_dir, new_prefix + '.qc4d.bim')
    bim_df = pd.read_csv(bim_file, sep="\t", header=None)
    bim_df.columns = ['CHR', 'SNP', 'CM', 'BP', 'ALT', 'REF']
    bim_df['allele_pair'] = bim_df['ALT'].astype(str) + '/' + bim_df['REF'].astype(str)
    
    # make logging file
    log_file = os.path.join(qc_dir, new_prefix + '.qc4f.flip_count_log')
    log_f = open(log_file, "w")
    
    # only keep A/T and C/G (vice versa) pairs
    ambig_pairs = ["A/T", "T/A", "C/G", "G/C"]
    
    ambig_df = bim_df.loc[bim_df['allele_pair'].isin(ambig_pairs)]
    
    ambig_file = os.path.join(outdir, os.path.join("4_qc", new_prefix + '.qc4f.flip_count'))
    
    if ambig_df.shape[0] == 0:
        ambig_df = pd.DataFrame({}, columns=['CHR', 'SNP', 'BP', 'allele_pair'])
        ambig_df.to_csv(ambig_file, sep="\t", index=False,\
 columns=['CHR', 'SNP', 'BP', 'allele_pair'])
    else:
        ambig_df.to_csv(ambig_file, sep="\t", index=False, columns=['CHR', 'SNP', 'BP', 'allele_pair'])
    
    n_flip_count = len(ambig_df.index)
    
    pretty_print("Found a total of %d SNPs where the strand may need to be flipped" % n_flip_count ,log_f)
    
    pretty_print("Writing list of possible SNPs to flip at: %s" % ambig_file ,log_f)
    
    
def pca_outliers(config):
    # process config file
    config_df=pd.read_csv(config, sep=':', header=None)
    config_df = config_df.set_index(0)
    config_df = config_df.T
    new_prefix = config_df['new_prefix'].values[0].replace(" ", "")

    outdir = config_df['outdir'].values[0].replace(" ", "")

    # read in pcs file
    pca_dir = os.path.join(outdir, '6_pca')
    pcs_file = os.path.join(pca_dir, new_prefix + '.pcs')
    pcs_df = pd.read_csv(pcs_file, sep="\t")
    outlier_df = pd.DataFrame({"FID":[], "IID":[], "outlier": [], "mean": [] })

    # make logging file
    log_file = os.path.join(pca_dir, new_prefix + '.pca_log')
    log_f = open(log_file, "w")

    # loop through 10 PCs
    for pc in range(1, 1 + 10):
        # calculate std
        pc_column = 'PC{}'.format(str(pc))
        std = np.std(pcs_df[pc_column].values)
        pc_mean = np.mean(pcs_df[pc_column].values)
        
        # remove individuals >6SD, <(-)6SD
        pc_outlier_df = pcs_df.loc[(pcs_df[pc_column] > (pc_mean + 6*std)) | (pcs_df[pc_column] < (pc_mean + -6*std))]

        # add to running list
        outlier_df = pd.concat([outlier_df, pc_outlier_df],sort=False)

        # log to screen the outliers

        for index, row in outlier_df.iterrows():

            time=datetime.now()
            date_time = time.strftime("%m/%d/%Y, %H:%M:%S")

            iid = row['IID']
            fid = row['FID']
            outlier_value = row[pc_column] 
            mean_value = pc_mean

            pretty_print("%s - Individual %s identified as an outlier on PC%d: %.4g (mean: %.4g)" % (date_time, fid, pc, outlier_value, mean_value), log_f)

    # output ids to remove
    remove_file = os.path.join(pca_dir, new_prefix + '.pca_outlier')
    outlier_df.drop_duplicates(inplace = True)

    if outlier_df.shape[0] == 0:
        time=datetime.now()
        date_time = time.strftime("%m/%d/%Y, %H:%M:%S")
        pretty_print("%s - ZERO outliers found" % (date_time), log_f)
        pd.DataFrame({'FID': [], 'IID': [] }).to_csv(remove_file, sep=' ', index=False, columns=['FID', 'IID'], header=False)
    else:
        outlier_df.to_csv(remove_file, sep=' ', index=False, columns=['FID', 'IID'], header=False)

        n_outlier = len(outlier_df.index)

        time=datetime.now()
        date_time = time.strftime("%m/%d/%Y, %H:%M:%S")
        pretty_print("%s - TOTAL %d outlier individuals" % (date_time, n_outlier), log_f)
        pretty_print("%s - outputing individuals to remove to %s" % (date_time, remove_file), log_f)


if __name__ == '__main__':
    fire.Fire()
