import java.io.*;
import java.util.*;
import java.util.zip.*;

public class CheckACMGAllele {

    public void printErrorMessage(String error) throws Exception {
        System.err.println("ERROR_in_CheckACMGAllele : "+error);
	System.exit(-1);
    }

    public void readACMGFile(String acmgfile, Hashtable<String,Hashtable<String,ArrayList<String>>> acmgInfo) throws Exception {
	Hashtable<String,Integer> diseaseInfo = new Hashtable<String,Integer>();
	BufferedReader in = new BufferedReader(new FileReader(acmgfile));
	String line = in.readLine();
	line = in.readLine();
	while (line != null) {
	    String[] token = line.split("\\t");
	    String chr = token[0];
	    String pos = token[1];
	    String altallele = token[3];
	    String disease = token[4];
	    String chrpos = chr+":"+pos;
	    if (diseaseInfo.containsKey(disease)) {
		int count = diseaseInfo.get(disease);
		count++;
		diseaseInfo.put(disease,count);
	    }
	    else {
		diseaseInfo.put(disease, 1);
	    }
	    if (acmgInfo.containsKey(chrpos)) {
		Hashtable<String,ArrayList<String>> tempInfo = acmgInfo.get(chrpos);
		ArrayList<String> tempList = null;
		if (tempInfo.containsKey(altallele)) {
		    tempList = tempInfo.get(altallele);
		}
		else {
		    tempList = new ArrayList<String>();
		}
                tempList.add(disease);
                tempInfo.put(altallele, tempList);
                acmgInfo.put(chrpos, tempInfo);
	    }
	    else {
		Hashtable<String,ArrayList<String>> tempInfo = new Hashtable<String,ArrayList<String>>();
		ArrayList<String> tempList = new ArrayList<String>();
		tempList.add(disease);
		tempInfo.put(altallele, tempList);
		acmgInfo.put(chrpos, tempInfo);
	    }
	    line = in.readLine();
	}
	in.close();
	System.out.println("# of unique diseases in ACMG = "+diseaseInfo.size());
        Enumeration<String> key = diseaseInfo.keys();
        while (key.hasMoreElements()) {
            String disease = key.nextElement();
            int count = diseaseInfo.get(disease);
            System.out.println(disease+"\t"+count);
        }
	System.out.println("##############################");
    }

