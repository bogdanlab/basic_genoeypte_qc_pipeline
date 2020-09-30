import java.io.*;
import java.util.*;
import java.lang.*;
//import org.apache.commons.math.stat.descriptive.*;

public class AnalyzeIBDMatrix {

    public void readFamfile(String famfile, ArrayList<String> indList, Hashtable<String,Integer> indInfo, Hashtable<Integer,String> indexInfo) throws Exception {
        BufferedReader in = new BufferedReader(new FileReader(famfile));
        String line = in.readLine();
        int index = 0;
        while (line != null) {
            String[] tokens = line.split("\\s");
            indList.add(new String(tokens[1]));
	    if (indInfo.containsKey(tokens[1])) {
		System.err.println(tokens[1]+" appears twice in fam file");
		System.exit(-1);
	    }
            indInfo.put(new String(tokens[1]), new Integer(index));
	    indexInfo.put(new Integer(index), new String(tokens[1]));
            line = in.readLine();
            index++;
        }
        in.close();
    }

    public void readIBDMatrix(String file, double[][] ibd) throws Exception {
	BufferedReader in = new BufferedReader(new FileReader(file));
	int numInd = ibd[0].length;
	for (int i = 0; i < numInd; i++) {
	    String line = in.readLine();
	    String[] tokens = line.split("\\s");
	    if (tokens.length != numInd) {
		System.err.println("# of columns != "+numInd);
		System.exit(-1);
	    }
	    for (int j = 0; j < numInd; j++) {
		ibd[i][j] = Double.parseDouble(tokens[j]);
	    }
	    if (i % 1000 == 0) {
		System.out.println("read "+i+"th individual's IBD matrix");
	    }
	}
	String line = in.readLine();
	if (line != null) {
	    System.err.println("unexpected end of line for ibd matrix");
	    System.exit(-1);
	}
        in.close();
    }

    public String createMergedID(int i, int j) throws Exception {
	Integer istr = new Integer(i);
	Integer jstr = new Integer(j);
	String mergedid = "";
	if (i < j) {
	    mergedid = istr.toString()+":"+jstr.toString();
	}
	else {
	    mergedid = jstr.toString()+":"+istr.toString();
	}
	return mergedid;
    }

