import java.io.*;
import java.util.*;
import java.lang.*;

public class ProcessPCAOutput1KGBPAMR {

    public void readPopinfoFile(String famfile, String popinfofile, Hashtable<String,String> popInfo, Hashtable<String,Integer> popList, Hashtable<Integer,String> popIDInfo, ArrayList<String> popNameList) throws Exception {
	BufferedReader in = new BufferedReader(new FileReader(famfile));
	String line = in.readLine();
	int popID = 0;
	ArrayList<String> temppopNameList = new ArrayList<String>();
	while (line != null) {
	    String[] token = line.split("\\s");
	    String indID = token[0].toUpperCase();
            if (popInfo.containsKey(indID)) {
                System.err.println(indID+" seen twice in pop info file");
                System.exit(-1);
            }
	    String pop = "UCLA";
	    if (indID.startsWith(pop)) {
		popInfo.put(indID, pop);
		if (!popList.containsKey(pop)) {
		    popList.put(pop, popID);
		    popIDInfo.put(popID, pop);
		    temppopNameList.add(pop);
		    popID++;
		}
	    }
	    line = in.readLine();
	}
	in.close();

	in = new BufferedReader(new FileReader(popinfofile));
	line = in.readLine();
	line = in.readLine();
	while (line != null) {
	    String[] tokens = line.split("\\s");
	    String indID = tokens[0];
	    String pop = tokens[1];
	    if (popInfo.containsKey(indID)) {
		System.err.println(indID+" seen twice in pop info file");
		System.exit(-1);
	    }
	    popInfo.put(indID, pop);
	    if (!popList.containsKey(pop)) {
		popList.put(pop, new Integer(popID));
		popIDInfo.put(new Integer(popID), pop);
		temppopNameList.add(pop);
		popID++;
	    }
	    line = in.readLine();
	}
	in.close();
	popNameList.add("UCLA");
	popNameList.add("MXL");
	popNameList.add("PUR");
	popNameList.add("CLM");
	popNameList.add("PEL");
	/*
	popNameList.add("CEU");
	popNameList.add("TSI");
	popNameList.add("FIN");
	popNameList.add("GBR");
	popNameList.add("IBS");
	popNameList.add("CHB");
	popNameList.add("JPT");
	popNameList.add("CHS");
	popNameList.add("CDX");
	popNameList.add("KHV");
	popNameList.add("YRI");
	popNameList.add("LWK");
	popNameList.add("GWD");
	popNameList.add("MSL");
	popNameList.add("ESN");
	popNameList.add("ASW");
	popNameList.add("ACB");
	popNameList.add("GIH");
	popNameList.add("PJL");
	popNameList.add("BEB");
	popNameList.add("STU");
	popNameList.add("ITU");
	*/
    }

    public void readPhenoInfo(String famfile, Hashtable<String,Integer> phenoInfo, Hashtable<String,String> popInfo, Hashtable<String,Integer> popSymbolInfo) throws Exception {
        BufferedReader in = new BufferedReader(new FileReader(famfile));
        String line = in.readLine();
        while (line != null) {
            String[] token = line.split("\\s");
            String indID = token[0].toUpperCase();
	    int pheno = -1;
	    if (indID.startsWith("UCLA")) {
		pheno = 3;
	    }
	    else {
		String pop = popInfo.get(indID);
		if (pop.compareTo("CHB") == 0 || pop.compareTo("JPT") == 0 || pop.compareTo("CHS") == 0 || pop.compareTo("CDX") == 0 || pop.compareTo("KHV") == 0) {
		    pheno = 0;
		}
		else if (pop.compareTo("CEU") == 0 || pop.compareTo("TSI") == 0 || pop.compareTo("FIN") == 0 || pop.compareTo("GBR") == 0 || pop.compareTo("IBS") == 0) {
                    pheno = 20;
		}
		else if (pop.compareTo("YRI") == 0 || pop.compareTo("LWK") == 0 || pop.compareTo("GWD") == 0 || pop.compareTo("MSL") == 0 || pop.compareTo("ESN") == 0 || pop.compareTo("ASW") == 0 || pop.compareTo("ACB") == 0) {
                    pheno = 2;
		}
		else if (pop.compareTo("MXL") == 0 || pop.compareTo("PUR") == 0 || pop.compareTo("CLM") == 0 || pop.compareTo("PEL") == 0) {
                    pheno = 18;
		}
		else if (pop.compareTo("GIH") == 0 || pop.compareTo("PJL") == 0 || pop.compareTo("BEB") == 0 || pop.compareTo("STU") == 0 || pop.compareTo("ITU") == 0) {
                    pheno = 4;
		}
		else {
		    System.err.println("unknown pop "+pop);
		    System.exit(-1);
		}
		if (!popSymbolInfo.containsKey(pop)) {
		    popSymbolInfo.put(pop, pheno);
		}
	    }
	    phenoInfo.put(indID, pheno);
            line = in.readLine();
	}
	in.close();
    }

