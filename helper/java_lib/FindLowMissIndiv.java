import java.io.*;
import java.util.*;
import java.util.zip.*;

public class FindLowMissIndiv {

    public void printErrorMessage(String error) throws Exception {
        System.err.println("ERROR_in_FindLowMissIndiv : "+error);
	System.exit(-1);
    }

    public void handleOuptut(Hashtable<String,Double> missInfo, String inputfile, String outputfile, boolean append) throws Exception {
	PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputfile, append)));
	BufferedReader in = new BufferedReader(new FileReader(inputfile));
        String line = in.readLine();
        line = in.readLine();
        while (line != null) {
	    String[] token = line.split(",");
	    String id1 = token[0];
	    String id2 = token[1];
	    if (!missInfo.containsKey(id1)) {
		printErrorMessage(id1+" does not have missing rate info");
	    }
	    if (!missInfo.containsKey(id2)) {
		printErrorMessage(id2+" does not have missing rate info");
	    }
	    double miss1 = missInfo.get(id1);
	    double miss2 = missInfo.get(id2);
	    if (miss1 < miss2) {
		out.println(id2+" "+id2);
	    }
	    else {
		out.println(id1+" "+id1);
	    }
            line = in.readLine();
        }
        in.close();
	out.close();
    }

    public void createFiles(String imissfile, String ibdprefix, String outputdupfile, String outputrelfile) throws Exception {
	Hashtable<String,Double> missInfo = new Hashtable<String,Double>();
	BufferedReader in = new BufferedReader(new FileReader(imissfile));
	String line = in.readLine();
	line = in.readLine();
	while (line != null) {
	    StringTokenizer stk = new StringTokenizer(line);
	    String fid = stk.nextToken();
	    String iid = stk.nextToken();
	    String misspheno = stk.nextToken();
	    String nmiss = stk.nextToken();
	    String ngeno = stk.nextToken();
	    double fmiss = Double.parseDouble(stk.nextToken());
	    if (missInfo.containsKey(iid)) {
		printErrorMessage(iid+" appears twice in "+imissfile);
	    }
	    missInfo.put(iid, fmiss);
	    line = in.readLine();
	}
	in.close();

	handleOuptut(missInfo, ibdprefix+".unknown.duplicate.csv", outputdupfile, false);
	handleOuptut(missInfo, ibdprefix+".unknown.related.ibd.0.4.csv", outputrelfile, false);
	handleOuptut(missInfo, ibdprefix+".unknown.related.ibd.0.25.csv", outputrelfile, true);
    }

    public static void main(String[] args) throws Exception {
	if (args.length != 4) {
	    System.err.println("[usage] java FindLowMissIndiv [imiss file] [ibd output prefix] [output dup txt] [output related txt]");
	    System.exit(-1);
	}
	FindLowMissIndiv cc = new FindLowMissIndiv();
	cc.createFiles(args[0], args[1], args[2], args[3]);
    }
}

