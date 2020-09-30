import java.io.*;
import java.util.*;
import java.lang.*;

public class RemoveGeneticDistBim {

    public void createFiles(String inputfile) throws Exception {
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
	    out.println(tokens[0]+"\t"+tokens[1]+"\t0\t"+tokens[3]+"\t"+tokens[4]+"\t"+tokens[5]);
	    line = in.readLine();
	}
	in.close();
	out.close();
    }

    public static void main(String[] args) throws Exception {
	if (args.length != 1) {
	    System.err.println("[usage] java RemoveGeneticDistBim [input bim file (without .orig suffix)]");
	    System.exit(-1);
	}
	RemoveGeneticDistBim cc = new RemoveGeneticDistBim();
	cc.createFiles(args[0]);
    }
}