    public void createFiles(String famfile, String popinfofile, String evecfile, String rcolorfile, String outputdir) throws Exception {
	int numNon1KGPop = 1;
	File temp = new File(outputdir);
	temp.mkdir();

	ArrayList<String> popNameList = new ArrayList<String>();
	Hashtable<String,String> popInfo = new Hashtable<String,String>();
	Hashtable<String,Integer> popList = new Hashtable<String,Integer>();
	Hashtable<Integer,String> popIDInfo = new Hashtable<Integer,String>();
	readPopinfoFile(famfile, popinfofile, popInfo, popList, popIDInfo, popNameList);

	Hashtable<String,Integer> phenoInfo = new Hashtable<String,Integer>();
	Hashtable<String,Integer> popSymbolInfo = new Hashtable<String,Integer>();
	readPhenoInfo(famfile, phenoInfo, popInfo, popSymbolInfo);

	int numPop = popList.size();
	PrintWriter[] out = new PrintWriter[numPop];
	for (int i = 0; i < numPop; i++) {
	    if (!popIDInfo.containsKey(i)) {
		System.err.println(i+" does not appear in popIDInfo");
		System.exit(-1);
	    }
	    String pop = popIDInfo.get(i);
	    out[i] = new PrintWriter(new BufferedWriter(new FileWriter(outputdir+"/"+pop+".evec.txt")));
	}

	BufferedReader in = new BufferedReader(new FileReader(evecfile));
	String line = in.readLine();
	line = in.readLine();
	while (line != null) {
	    StringTokenizer stk = new StringTokenizer(line);
	    String id = stk.nextToken();
	    double pc1 = Double.parseDouble(stk.nextToken());
	    double pc2 = Double.parseDouble(stk.nextToken());
	    double pc3 = Double.parseDouble(stk.nextToken());
	    double pc4 = Double.parseDouble(stk.nextToken());
	    double pc5 = Double.parseDouble(stk.nextToken());
	    double pc6 = Double.parseDouble(stk.nextToken());
	    double pc7 = Double.parseDouble(stk.nextToken());
	    double pc8 = Double.parseDouble(stk.nextToken());
	    double pc9 = Double.parseDouble(stk.nextToken());
	    double pc10 = Double.parseDouble(stk.nextToken());
	    int pheno = 1;
	    if (!phenoInfo.containsKey(id)) {
		pheno = 1;
	    }
	    else {
		pheno = phenoInfo.get(id);
	    }
	    String pop = "";
	    if (!popInfo.containsKey(id)) {
		System.err.println(id+" does not appear in popInfo");
		System.exit(-1);
	    }
	    pop = popInfo.get(id);
	    if (!popList.containsKey(pop)) {
		System.err.println("cannot find index for "+pop);
		System.exit(-1);
	    }
	    int outindex = popList.get(pop);
	    out[outindex].println(id+"\t"+pheno+"\t"+pc1+"\t"+pc2+"\t"+pc3+"\t"+pc4+"\t"+pc5+"\t"+pc6+"\t"+pc7+"\t"+pc8+"\t"+pc9+"\t"+pc10);
	    line = in.readLine();
	}
	in.close();
	for (int i = 0; i < numPop; i++) {
	    out[i].close();
	}

	in = new BufferedReader(new FileReader(rcolorfile));
	ArrayList<String> colorList = new ArrayList<String>();
	line = in.readLine();
	while (line != null) {
	    colorList.add(line);
	    line = in.readLine();
	}
	in.close();

	String symbolList = "";
	PrintWriter out2 = new PrintWriter(new BufferedWriter(new FileWriter(outputdir+"/plot.pca.R")));
	out2.print("popname=c(");
	boolean isfirst = true;
	for (int i = 0; i < popNameList.size(); i++) {
	    String pop = popNameList.get(i);
	    out2.print("\""+pop+"\"");
	    if (i == popNameList.size() - 1) {
		out2.println(")");
	    }
	    else {
		out2.print(",");
	    }
	    if (pop.compareTo("UCLA") == 0) continue;
	    int symbolID = popSymbolInfo.get(pop);
	    if (isfirst) {
		symbolList = (new Integer(symbolID)).toString();
		isfirst = false;
	    }
	    else {
		symbolList = symbolList+","+symbolID;
	    }
	}
	out2.print("popcolor=c(");
	int colorindex = 0;
	for (int i = 0; i < popNameList.size(); i++) {
	    String pop = popNameList.get(i);
	    if (pop.compareTo("CO") == 0) {
		out2.print("\"red\"");
	    }
	    else if (pop.compareTo("CR") == 0) {
		out2.print("\"blue\"");
	    }
	    else {
		out2.print("\""+colorList.get(colorindex)+"\"");
		colorindex++;
	    }
	    if (i == popNameList.size() - 1) {
		out2.println(")");
	    }
	    else {
		out2.print(",");
	    }
	}
	out2.println();
        for (int i = 0; i < popNameList.size(); i++) {
            String pop = popNameList.get(i);
	    out2.println(pop+" = read.table(paste(\""+outputdir+"/"+pop+".evec.txt\",sep=\"\"))");
        }
        out2.println();
        for (int i = 0; i < popNameList.size(); i++) {
            String pop = popNameList.get(i);
	    out2.println(pop+".num = dim("+pop+")[1]");
        }
        out2.println();
        for (int i = 0; i < popNameList.size(); i++) {
            String pop = popNameList.get(i);
	    out2.println(pop+".color = rep(popcolor["+(i+1)+"],"+pop+".num)");
        }
        out2.println();
	out2.println("ptcex = 1");
	out2.println("ptcex.main = 2");
	out2.println("ptcex.lab = 2");
	out2.println();
	out2.print("pch.merged = c(");
        for (int i = 0; i < popNameList.size(); i++) {
            String pop = popNameList.get(i);
	    out2.print(pop+"[,2]");
            if (i == popNameList.size() - 1) {
                out2.println(")");
	    }
            else {
                out2.print(",");
            }
        }
	out2.println();
	out2.println("for (i in 3:12) {");
	out2.print("\tpc.merged = c(");
        for (int i = 0; i < popNameList.size(); i++) {
            String pop = popNameList.get(i);
            out2.print(pop+"[,i]");
            if (i == popNameList.size() - 1) {
                out2.println(")");
            }
            else {
                out2.print(",");
            }
        }
        out2.print("\tcolor.merged = c(");
        for (int i = 0; i < popNameList.size(); i++) {
            String pop = popNameList.get(i);
            out2.print(pop+".color");
            if (i == popNameList.size() - 1) {
                out2.println(")");
            }
            else {
                out2.print(",");
            }
        }
	out2.println("\tpdf(paste(\""+outputdir+"\",\"/pcaplot.pc\",(i-2),\".pdf\",sep=\"\"),width=10,height=10)");
	out2.println("\tylabstr = paste(\"PC \",(i-2))");
	out2.println("\tmainstr = paste(\"PC \",(i-2))");
	out2.println("\tplot(pc.merged,col=color.merged,pch=pch.merged,ylab=ylabstr,cex=ptcex,cex.main=ptcex.main,cex.lab=ptcex.lab,mgp=c(2.5,1,0))");
	out2.println("\tyrange = max(pc.merged)-min(pc.merged)");
	out2.println("\typos = 0.02");
	out2.println("\typos = min(pc.merged)+yrange*0.5");
	//out2.println("\tlegend(length(pc.merged)-200,max(pc.merged),c(popname),cex=1,col=c(popcolor),pch=c(3,4, rep(20,"+(popNameList.size()-numNon1KGPop)+")))");
	out2.println("\tlegend(\"top\",c(popname),cex=1,col=c(popcolor),pch=c(3,"+symbolList+"),ncol=7)");
	//out2.println("\tlegend(\"top\",c(popname),cex=1,col=c(popcolor),pch=c(3,rep(20,"+(popNameList.size()-numNon1KGPop)+")),ncol=7)");
	out2.println("\tdev.off()");
	out2.println("}");
	out2.println();
	out2.println("for (i in 3:6) {");
        out2.print("\txmin = min(c(");
        for (int i = 0; i < popNameList.size(); i++) {
            String pop = popNameList.get(i);
            out2.print(pop+"[,i]");
            if (i == popNameList.size() - 1) {
                out2.println("))");
            }
            else {
                out2.print(",");
            }
        }
        out2.print("\txmax = max(c(");
        for (int i = 0; i < popNameList.size(); i++) {
            String pop = popNameList.get(i);
            out2.print(pop+"[,i]");
            if (i == popNameList.size() - 1) {
                out2.println("))");
            }
            else {
                out2.print(",");
            }
        }
	out2.println("for (j in (i+1):12) {");
        out2.print("\t\tymin = min(c(");
        for (int i = 0; i < popNameList.size(); i++) {
            String pop = popNameList.get(i);
            out2.print(pop+"[,j]");
            if (i == popNameList.size() - 1) {
                out2.println("))");
            }
            else {
                out2.print(",");
            }
        }
        out2.print("\t\tymax = max(c(");
        for (int i = 0; i < popNameList.size(); i++) {
            String pop = popNameList.get(i);
            out2.print(pop+"[,j]");
            if (i == popNameList.size() - 1) {
                out2.println("))");
            }
            else {
                out2.print(",");
            }
        }
        out2.println("\t\tpdf(paste(\""+outputdir+"\",\"/pcaplot.pc\",(i-2),\".pc\",(j-2),\".pdf\",sep=\"\"),width=10,height=10)");
	out2.println("\t\tmainstr = paste(\"PC \",(i-2),\" vs PC \",(j-2))");
	out2.println("\t\tylabstr = paste(\"PC \",(j-2))");
	out2.println("\t\txlabstr = paste(\"PC \",(i-2))");
	out2.println("\t\tplot("+popNameList.get(numNon1KGPop)+"[,i],"+popNameList.get(numNon1KGPop)+"[,j],xlim=c(xmin,xmax),ylim=c(ymin,ymax),col=popcolor["+(numNon1KGPop+1)+"],pch="+popNameList.get(numNon1KGPop)+"[,2],xlab=xlabstr,ylab=ylabstr,cex=ptcex,cex.main=ptcex.main,cex.lab=ptcex.lab,mgp=c(2.5,1,0))");
	for (int i = numNon1KGPop+1; i < popNameList.size(); i++) {
	    out2.println("\t\tpoints("+popNameList.get(i)+"[,i],"+popNameList.get(i)+"[,j],col=popcolor["+(i+1)+"],pch="+popNameList.get(i)+"[,2],cex=ptcex)");
	}
	for (int i = 0; i < numNon1KGPop; i++) {
	    out2.println("\t\tpoints("+popNameList.get(i)+"[,i],"+popNameList.get(i)+"[,j],col=popcolor["+(i+1)+"],pch="+popNameList.get(i)+"[,2],cex=ptcex)");
	}
        out2.println("\t\txrange = xmax - xmin");
	out2.println("\t\txpos = xmax - xrange*0.05");
	out2.println("\t\tyrange = ymax-ymin");
	out2.println("\t\typos = ymin+yrange*0.25");
	out2.println("\t\typos = ymax");
	out2.println("\t\typos = 0.02");
	out2.println("\t\tlegend(\"top\",c(popname),cex=1,col=c(popcolor),pch=c(3,"+symbolList+"),ncol=7)");
	//out2.println("\t\tlegend(xpos,ymax,c(popname),cex=1,col=c(popcolor),pch=c(3,4, rep(20,"+(popNameList.size()-numNon1KGPop)+")))");
	out2.println("\t\tdev.off()");
	out2.println("\t}");
	out2.println("}");
	out2.close();
    }

    public static void main(String[] args) throws Exception {
	if (args.length != 5) {
	    System.err.println("[usage] java ProcessPCAOutput1KGBPAMR [fam file] [pop info file] [eigenstrat evec file] [R color file] [outputdir]");
	    System.exit(-1);
	}
	ProcessPCAOutput1KGBPAMR cc = new ProcessPCAOutput1KGBPAMR();
	cc.createFiles(args[0], args[1], args[2], args[3], args[4]);
    }
}

