import java.io.*;
import java.util.*;
import java.lang.*;

public class ChangeRSIDtoSNPRSID {

    public void createFiles(String snpbimfile, String seqbimfile, String outputfile, String duplicatefile) throws Exception {
	BufferedReader in = new BufferedReader(new FileReader(snpbimfile));	
	String line = in.readLine();
	Hashtable<String,String> posID = new Hashtable<String,String>(100000000);
	int numSNP = 0;
	PrintWriter outd = new PrintWriter(new BufferedWriter(new FileWriter(duplicatefile)));
	while (line != null) {
	    String[] tokens = line.split("\\s");
	    String posid = new String(tokens[0]+":"+tokens[3]);
	    if (posID.containsKey(posid)) {
		System.err.println(posid+" seen twice in SNP bim file");
		outd.println(tokens[1]);
		/*
		if (tokens[1].startsWith("rs")) {
		    String previd = posID.get(posid);
		    outd.println(previd);
		    posID.put(posid, new String(tokens[1]));
		}
		else {
		    outd.println(posid);
		}
		*/
	    }
	    else {
		posID.put(posid, new String(tokens[1]));
	    }
	    line = in.readLine();
	    if (numSNP % 100000 == 0) {
		System.out.println(numSNP+" done");
	    }
	    numSNP++;
	}
	in.close();
	outd.close();

	in = new BufferedReader(new FileReader(seqbimfile));
	line = in.readLine();
	PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputfile)));
	int numJoint = 0;
	while (line != null) {
	    String[] tokens = line.split("\\s");
	    String posid = tokens[0]+":"+tokens[3];
	    String rsid = tokens[1];
	    if (posID.containsKey(posid)) {
		numJoint++;
		rsid = posID.get(posid);
	    }
	    out.println(tokens[0]+"\t"+rsid+"\t"+tokens[2]+"\t"+tokens[3]+"\t"+tokens[4]+"\t"+tokens[5]);
	    line = in.readLine();
	}
	System.out.println("# of joint SNPs = "+numJoint);
	in.close();
	out.close();
    }

    public static void main(String[] args) throws Exception {
	if (args.length != 4) {
	    System.err.println("[usage] java ChangeRSIDtoSNPRSID [SNP bim file] [sequencing bim file] [output bim file] [duplicate SNP file]");
	    System.exit(-1);
	}
	ChangeRSIDtoSNPRSID cc = new ChangeRSIDtoSNPRSID();
	cc.createFiles(args[0], args[1], args[2], args[3]);
    }
}

