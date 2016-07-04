package com.paulmunly.speedreader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class SpeedReaderTextResource extends SpeedReaderResource {
    
    public SpeedReaderTextResource(Path fName) {
        super(fName);
        try {
            super.BUFREADER = Files.newBufferedReader(super.FILEPATH);
            new Thread(this).start();
        } catch (FileNotFoundException e) {
            //TODO - handle the exception properly.
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}