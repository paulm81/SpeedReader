package com.paulmunly.speedreader;

import java.util.*;
import java.nio.file.*;
import java.io.IOException;

public class SpeedReader {
  
    private static final String     VERSION = "0.1";
    private static final boolean    DEBUG = true;
    private static final String     AUTHOR = "Created by Paul Munly <paul@paulmunly.com>";
    private static final String []  ARGUMENTS = {"-h \t\tShow this help message.",
                                                 "-f <file>\t Open file.  Include the full path to the file.",
                                                 "-wpm <Words Per Minute>\t Set the initial Words Per Minute speed."
                                                 };
    private static final String     HELP_ARG = "-h";
    private static final String     FILE_ARG = "-f";
    private static final String     WPM_ARG = "-wpm";
    private static final String     TXT = "text/plain";
    private static final String     PDF = "application/pdf";
    private static final String     DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private static final String     DOC = "application/msword";
    private static final String     RTF = DOC;
    private enum SPT_FILE_TYPES {
                        //PDF, TXT, DOC, DOCX, RTF
                        TXT;
                        static final Map<String, SPT_FILE_TYPES> fileMap =
                            new HashMap<String, SPT_FILE_TYPES>();
                        static  {
                            for (SPT_FILE_TYPES ft : SPT_FILE_TYPES.values())
                                fileMap.put(ft.toString(), ft);
                        }
    }
  
    public static void main(String[] args) {
        System.out.println("SpeedReader version " + getVersion() + "\n" + getAuthor());
        switch(args.length) {
            case 0:
                printHelp();
                new SpeedReaderGUI();
                break;
            case 1:
                printHelp();
                break;
            case 2:
                if(args[0].matches(HELP_ARG)) {
                    printHelp();
                } else if(args[0].matches(FILE_ARG)) {
                    openFile(args);
                } else if(args[0].matches(WPM_ARG)) {
                    printHelp();
                }
                break;
            case 3:
                break;
            case 4:
                break;
        }
    }
    
    private static void openFile(String[] args) {
        try {
            String fileName = new String();
            if(args.length > 2) {
                for(int i = 0; i < args.length; i++) {
                    fileName = fileName.concat(args[i] +
                                    (i == (args.length - 1) ? "" : " ")); //Add a space between each argument while we string fileName back together
                }
            } else {
                fileName = args[1];
            }
            String fType = Files.probeContentType(Paths.get(fileName));
            SpeedReaderResource srr = null;
            if(fType.compareTo(TXT) == 0) {
                srr = new SpeedReaderTextResource(Paths.get(fileName));
            } else if(fType.compareTo(PDF) == 0) {
                srr = new SpeedReaderPDFResource(Paths.get(fileName));
            } else if(fType.compareTo(DOCX) == 0) {
                //srr = new SpeedReaderDOCXResource(Paths.get(fileName));
            } else if(fType.compareTo(DOC) == 0 || fType.compareTo(RTF) == 0) {
                //srr = new SpeedReaderMSWordResource(Paths.get(fileName));
            } else {
                //Found something else --> inform the user
            }
            if(inDebug()) {
                System.out.println("DEBUG: args[0] = "+args[0]);
                System.out.println("DEBUG: Type = " + fType);
                System.out.println("DEBUG: SpeedReader Resource = " + srr.toString());
            }
            //Temporary for testing
            int WPM = 500;
            //End Temporary for testing

            //Start the GUI
            new SpeedReaderGUI(srr, WPM);
        } catch (IOException e) {
            // TODO - Exception Handler
            e.printStackTrace();
        }
    
    }
    
    public static String getVersion() {
        return VERSION;
    }
    
    public static boolean inDebug() {
        return DEBUG;
    }
    
    private static String getAuthor() {
        return AUTHOR;
    }
    
    private static void printHelp() {
        System.out.println("For faster access, feel free to use the following arguments: "); //TODO - determine the args we will accept :)
        for(int i = 0; i < ARGUMENTS.length; i++) {
            System.out.println("\t" + ARGUMENTS[i]);
        }
        System.out.println("Supported extensions include: ");
        for (SPT_FILE_TYPES ft : SPT_FILE_TYPES.values()) {
            System.out.print(ft.name() + " ");
        }
    }
}