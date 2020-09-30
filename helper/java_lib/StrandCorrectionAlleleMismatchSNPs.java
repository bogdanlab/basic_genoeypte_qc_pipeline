import java.io.*;
import java.util.*;
import java.lang.*;

public class StrandCorrectionAlleleMismatchSNPs {

    public void readFrqFile(String bimfile, Hashtable<String,ArrayList> alleles, ArrayList<String> snpList) throws Exception {
	snpList.clear();
	BufferedReader in = new BufferedReader(new FileReader(bimfile));
	String line = in.readLine();
	while (line != null) {
	    String[] tokens = line.split("\\s");
	    if (tokens.length != 6) {
		System.err.println(tokens[1]+" has less than 6 columns");
	    }
	    String rsid = new String(tokens[1]);
	    ArrayList<Character> alist = new ArrayList<Character>();
	    alist.add(new Character(tokens[4].charAt(0)));
	    alist.add(new Character(tokens[5].charAt(0)));
	    alleles.put(rsid, alist);
	    snpList.add(rsid);
	    line = in.readLine();
	}
	in.close();
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

    public void createFiles(String firstbimfile, String secondbimfile, String outputfile, String outputupdate, String atgcsnp) throws Exception {
	Hashtable<String,ArrayList> gediAlleles = new Hashtable<String,ArrayList>();
	Hashtable<String,ArrayList> refAlleles = new Hashtable<String,ArrayList>();
	ArrayList<String> snpList = new ArrayList<String>();
	readFrqFile(firstbimfile, gediAlleles, snpList);
	readFrqFile(secondbimfile, refAlleles, snpList);
	PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputfile)));
	PrintWriter outu = new PrintWriter(new BufferedWriter(new FileWriter(outputupdate)));
	PrintWriter outa = new PrintWriter(new BufferedWriter(new FileWriter(atgcsnp)));
	for (int i = 0; i < snpList.size(); i++) {
	    String rsid = snpList.get(i);
	    ArrayList gList = gediAlleles.get(rsid);
	    ArrayList rList = refAlleles.get(rsid);
	    char gfirst = (Character)gList.get(0);
	    char gsecond = (Character)gList.get(1);
	    char rfirst = (Character)rList.get(0);
	    char rsecond = (Character)rList.get(1);
	    int numSame = compareAlleles(gfirst, gsecond, rfirst, rsecond);
	    if (numSame == 2) {
		// do nothing
	    }
	    else if (numSame == 0) {
		char gfirstflip = flipAllele(gfirst,rsid);
		char gsecondflip = flipAllele(gsecond,rsid);
		int numSame2 = compareAlleles(gfirstflip, gsecondflip, rfirst, rsecond);
		if (rfirst == '0' || rsecond == '0' || gfirst == '0' || gsecond == '0') {
		    if (numSame2 != 1) {
			System.out.println(rsid+" SNP has one 0 allele but flipping does not help");
		    }
		    else {
			out.println(rsid);
		    }
		}
		else if (numSame2 != 2) {
		    System.out.println("Flipping "+rsid+" does not work");
		}
		else {
		    out.println(rsid);
		}
	    }
	    else if (numSame == 1) {
		if ((rfirst != '0' && rsecond != '0') && (gfirst != '0' && gsecond != '0')) {
		    System.out.println(rsid+" SNP has non 0 allele but share only one allele");
		    //out.println(rsid);
		    outu.println(rsid+" "+gfirst+" "+gsecond+" "+rfirst+" "+rsecond);
		}
		if (gfirst == '0' && rfirst == '0') {
		    char gsecondflip = flipAllele(gsecond,rsid);
		    if (gsecondflip == rsecond) {
			System.out.println(rsid+" are monomorphic in both datasets, and flipping helps: "+gfirst+" "+gsecond+" "+rfirst+" "+rsecond);
			out.println(rsid);
		    }
		    else {
			System.out.println(rsid+" are monomorphic in both datasets, and flipping does not help: "+gfirst+" "+gsecond+" "+rfirst+" "+rsecond);
			outu.println(rsid+" "+gfirst+" "+gsecond+" "+rfirst+" "+rsecond);
		    }
		}
	    }
	    else {
		System.out.println(rsid+" SNP does not have correct allele combination");
	    }
	    boolean isATGCfirst = isATGCSNP(gfirst, gsecond);
	    boolean isATGCsecond = isATGCSNP(rfirst, rsecond);
	    if (isATGCfirst || isATGCsecond) {
		if ((isATGCfirst && !isATGCsecond) || (!isATGCfirst && isATGCsecond)) {
		    System.out.println(rsid+" "+gfirst+" "+gsecond+" "+rfirst+" "+rsecond);
		}
		outa.println(rsid);
	    }
	}
	out.close();
	outu.close();
	outa.close();
    }

    public static void main(String[] args) throws Exception {
	if (args.length != 5) {
	    System.err.println("[usage] java StrandCorrectionAlleleMismatchSNPs [First bim file] [Second bim file] [output allele mismatch SNP list] [output update allele list] [A/T G/C SNP list]");
	    System.exit(-1);
	}
	StrandCorrectionAlleleMismatchSNPs cc = new StrandCorrectionAlleleMismatchSNPs();
	cc.createFiles(args[0], args[1], args[2], args[3], args[4]);
    }
}

