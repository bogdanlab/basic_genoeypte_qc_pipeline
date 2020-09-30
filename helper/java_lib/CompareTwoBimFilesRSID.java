import java.io.*;
import java.util.*;
import java.lang.*;

public class CompareTwoBimFilesRSID {

    public void createFiles(String inputfile1, String inputfile2, String outputfile, String outputbim, String missingvarfile) throws Exception {
	ArrayList<String> list1 = new ArrayList<String>();
	Hashtable<String,Integer> info1 = new Hashtable<String,Integer>();
	Hashtable<String,String> biminfo = new Hashtable<String,String>();
	BufferedReader in = new BufferedReader(new FileReader(inputfile1));
	String line = in.readLine();
	while (line != null) {
	    String[] tokens = line.split("\\s");
	    int chr = new Integer(tokens[0]);
            String id = tokens[1];
	    info1.put(id,new Integer(0));
	    list1.add(id);
	    biminfo.put(id,line);
	    line = in.readLine();
	}
	in.close();

	ArrayList<String> list2 = new ArrayList<String>();
        Hashtable<String,Integer> info2 = new Hashtable<String,Integer>();
        in = new BufferedReader(new FileReader(inputfile2));
        line = in.readLine();
        while (line != null) {
            String[] tokens = line.split("\\s");
            int chr= new Integer(tokens[0]);
            String id = tokens[1];
            info2.put(id,new Integer(0));
	    list2.add(id);
            line = in.readLine();
        }
        in.close();

	PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputfile)));
	PrintWriter outb = new PrintWriter(new BufferedWriter(new FileWriter(outputbim)));
	PrintWriter outm = new PrintWriter(new BufferedWriter(new FileWriter(missingvarfile)));
	int inOneNotInTwo = 0;
	int inBoth = 0;
	for (int i = 0; i < list1.size(); i++) {
	    String id = list1.get(i);
	    if (!info2.containsKey(id)) {
		inOneNotInTwo++;
		outm.println(biminfo.get(id));
	    }
	    else {
		inBoth++;
		out.println(id);
		outb.println(biminfo.get(id));
	    }
	}
	outm.close();
	System.out.println("# of SNPs in both dataset = "+inBoth);
	System.out.println("# of SNPs in first dataset = "+list1.size());
	System.out.println("# of SNPs in first but not in second dataset = "+inOneNotInTwo);
	System.out.println();
	out.close();
	outb.close();
        int inTwoNotInOne = 0;
        inBoth = 0;
        for (int i = 0; i < list2.size(); i++) {
            String id = list2.get(i);
            if (!info1.containsKey(id)) {
                inTwoNotInOne++;
            }
            else {
                inBoth++;
            }
        }
	System.out.println("# of SNPs in both dataset = "+inBoth);
	System.out.println("# of SNPs in second dataset = "+list2.size());
	System.out.println("# of SNPs in second but not in first dataset = "+inTwoNotInOne);
    }

    public static void main(String[] args) throws Exception {
	if (args.length != 5) {
	    System.err.println("[usage] java CompareTwoBimFilesRSID [input bim file 1] [input bim file 2] [joint SNP list] [joint SNP list bim file] [missed variants file]");
	    System.exit(-1);
	}
	CompareTwoBimFilesRSID cc = new CompareTwoBimFilesRSID();
	cc.createFiles(args[0], args[1], args[2], args[3], args[4]);
    }
}

