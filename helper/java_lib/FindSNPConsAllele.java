import java.io.*;
import java.util.*;
import java.util.zip.*;

public class FindSNPConsAllele {

    public void printErrorMessage(String error) throws Exception {
        System.err.println("ERROR_in_FindSNPConsAllele : "+error);
	System.exit(-1);
    }

    public void readACMGFile(String acmgfile, Hashtable<String,ArrayList<ArrayList<String>>> acmgPosAlleleInfo) throws Exception {
	BufferedReader in = new BufferedReader(new FileReader(acmgfile));
	String line = in.readLine();
	line = in.readLine();
	while (line != null) {
	    String[] token = line.split("\\t");
	    String chr = token[0];
	    String pos = token[1];
	    String refallele = token[2];
	    String altallele = token[3];
	    ArrayList<String> alleleList = new ArrayList<String>();
	    alleleList.add(refallele);
	    alleleList.add(altallele);
	    String chrpos = chr+":"+pos;
	    if (acmgPosAlleleInfo.containsKey(chrpos)) {
		ArrayList<ArrayList<String>> tempList = acmgPosAlleleInfo.get(chrpos);
                tempList.add(alleleList);
                acmgPosAlleleInfo.put(chrpos, tempList);
	    }
	    else {
		ArrayList<ArrayList<String>> tempList = new ArrayList<ArrayList<String>>();
		tempList.add(alleleList);
		acmgPosAlleleInfo.put(chrpos, tempList);
	    }
	    line = in.readLine();
	}
	in.close();
	System.out.println("# of unique positions in ACMG = "+acmgPosAlleleInfo.size());
    }

    public int compareAlleles(char gfirst, char gsecond, char rfirst, char rsecond) throws Exception {
        int numSame = 0;
        if (gfirst == rfirst) {
            numSame++;
        }
        else if (gfirst == rsecond) {
            numSame++;
        }
        if (gsecond == rfirst) {
            numSame++;
        }
        else if (gsecond == rsecond) {
            numSame++;
        }
        return numSame;
    }

    public char flipAllele(char allele, String rsid) throws Exception {
        char flipallele = ' ';
        if (allele == 'A') {
            flipallele = 'T';
        }
        else if (allele == 'C') {
            flipallele = 'G';
        }
        else if (allele == 'G') {
            flipallele = 'C';
        }
        else if (allele == 'T') {
            flipallele = 'A';
        }
        else if (allele == '0') {
            flipallele = '0';
        }
        else {
            System.err.println(rsid+" SNP has unrecognized allele " + allele);
            System.exit(-1);
        }
        return flipallele;
    }

    public boolean isATGCSNP(char fallele, char sallele) throws Exception {
        boolean isATGC = false;
        if (fallele == 'A' && sallele == 'T') {
            isATGC = true;
        }
        if (fallele == 'T' && sallele == 'A') {
            isATGC = true;
        }
        if (fallele == 'G' && sallele == 'C') {
            isATGC = true;
        }
        if (fallele == 'C' && sallele == 'G') {
            isATGC = true;
        }
        return isATGC;
    }

