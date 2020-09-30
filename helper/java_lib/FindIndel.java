import java.io.*;
import java.util.*;
import java.util.zip.*;

public class FindIndel {

    public void printErrorMessage(String error) throws Exception {
        System.err.println("ERROR_in_FindIndel : "+error);
	System.exit(-1);
    }

    public void createFiles(String bimfile, String outputfile) throws Exception {
	BufferedReader in = new BufferedReader(new FileReader(bimfile));
	PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputfile)));
	String line = in.readLine();
	while (line != null) {
	    String[] token = line.split("\\s");
	    String rsid = token[1];
	    String fallele = token[4];
	    String sallele = token[5];
	    boolean isfail = false;
	    if (!(fallele.compareTo("A") == 0 || fallele.compareTo("C") == 0 || fallele.compareTo("T") == 0 || fallele.compareTo("G") == 0 || fallele.compareTo("0") == 0)) {
		isfail = true;
	    }
	    if (!(sallele.compareTo("A") == 0 || sallele.compareTo("C") == 0 || sallele.compareTo("T") == 0 || sallele.compareTo("G") == 0 || sallele.compareTo("0") == 0)) {
		isfail = true;
	    }
	    if (isfail) {
		out.println(rsid);
	    }
	    line = in.readLine();
	}
	in.close();
	out.close();
    }

    public static void main(String[] args) throws Exception {
	if (args.length != 2) {
	    System.err.println("[usage] java FindIndel [bim file] [output file]");
	    System.exit(-1);
	}
	FindIndel cc = new FindIndel();
	cc.createFiles(args[0], args[1]);
    }
}