    public void createFiles(String acmgfile, String pedfileprefix, String outputfile, String outputaffile) throws Exception {
	Hashtable<String,Hashtable<String,ArrayList<String>>> acmgInfo = new Hashtable<String,Hashtable<String,ArrayList<String>>>();
	readACMGFile(acmgfile, acmgInfo);
	System.out.println("# of lines in ACMG = "+acmgInfo.size());

	ArrayList<String> chrposList = new ArrayList<String>();
	ArrayList<String> rsidList = new ArrayList<String>();
	BufferedReader in = new BufferedReader(new FileReader(pedfileprefix+".map"));
	String line = in.readLine();
	while (line != null) {
	    String[] token = line.split("\\s");
	    String chrpos = token[0]+":"+token[3];
	    if (!acmgInfo.containsKey(chrpos)) {
		printErrorMessage(chrpos+" does not appear in ACMG file");
	    }
	    chrposList.add(chrpos);
	    rsidList.add(token[1]);
	    line = in.readLine();
	}
	in.close();

	PrintWriter out2 = new PrintWriter(new BufferedWriter(new FileWriter(outputaffile)));
	in = new BufferedReader(new FileReader(pedfileprefix+".freq.frq"));
	line = in.readLine();
	line = in.readLine();
	int snpIndex = 0;
	Hashtable<Integer,Boolean> skipSNPInfo = new Hashtable<Integer,Boolean>();
	while (line != null) {
	    StringTokenizer stk = new StringTokenizer(line);
	    String chr = stk.nextToken();
	    String rsid = stk.nextToken();
	    String a1 = stk.nextToken();
	    String a2 = stk.nextToken();
	    String mafstr = stk.nextToken();
	    String rsid2 = rsidList.get(snpIndex);
	    if (rsid.compareTo(rsid2) != 0) {
		printErrorMessage("unexpected SNP in frq file "+rsid+" "+rsid2);
	    }
	    double a1f = -1;
	    if (a1.compareTo("0") == 0) {
		a1f = 0;
	    }
	    else {
		a1f = Double.parseDouble(mafstr);
	    }
	    if (a2.compareTo("0") == 0) {
		printErrorMessage(rsid+" has major allele of 0");
	    }
	    double a2f = 1.0-Double.parseDouble(mafstr);
	    String chrpos = chrposList.get(snpIndex);
	    Hashtable<String,ArrayList<String>> altDiseaseInfo = acmgInfo.get(chrpos);
	    String altallele = "";
	    double altaf = -1;
	    if (a1.compareTo("0") != 0 && altDiseaseInfo.containsKey(a1)) {
		if (altDiseaseInfo.containsKey(a2)) {
		    printErrorMessage(rsid+" has both alleles in ACMG "+a1+" "+a2);
		}
		altallele = a1;
		altaf = a1f;
	    }
	    else if (altDiseaseInfo.containsKey(a2)) {
		altallele = a2;
		altaf = a2f;
	    }
	    else {
		altallele = "?";
		altaf = 0;
	    }
	    if (altaf > 0.05) {
		System.out.println(rsid+" ALT allele = "+altallele+" has MAF "+altaf+", which is > 5%, so skipping this SNP");
		skipSNPInfo.put(snpIndex, true);
	    }
	    out2.println(chrpos+"\t"+rsid+"\t"+altallele+"\t"+altaf);
	    snpIndex++;
	    line = in.readLine();
	}
	in.close();
	out2.close();

	Hashtable<String,Integer> diseaseInfo = new Hashtable<String,Integer>();
	Hashtable<Integer,Integer> numDiseaseInfo = new Hashtable<Integer,Integer>();
	PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputfile)));
	in = new BufferedReader(new FileReader(pedfileprefix+".ped"));
        line = in.readLine();
        while (line != null) {
            String[] token = line.split("\\s");
	    String indID = token[0];
	    ArrayList<String> allDiseaseList = new ArrayList<String>();
	    for (int i = 0; i < chrposList.size(); i++) {
		if (skipSNPInfo.containsKey(i)) {
		    continue;
		}
		int fcharindex = 6+(i*2);
		int scharindex = 6+(i*2)+1;
		String fallele = token[fcharindex];
		String sallele = token[scharindex];
		String chrpos = chrposList.get(i);
		String rsid = rsidList.get(i);
		Hashtable<String,ArrayList<String>> altDiseaseInfo = acmgInfo.get(chrpos);
		if (altDiseaseInfo.containsKey(fallele)) {
		    ArrayList<String> diseaseList = altDiseaseInfo.get(fallele);
		    for (int j = 0; j < diseaseList.size(); j++) {
			String disease = diseaseList.get(j);
			if (diseaseInfo.containsKey(disease)) {
			    int count = diseaseInfo.get(disease);
			    count++;
			    diseaseInfo.put(disease,count);
			}
			else {
			    diseaseInfo.put(disease, 1);
			}
			allDiseaseList.add(disease+":chr"+chrpos+":"+rsid);
		    }
		}
                if (sallele.compareTo(fallele) != 0 && altDiseaseInfo.containsKey(sallele)) {
                    ArrayList<String> diseaseList = altDiseaseInfo.get(sallele);
                    for (int j = 0; j < diseaseList.size(); j++) {
			String disease = diseaseList.get(j);
                        if (diseaseInfo.containsKey(disease)) {
                            int count = diseaseInfo.get(disease);
                            count++;
                            diseaseInfo.put(disease,count);
                        }
                        else {
                            diseaseInfo.put(disease, 1);
			}
                        allDiseaseList.add(disease+":chr"+chrpos+":"+rsid);
                    }
                }
	    }
	    int numDisease = allDiseaseList.size();
	    out.print(indID+"\t"+numDisease);
	    if (numDiseaseInfo.containsKey(numDisease)) {
		int count = numDiseaseInfo.get(numDisease);
		count++;
		numDiseaseInfo.put(numDisease, count);
	    }
	    else {
		numDiseaseInfo.put(numDisease, 1);
	    }
	    for (int i = 0; i < allDiseaseList.size(); i++) {
		out.print("\t"+allDiseaseList.get(i));
	    }
	    out.println();
            line = in.readLine();
	}
        in.close();
	out.close();
	Enumeration<String> key = diseaseInfo.keys();
	while (key.hasMoreElements()) {
	    String disease = key.nextElement();
	    int count = diseaseInfo.get(disease);
	    System.out.println(disease+"\t"+count);
	}
	System.out.println("##############################");
	Enumeration<Integer> key2 = numDiseaseInfo.keys();
	while (key2.hasMoreElements()) {
	    int numDisease = key2.nextElement();
	    int count = numDiseaseInfo.get(numDisease);
	    System.out.println(numDisease+"\t"+count);
	}
    }

    public static void main(String[] args) throws Exception {
	if (args.length != 4) {
	    System.err.println("[usage] java CheckACMGAllele [acmg file] [ped file prefix] [output file] [output ALT AF file]");
	    System.exit(-1);
	}
	CheckACMGAllele cc = new CheckACMGAllele();
	cc.createFiles(args[0], args[1], args[2], args[3]);
    }
}

