import java.io.*;
import java.util.*;
import java.lang.*;

public class CreateIBDMatrix {

    public void readFamfile(String famfile, ArrayList<String> indList, Hashtable<String,Integer> indInfo) throws Exception {
	BufferedReader in = new BufferedReader(new FileReader(famfile));
	String line = in.readLine();
	int index = 0;
	while (line != null) {
	    String[] tokens = line.split("\\s");
	    indList.add(new String(tokens[1]));
	    indInfo.put(new String(tokens[1]), new Integer(index));
	    //indList.add(new String(tokens[0]+":"+tokens[1]));
	    //indInfo.put(new String(tokens[0]+":"+tokens[1]), new Integer(index));
	    line = in.readLine();
	    index++;
	}
	in.close();
    }

    public int readIBDfile(String ibdfile, Hashtable<String,Integer> indInfo, double[][] ibd, boolean[][] isSet) throws Exception {
	String filename = ibdfile;
	BufferedReader in = null;
	try {
	    in = new BufferedReader(new FileReader(filename));
	}
	catch (FileNotFoundException e) {
	    System.err.println(filename+" not found");
	    return 0;
	}
	int numLine = 0;
	try {
	    String line = in.readLine();
	    line = in.readLine();
	    while (line != null) {
		StringTokenizer stk = new StringTokenizer(line);
		String firstfamid = stk.nextToken(); //FID1
		String firstid = new String(stk.nextToken()); // IID1
		String secondfamid = stk.nextToken(); //FID2
		String secondid = new String(stk.nextToken()); // IID2
		for (int i = 0; i < 5; i++) {
		    stk.nextToken();
		}
		double pihat = Double.parseDouble(stk.nextToken());
		//firstid = firstfamid+":"+firstid;
		//secondid = secondfamid+":"+secondid;
		if (!indInfo.containsKey(firstid)) {
		    System.err.println(firstid+" does not appear in fam file");
		}
		if (!indInfo.containsKey(secondid)) {
		    System.err.println(secondid+" does not appear in fam file");
		}
		int findex = indInfo.get(firstid);
		int sindex = indInfo.get(secondid);
		if (isSet[findex][sindex]) {
		    if (ibd[findex][sindex] != pihat) {
			System.err.println("Differe piHat between the same two pairs: "+firstid+" "+secondid);
			System.exit(-1);
		    }
		}
		else {
		    ibd[findex][sindex] = pihat;
		    ibd[sindex][findex] = pihat;
		    isSet[findex][sindex] = true;
		    isSet[sindex][findex] = true;
		}
		line = in.readLine();
		numLine++;
	    }
	    in.close();
	}
	catch (Exception e) {
            System.err.println(filename+" has problem");
            return numLine;
	}
	return numLine;
    }

    public void createFiles(String famfile, String ibdfile, String outputfile) throws Exception {
	ArrayList<String> indList = new ArrayList<String>();
	Hashtable<String,Integer> indInfo = new Hashtable<String,Integer>();
	readFamfile(famfile, indList, indInfo);

	int numInds = indList.size();
	double[][] ibd = new double[numInds][numInds];
	boolean[][] isSet = new boolean[numInds][numInds];
	for (int i = 0; i < numInds; i++) {
	    for (int j = 0; j < numInds; j++) {
		isSet[i][j] = false;
	    }
	}
	int numLine = 0;
	int numLineTemp = readIBDfile(ibdfile, indInfo, ibd, isSet);
	for (int i = 0; i < numInds; i++) {
	    for (int j = 0; j < numInds; j++) {
		if (!isSet[i][j]) {
		    if (i != j) {
			System.err.println(i+" "+j+" don't have IBD value");
		    }
		}
		if (ibd[i][j] != ibd[j][i]) {
		    System.err.println(i+" "+j+" ibd value not symmetric");
		}
	    }
	}
	PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputfile)));
	for (int i = 0; i < numInds; i++) {
	    for (int j = 0; j < numInds; j++) {
		out.print(ibd[i][j]);
		if (j == numInds - 1) {
		    out.println();
		}
		else {
		    out.print(" ");
		}
	    }
	}
	out.close();
    }

    public static void main(String[] args) throws Exception {
	if (args.length != 3) {
	    System.err.println("[usage] java CreateIBDMatrix [fam file] [ibd file] [output file]");
	    System.exit(-1);
	}
	CreateIBDMatrix cc = new CreateIBDMatrix();
	cc.createFiles(args[0], args[1], args[2]);
    }
}