    public void createFiles(String acmgfile, String acmgoutputfile, String bimfile, String bimoutputfile, String bimflipfile) throws Exception {
	Hashtable<String,ArrayList<ArrayList<String>>> acmgPosAlleleInfo = new Hashtable<String,ArrayList<ArrayList<String>>>();
	readACMGFile(acmgfile, acmgPosAlleleInfo);

	Hashtable<String,Integer> passRSIDInfo = new Hashtable<String,Integer>();
	BufferedReader in = new BufferedReader(new FileReader(bimfile));
	PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(bimoutputfile)));
	PrintWriter out2 = new PrintWriter(new BufferedWriter(new FileWriter(bimflipfile)));
	String line = in.readLine();
	int numSNPJointBim = 0;
	int numATGCBim = 0;
	int numATGCACMG = 0;
	int numPassBim = 0;
	int numAllMissingBim = 0;
	int numIndelBim = 0;
	int numFlipBim = 0;
	Hashtable<String,Integer> acmgPassInfo = new Hashtable<String,Integer>();
	Hashtable<String,String> chrposRSIDInfo = new Hashtable<String,String>();
	while (line != null) {
	    String[] token = line.split("\\s");
	    String chr = token[0];
	    String rsid = token[1];
	    String pos = token[3];
	    String chrpos = chr+":"+pos;
	    chrposRSIDInfo.put(chrpos, rsid);
	    if (!acmgPosAlleleInfo.containsKey(chrpos)) {
		line = in.readLine();
		continue;
	    }
	    numSNPJointBim++;
	    char gfirst = token[4].charAt(0);
	    char gsecond = token[5].charAt(0);
	    if (gfirst == '0' && gsecond == '0') {
		numAllMissingBim++;
                line = in.readLine();
                continue;
	    }
	    if (gfirst == 'I' || gfirst == 'D' || gsecond == 'I' || gsecond == 'D') {
		numIndelBim++;
                line = in.readLine();
                continue;
	    }
	    boolean isATGCfirst = isATGCSNP(gfirst, gsecond);
	    if (isATGCfirst) {
		numATGCBim++;
                line = in.readLine();
                continue;
	    }
	    ArrayList<ArrayList<String>> posAlleleList = acmgPosAlleleInfo.get(chrpos);
	    boolean isfirst = true;
	    boolean isflip = false;
	    for (int i = 0; i < posAlleleList.size(); i++) {
		ArrayList<String> alleleList = posAlleleList.get(i);
		char rfirst = alleleList.get(0).charAt(0);
		char rsecond = alleleList.get(1).charAt(0);
		if (rfirst == '0' || rsecond == '0') {
		    printErrorMessage("ACMG cannot have 0 allele at chr"+chr+" "+pos);
		}
		boolean isATGCsecond = isATGCSNP(rfirst, rsecond);
		if (isATGCsecond) {
		    numATGCACMG++;
		    continue;
		}
		boolean ispass = false;
		int numSame = compareAlleles(gfirst, gsecond, rfirst, rsecond);
		if (numSame == 2) {
		    ispass = true;
		}
		else if (numSame == 0) {
		    char gfirstflip = flipAllele(gfirst,rsid);
		    char gsecondflip = flipAllele(gsecond,rsid);
		    int numSame2 = compareAlleles(gfirstflip, gsecondflip, rfirst, rsecond);
		    if (gfirst == '0' || gsecond == '0') {
			if (numSame2 != 1) {
			    printErrorMessage("BIM file has 0 allele, and flipping does not help, which is not possible at chr"+chr+" "+pos);
			}
			else {
			    ispass = true;
			    isflip = true;
			}
		    }
		    else if (numSame2 != 2) {
			printErrorMessage("BIM file has 0 mismatching allele, but flipping does not help, which is not possible at chr"+chr+" "+pos);
		    }
		    else {
			ispass = true;
			isflip = true;
		    }
		}
		else if (numSame == 1) {
		    if (gfirst == '0' || gsecond == '0') {
			ispass = true;
		    }
		}
		if (ispass) {
		    if (isfirst) {
			if (passRSIDInfo.containsKey(rsid)) {
			    printErrorMessage(rsid+" seen twice in bim file");
			}
			else {
			    passRSIDInfo.put(rsid, 0);
			}
			numPassBim++;
			out.println(rsid);
			isfirst = false;
			if (isflip) {
			    numFlipBim++;
			    out2.println(rsid);
			}
		    }
		    String mergedAllele = "";
		    if (rfirst < rsecond) {
			mergedAllele = rfirst+":"+rsecond;
		    }
		    else {
			mergedAllele = rsecond+":"+rfirst;
		    }
		    String mergedPosAllele = chrpos+"_"+mergedAllele;
		    if (!acmgPassInfo.containsKey(mergedPosAllele)) {
			acmgPassInfo.put(mergedPosAllele, 0);
		    }
		}
	    }
	    line = in.readLine();
	}
	in.close();
	out.close();
	out2.close();
	System.out.println("# of SNPs in both Bim and ACMG = "+numSNPJointBim);
	System.out.println("# of all missing SNPs in Bim = "+numAllMissingBim);
	System.out.println("# of indels in Bim = "+numIndelBim);
	System.out.println("# of A/T or G/C SNPs in Bim = "+numATGCBim);
	System.out.println("# of A/T or G/C SNPs in ACMG = "+numATGCACMG);
	System.out.println("# of pass SNPs in Bim = "+numPassBim);
	System.out.println("# of flip SNPs in Bim = "+numFlipBim);

	in = new BufferedReader(new FileReader(acmgfile));
	out = new PrintWriter(new BufferedWriter(new FileWriter(acmgoutputfile)));
	line = in.readLine();
	out.println(line+"\trsID");
	line = in.readLine();
	while (line != null) {
	    String[] token = line.split("\\t");
            String chr = token[0];
            String pos = token[1];
	    String chrpos = chr+":"+pos;
	    String rsid = chrposRSIDInfo.get(chrpos);
            char rfirst = token[2].charAt(0);
            char rsecond = token[3].charAt(0);
	    String mergedAllele = "";
	    if (rfirst < rsecond) {
		mergedAllele = rfirst+":"+rsecond;
	    }
	    else {
		mergedAllele = rsecond+":"+rfirst;
	    }
	    String mergedPosAllele = chrpos+"_"+mergedAllele;
	    if (acmgPassInfo.containsKey(mergedPosAllele)) {
		out.println(line+"\t"+rsid);
	    }
	    line = in.readLine();
	}
	in.close();
	out.close();
    }

    public static void main(String[] args) throws Exception {
	if (args.length != 5) {
	    System.err.println("[usage] java FindSNPConsAllele [merged_ACMG.parsed.txt] [acmg output file] [bim file] [bim output file] [bim flip file]");
	    System.exit(-1);
	}
	FindSNPConsAllele cc = new FindSNPConsAllele();
	cc.createFiles(args[0], args[1], args[2], args[3], args[4]);
    }
}

