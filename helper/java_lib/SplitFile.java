import java.io.*;
import java.util.*;
import java.util.zip.*;

public class SplitFile {

    public void printErrorMessage(String error) throws Exception {
        System.err.println("ERROR_in_SplitFile : "+error);
	System.exit(-1);
    }

    public void createFiles(String file, String numLineOut, String outputdir, String mfix) throws Exception {
    }

    public static void main(String[] args) throws Exception {
	if (args.length != 4) {
	    System.err.println("[usage] java SplitFile [file] [# of lines per output] [output dir] [middlefix]");
	    System.exit(-1);
	}
	SplitFile cc = new SplitFile();
	cc.createFiles(args[0], args[1], args[2], args[3]);
    }
}

