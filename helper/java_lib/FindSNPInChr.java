import java.io.*;
import java.util.*;
import java.util.zip.*;

public class FindSNPInChr {

    public void printErrorMessage(String error) throws Exception {
        System.err.println("ERROR_in_FindSNPInChr : "+error);
	System.exit(-1);
    }

    public void createFiles(String bimfile, int targetchr, String outputfile) throws Exception {
	BufferedReader in = new BufferedReader(new FileReader(bimfile));
	PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputfile)));
	String line = in.readLine();
	int numTargetSNP = 0;
	while (line != null) {
	    String[] token = line.split("\\s");
	    int chr = Integer.parseInt(token[0]);
	    if (chr == targetchr) {
		out.println(token[1]);
		numTargetSNP++;
	    }
	    line = in.readLine();
	}
	in.close();
	out.close();
	System.out.println("# of SNPs in chr "+targetchr+" = "+numTargetSNP);
    }

    public static void main(String[] args) throws Exception {
	if (args.length != 3) {
	    System.err.println("[usage] java FindSNPInChr [bim file] [chr number] [output file]");
	    System.exit(-1);
	}
	FindSNPInChr cc = new FindSNPInChr();
	cc.createFiles(args[0], Integer.parseInt(args[1]), args[2]);
    }
}

