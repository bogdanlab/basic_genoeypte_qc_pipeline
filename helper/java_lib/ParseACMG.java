import java.io.*;
import java.util.*;
import java.util.zip.*;

public class ParseACMG {

    public void printErrorMessage(String error) throws Exception {
        System.err.println("ERROR_in_ParseACMG : "+error);
	System.exit(-1);
    }

    public boolean parseRefAltAllele(String name, ArrayList<String> refAltAlleleList) throws Exception {
	int index = name.indexOf(">");
	if (index < 0) {
	    return false;
	}
	String firstAllele = (new Character(name.charAt(index-1))).toString();
	String secondAllele = (new Character(name.charAt(index+1))).toString();
	if (firstAllele.compareTo("A") == 0 || firstAllele.compareTo("C") == 0 || firstAllele.compareTo("G") == 0 || firstAllele.compareTo("T") == 0) {
	    refAltAlleleList.add(firstAllele);
	}
	else {
	    System.out.println("unrecognized allele "+firstAllele+" in "+name);
	    return false;
	}

	if (secondAllele.compareTo("A") == 0 || secondAllele.compareTo("C") == 0 || secondAllele.compareTo("G") == 0 || secondAllele.compareTo("T") == 0) {
	    refAltAlleleList.add(secondAllele);
	}
	else {
	    System.out.println("unrecognized allele "+secondAllele+" in "+name);
	    return false;
	}
	return true;
    }

    public void createFiles(String inputfile, String outputfile) throws Exception {
	PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputfile)));
	BufferedReader in = new BufferedReader(new FileReader(inputfile));
	String line = in.readLine();
	line = in.readLine();
	out.println("GRCh37Chromosome\tGRCh37Location\tRef_Allele\tAlt_Allele\tDisease\tName\tGene(s)\tCondition(s)\tClinical_significance\tReview_status");
	int numDiffColumn = 0;
	int unParsableName = 0;
	int numLine = 0;
	while (line != null) {
	    numLine++;
	    String[] token = line.split("\\t");
	    if (token.length != 12) {
		numDiffColumn++;
		line = in.readLine();
		continue;
	    }
	    String name = token[1];
	    ArrayList<String> refAltAlleleList = new ArrayList<String>();
	    boolean success = parseRefAltAllele(name, refAltAlleleList);
	    if (!success) {
		unParsableName++;
                line = in.readLine();
                continue;
	    }
	    String chr = token[6];
	    if (chr.compareTo("") == 0) {
                unParsableName++;
                line = in.readLine();
                continue;
	    }
	    String[] chrtoken = chr.split("\\|");
	    //System.out.println(chr+" "+chrtoken.length);
	    if (chrtoken.length > 2) {
		unParsableName++;
                line = in.readLine();
                continue;
	    }
	    if (chrtoken.length == 2) {
		if (chrtoken[0].compareTo(chrtoken[1]) != 0) {
		    unParsableName++;
		    line = in.readLine();
		    continue;
		}
		else {
		    chr = chrtoken[0];
		}
	    }
	    if (chr.compareTo("X") == 0) {
		chr = "23";
	    }
	    else if (chr.compareTo("Y") == 0) {
		chr = "24";
	    }
	    int chrint = Integer.parseInt(chr);
	    out.println(chr+"\t"+token[7]+"\t"+refAltAlleleList.get(0)+"\t"+refAltAlleleList.get(1)+"\t"+token[0]+"\t"+token[1]+"\t"+token[2]+"\t"+token[3]+"\t"+token[4]+"\t"+token[5]);
	    line = in.readLine();
	}
	in.close();
	out.close();
	System.out.println("# of variants = "+numLine);
	System.out.println("# of incorrect # of columns = "+numDiffColumn);
	System.out.println("# of unparsable line = "+unParsableName);
    }

    public static void main(String[] args) throws Exception {
	if (args.length != 2) {
	    System.err.println("[usage] java ParseACMG [input file] [output file]");
	    System.exit(-1);
	}
	ParseACMG cc = new ParseACMG();
	cc.createFiles(args[0], args[1]);
    }
}

