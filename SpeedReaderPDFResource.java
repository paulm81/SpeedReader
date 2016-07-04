/*
 * SpeedReaderPDFResource.java
 * 
 * Created: Jun 26, 2016
 * Version: 
 *
 * TODO: 
 */
package com.paulmunly.speedreader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.pdfparser.PDFStreamParser;

public class SpeedReaderPDFResource extends SpeedReaderResource {

    private PDDocument DOCUMENT;
    
    public SpeedReaderPDFResource(Path fName) {
        super(fName);
        try {
            this.DOCUMENT = PDDocument.load(fName.toFile());
            /*
            for(PDPage page : this.DOCUMENT.getPages()) {
                PDFStreamParser parser = new PDFStreamParser(page);
                parser.parse();
                List<Object> tokens = parser.getTokens();
                tokens.forEach(item->System.out.println(item));
            }
            */
            
            PDFTextStripper pdftext = new PDFTextStripper();
            
            super.BUFREADER = new BufferedReader(new StringReader(pdftext.getText(this.DOCUMENT)));
            new Thread(this).start();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected void closeResource() {
        try {
            this.DOCUMENT.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
