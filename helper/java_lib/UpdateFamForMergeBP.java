import java.io.*;
import java.util.*;
import java.lang.*;

public class UpdateFamForMergeBP {

    public void createFiles(String inputfile, String prefix, String changephenocontrol) throws Exception {
	File temp2 = new File(inputfile+".orig");
	if (!temp2.exists()) {
	    File origfilename = new File(inputfile+".orig");
	    File origfile = new File(inputfile);
	    origfile.renameTo(origfilename);
	}
	BufferedReader in = new BufferedReader(new FileReader(inputfile+".orig"));
	PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(inputfile)));
	String line = in.readLine();
	while (line != null) {
	    String[] tokens = line.split("\\s");
	    String newfamid = tokens[0];
	    if (prefix.compareTo("null") != 0) {
		newfamid = prefix+":"+tokens[0];
	    }
	    String newindid = tokens[1];
	    if (prefix.compareTo("null") != 0) {
		newindid = prefix+":"+tokens[1];
	    }
	    String newpatid = tokens[2];
	    if (prefix.compareTo("null") != 0 && newpatid.compareTo("0") != 0) {
		newpatid = prefix+":"+newpatid;
	    }
	    String newmatid = tokens[3];
	    if (prefix.compareTo("null") != 0 && newmatid.compareTo("0") != 0) {
		newmatid = prefix+":"+newmatid;
	    }
	    String phenotype = tokens[5];
	    if (changephenocontrol.compareTo("T") == 0) {
		phenotype = "1";
	    }
	    if (newfamid.compareTo(newindid) != 0) {
		newfamid = newindid;
	    }
	    out.println(newfamid+" "+newindid+" "+newpatid+" "+newmatid+" "+tokens[4]+" "+phenotype);
	    line = in.readLine();
	}
	in.close();
	out.close();
    }

    public static void main(String[] args) throws Exception {
	if (args.length != 3) {
	    System.err.println("[usage] java UpdateFamForMergeBP [input fam file (without .orig suffix)] [prefix to add] [change phenotype to controls? {T/F}]");
	    System.exit(-1);
	}
	UpdateFamForMergeBP cc = new UpdateFamForMergeBP();
	cc.createFiles(args[0], args[1], args[2]);
    }
}