    public void checkUnknownDuplicate(ArrayList<String> indList, Hashtable<String,Integer> indInfo, Hashtable<Integer,String> indexInfo, double[][] ibd, Hashtable<String,Integer> knownDupIndex, String outfile) throws Exception {
	System.out.println("checking Unknown duplicates, # of known duplicate pairs = "+knownDupIndex.size());
	PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outfile+".unknown.duplicate.csv")));
	out.println("ID1,ID2,PI_HAT,SimilarID");
	int numInd = indList.size();
	int numDup = 0;
	int numSubsetID = 0;
	for (int i = 0; i < numInd; i++) {
	    for (int j = i+1; j < numInd; j++) {
		if (ibd[i][j] > 0.9) {
                    String mergedid = createMergedID(i,j);
		    if (!indexInfo.containsKey(i)) {
			System.err.println(i+" does not appear in index info");
		    }
		    if (!indexInfo.containsKey(j)) {
			System.err.println(j+" does not appear in index info");
		    }
		    String firstid = indexInfo.get(i);
		    String secondid = indexInfo.get(j);
		    System.out.println(firstid+" "+secondid+" has IBD > 0.9 = "+ibd[i][j]);
		    knownDupIndex.put(mergedid, new Integer(0));
		    boolean isSubset = false;
		    if (firstid.indexOf(secondid) >=0 || secondid.indexOf(firstid) >= 0) {
			isSubset = true;
			numSubsetID++;
		    }
		    out.println(firstid+","+secondid+","+ibd[i][j]+","+isSubset);
		    numDup++;
		}
	    }
	}
	out.close();
	System.out.println("# of unknown duplicates = "+numDup+" and # of pairs with similar IDs = "+numSubsetID);
    }

    public void readList(ArrayList<String> list, String line) throws Exception {
	String[] tokens = line.split("\\s");
	for (int i = 0; i < tokens.length; i++) {
	    list.add(tokens[i]);
	}
    }

    public void checkFamily(ArrayList<String> parent, ArrayList<String> kid, Hashtable<String,Integer> indInfo, double[][] ibd, Hashtable<String,Integer> knownRelatedIndex, PrintWriter out) throws Exception {
	for (int i = 0; i < parent.size(); i++) {
	    String patid = parent.get(i);
	    if (!indInfo.containsKey(patid)) {
		System.err.println(patid+" does not appear in indInfo");
		System.exit(-1);
	    }
	    int patindex = indInfo.get(patid);
	    for (int j = 0; j < kid.size(); j++) {
		String kidid = kid.get(j);
		if (!indInfo.containsKey(kidid)) {
		    System.err.println(kidid+" does not appear in indInfo");
		    System.exit(-1);
		}
		int kidindex = indInfo.get(kidid);
                String mergedid = createMergedID(kidindex, patindex);
                if (knownRelatedIndex.containsKey(mergedid)) {
		    System.out.println(patid+" "+kidid+" already computed, skip this");
		    continue;
		}
		//System.out.println("found HapMap relatedness: "+patid+" "+kidid+" "+ibd[patindex][kidindex]);
		if (ibd[patindex][kidindex] < 0.45 || ibd[patindex][kidindex] > 0.55) {
		    System.err.println("Parent: "+patid+" Kid; "+kidid+" has IBD < 0.45 or IBD > 0.55 = "+ibd[patindex][kidindex]);
		}
                knownRelatedIndex.put(mergedid, new Integer(0));
		out.println(kidid+","+patid+","+ibd[patindex][kidindex]);
	    }
	}
    }

    public class Related {
	String id;
	int numRelated;
    }

    public class RelatedComparator implements Comparator<Object> {
        public int compare(Object o1, Object o2) {
            Related ind1 = (Related)o1;
            Related ind2 = (Related)o2;
            if (ind1.numRelated > ind2.numRelated) return -1;
            return 1;
        }
    }

    public void checkUnknownRelated(ArrayList<String> indList, Hashtable<String,Integer> indInfo, Hashtable<Integer,String> indexInfo, double[][] ibd, Hashtable<String,Integer> knownDupIndex, Hashtable<String,Integer> knownRelatedIndex, double ibdThreshold, boolean writeIBD, String outfile) throws Exception {
	System.out.println("checking Unknown related, # of known duplicate pairs = "+knownDupIndex.size()+" # of known related pairs "+knownRelatedIndex.size());
	int numInd = indList.size();
	Hashtable<String,Integer> relatedCount = new Hashtable<String,Integer>();
	PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outfile+".unknown.related.ibd."+ibdThreshold+".csv")));
	out.println("ID1,ID2,PI_HAT");
	Hashtable<String,TreeSet<String>> relatedInfo = new Hashtable<String,TreeSet<String>>();
	TreeSet<Related> relatedInfoList = new TreeSet<Related>(new RelatedComparator());
	for (int i = 0; i < numInd; i++) {
	    String firstid = indexInfo.get(i);
	    TreeSet<String> relatedList = new TreeSet<String>();
	    for (int j = (i+1); j < numInd; j++) {
		if (ibd[i][j] > ibdThreshold) {
                    String mergedid = createMergedID(i,j);
                    if (knownDupIndex.containsKey(mergedid)) {
			continue;
		    }
		    if (knownRelatedIndex.containsKey(mergedid)) {
			continue;
		    }
		    if (!indexInfo.containsKey(i)) {
			System.err.println(i+" does not appear in index info");
		    }
		    if (!indexInfo.containsKey(j)) {
			System.err.println(j+" does not appear in index info");
		    }
		    String secondid = indexInfo.get(j);
		    //System.out.println(firstid+" "+secondid+" has IBD > "+ibdThreshold+" = "+ibd[i][j]);
		    if (writeIBD) {
			knownRelatedIndex.put(mergedid, new Integer(0));
		    }
		    if (!writeIBD) {
			if (relatedCount.containsKey(firstid)) {
			    int count = relatedCount.get(firstid);
			    count++;
			    relatedCount.put(firstid, new Integer(count));
			}
			else {
			    relatedCount.put(firstid, new Integer(1));
			}
			if (relatedCount.containsKey(secondid)) {
			    int count = relatedCount.get(secondid);
			    count++;
			    relatedCount.put(secondid, new Integer(count));
			}
			else {
			    relatedCount.put(secondid, new Integer(1));
			}
			relatedList.add(secondid);
		    }
		    else {
			out.println(firstid+","+secondid+","+ibd[i][j]);
		    }
		}
	    }
	    if (!writeIBD && relatedList.size() > 0) {
		if (relatedInfo.containsKey(firstid)) {
		    System.err.println(firstid+" appears twice");
		    System.exit(-1);
		}
		relatedInfo.put(firstid, relatedList);
		Related related = new Related();
		related.id = firstid;
		related.numRelated = relatedList.size();
		relatedInfoList.add(related);
	    }
	    /*
	    if (!writeIBD) {
		if (relatedList.size() > 0) {
		    out.print(firstid);
		    Iterator ite = relatedList.iterator();
		    while (ite.hasNext()) {
			String id = (String)ite.next();
			out.print(","+id);
		    }
		    out.println();
		}
	    }
	    */
	}
	if (!writeIBD) {
	    Iterator ite = relatedInfoList.iterator();
	    Hashtable<String,Integer> singleRelated = new Hashtable<String,Integer>();
	    while (ite.hasNext()) {
		Related related = (Related)ite.next();
		if (!relatedInfo.containsKey(related.id)) {
		    System.err.println(related.id+" does not appear in relatedInfo");
		}
		TreeSet<String> relatedList = relatedInfo.get(related.id);
		if (related.numRelated == 1) {
		    String relatedid = relatedList.first();
		    int firstid = indInfo.get(related.id);
		    int secondid = indInfo.get(relatedid);
		    String tempmergedid = createMergedID(firstid,secondid);
		    if (!singleRelated.containsKey(tempmergedid)) {
			out.println(related.id+",1,"+relatedid);
			singleRelated.put(tempmergedid, new Integer(0));
		    }
		}
		else {
		    out.print(related.id+","+related.numRelated);
		    Iterator ite2 = relatedList.iterator();
		    int numTemp = 0;
		    while (ite2.hasNext()) {
			String id2 = (String)ite2.next();
			out.print(","+id2);
			numTemp++;
		    }
		    if (related.numRelated != numTemp) {
			System.err.println("number of IDs do not match "+related.numRelated+" "+numTemp);
			System.exit(-1);
		    }
		    out.println();
		}
	    }
	    PrintWriter out2 = new PrintWriter(new BufferedWriter(new FileWriter(outfile+".unknown.related.ibd."+ibdThreshold+".related.count.csv")));
	    TreeSet<Related> temp2 = new TreeSet<Related>(new RelatedComparator());
	    Enumeration keys = relatedCount.keys();
	    while (keys.hasMoreElements()) {
		String id = (String)keys.nextElement();
		int count = relatedCount.get(id);
		Related temprelated = new Related();
		temprelated.id = id;
		temprelated.numRelated = count;
		temp2.add(temprelated);
	    }
	    Iterator ite2 = temp2.iterator();
	    while (ite2.hasNext()) {
		Related temprelated = (Related)ite2.next();
		out2.println(temprelated.id+","+temprelated.numRelated);
	    }
	    out2.close();
	}
	out.close();
    }

    public void createFiles(String famfile, String ibdfile, String outfile, String type) throws Exception {
        ArrayList<String> indList = new ArrayList<String>();
        Hashtable<String,Integer> indInfo = new Hashtable<String,Integer>();
        Hashtable<Integer,String> indexInfo = new Hashtable<Integer,String>();
        readFamfile(famfile, indList, indInfo, indexInfo);

        int numInd = indList.size();
        double[][] ibd = new double[numInd][numInd];
	readIBDMatrix(ibdfile, ibd);

	Hashtable<String,Integer> knownDupIndex = new Hashtable<String,Integer>();
        Hashtable<String,Integer> knownRelatedIndex = new Hashtable<String,Integer>();

	checkUnknownDuplicate(indList, indInfo, indexInfo, ibd, knownDupIndex, outfile);

	checkUnknownRelated(indList, indInfo, indexInfo, ibd, knownDupIndex, knownRelatedIndex, 0.4, true, outfile);
	checkUnknownRelated(indList, indInfo, indexInfo, ibd, knownDupIndex, knownRelatedIndex, 0.25, true, outfile);
	if (type.compareTo("1") == 0) {
	    checkUnknownRelated(indList, indInfo, indexInfo, ibd, knownDupIndex, knownRelatedIndex, 0.1, true, outfile);
	}
	else {
	    checkUnknownRelated(indList, indInfo, indexInfo, ibd, knownDupIndex, knownRelatedIndex, 0.1, false, outfile);
	}
    }

    public static void main(String[] args) throws Exception {
	if (args.length != 4) {
	    System.err.println("[usage] java AnalyzeIBDMatrix [fam file] [ibd matrix file] [output file] [type]");
	    System.exit(-1);
	}
	AnalyzeIBDMatrix cc = new AnalyzeIBDMatrix();
	cc.createFiles(args[0], args[1], args[2], args[3]);
    }
}
