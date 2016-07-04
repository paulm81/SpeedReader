package com.paulmunly.speedreader;

import java.io.*;
import java.nio.file.*;
import java.util.Enumeration;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * TODO: Probably need to structure this in a way that allows us to view images, non-standard 'text', etc.
 * @author Paul Munly <paul@paulmunly.com>
 *
 */
public class SpeedReaderResource implements Runnable {
    protected File SRFILE;                  //The FIle handler
    protected Path FILEPATH;                //The Path associated with SRFILE
    protected String FILETYPE;              //The FileType associated with SRFILE
    protected BufferedReader BUFREADER;     //What we'll use (?) to read the file
    protected int WPM;                      //The number of words to display in one minute
    protected final String NOTREADY = "BUF_NOT_READY";
    private int WORDINDEX = 1;
    private int LINEINDEX = 1;              //Setting to 1 because we'll be starting on line 1, first increment should be to 2.
    protected boolean NEWLINEFLAG = false;  //Used to flag whether we are looking at a consecutive \r\n or \n\r type combination of characters
    private Vector<Bookmark> BOOKMARKS;
    protected static final String BOF = "[Beginning of Document]";
    protected static final String EOF = "[End of Document]";
    private Word PREVWORD;
    private Word CURRWORD;
    private Word FIRSTWORD;
    private boolean FIRSTWORD_FLAG = true;
    
    
    /**
     * Create a new SpeedReaderResource from the Path fName
     *
     * @param fName The java.nio.file.Path associated with the File we're opening
     */
    public SpeedReaderResource(Path fName) {
        try {
            this.FILEPATH = fName;
            this.SRFILE = this.FILEPATH.toFile();
            this.FILETYPE = Files.probeContentType(this.FILEPATH);
            this.BOOKMARKS = new Vector<Bookmark>(0,1);
        } catch (IOException e) {
            //TODO - handle the exception properly.
            e.printStackTrace();
        }
    }
    
    protected void closeResource() {
        //This is a stub
    }
    
    @Override
    public void run() {
        //This read through the file; putting this into a thread so we read the file and don't stop other actions from occurring.
        try {
            if(this.isReady()) {
            	if(SpeedReader.inDebug())
            		System.out.println("DEBUG: Beginning to ingest file." + System.currentTimeMillis());
                char c;
                String str = new String("");
                Word word = new Word();
                while(this.isReady()) {
                    c = (char)this.BUFREADER.read();
                    if(Character.isWhitespace(c)) {
                        if((c == '\r' || c == '\n') && str.length() > 1) {
                            this.incrementLineIndex();
                        } else if((c == '\r' || c == '\n')
                                      && !this.NEWLINEFLAG) {
                            this.incrementLineIndex();
                            this.NEWLINEFLAG = true;
                            str = new String();
                            continue;
                        } else if(c == '\r' || c == '\n') {     //This should handle the case where we've got 2+ newline characters in a row
                            this.incrementLineIndex();
                            str = new String();
                            continue;
                        }
                        word = new Word(str.trim(), this.getLineIndex(), this.getWordIndex());
                        this.incrementWordIndex();
                        if(!this.FIRSTWORD_FLAG) {
                            word.setPreviousWord(this.PREVWORD);
                        } else {
                            this.FIRSTWORD_FLAG = false;
                            this.FIRSTWORD = word;
                            this.FIRSTWORD.setPreviousWord(new Word(BOF, -1, -1));
                            this.CURRWORD = this.FIRSTWORD;
                        }
                        this.PREVWORD = word;
                        str = new String();
                    }
                    this.NEWLINEFLAG = false;
                    str += new String() + c;
                }
                //We've reached the end of the file.
                Word w = new Word(EOF, -1, -1);
                w.setPreviousWord(word);
                if(SpeedReader.inDebug())
                    System.out.println("DEBUG: Finished Reading File "+this.FILEPATH+" Time: "+System.currentTimeMillis());
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * Returns the file size in bytes, kilobytes, or megabytes.  If the file is 1GB or greater, we assume it's too large to load
     * (Need to test 1GB and larger)
     * @param p The java.nio.file.Path pointing to the file associated with the SpeedReaderResource
     * @return A String indicating the file size in bytes, kilobytes, megabytes.
     */
    private String getPrettySize(Path p) {
        try {
            long bytes = Files.size(p);
            if(bytes < 1024)
                return bytes + " Bytes";
            else if(bytes < (1024*1024))
                return bytes/1024 + " KB";
            else if (bytes < (1024*1024*1024)) {
                bytes = bytes/1024;
                String b = ""+bytes;
                String beginning = b.substring(0, b.length() - 3);
                String end = b.substring(b.length() - 3);
                b = beginning.concat("," + end + " MB");
                return b;
            } else
                return "Too Big to Handle!";
        } catch (IOException e) {
            //TODO - handle the exception properly.
            e.printStackTrace();
        }
        return null;
    }
    
    public void setWPM(int wpm) {
        this.WPM = wpm;
    }
    
    /**
     * Returns the delay in milliseconds necessary to achieve wpm Words Per Minute
     * @param wpm The desired Words Per Minute text should be displayed.
     * @return The delay in milliseconds necessary to achieve wpm Words Per Minute.
     */
    protected int getDelay(int wpm) {
        return 60000/wpm;
    }
    
    /**
     * Checks to see if the SpeedReaderResource is ready for reading
     * @return True or False indicating a ready or not ready state
     * @throws FileNotFoundException
     * @throws IOException
     */
    protected boolean isReady() throws FileNotFoundException, IOException {
        return this.BUFREADER.ready();
    }
    
    /**
     * Advances the SpeedReaderResource and returns the next available line of text associated with the SpeedReaderResource
     * @return The next line of text in the SpeedReaderResource or NOTREADY
     * @throws FileNotFoundException
     * @throws IOException
     */
    protected String getNextLine() throws FileNotFoundException, IOException {
        if(this.isReady())
            return this.BUFREADER.readLine();
        else
            return this.NOTREADY;
    }
    
    private void incrementWordIndex() {
        this.WORDINDEX += 1;
    }
    
    private void incrementLineIndex() {
        this.LINEINDEX += 1;
        //Reset our WORDINDEX for the new line
    }
    
    /**
     * Returns the int-value position within the current line of words we're currently reading
     * @return the int-value position within the current line of words we're currently reading
     */
    protected int getWordIndex() {
        return this.WORDINDEX;
    }
    
    /**
     * Returns the int-value line number of our current word within the file we're reading
     * @return the int-value line number of our current word within the file we're reading
     */
    protected int getLineIndex() {
        return this.LINEINDEX;
    }
    
    /**
     * Returns the Long size of the file associated with the SpeedReaderResource
     * Note that this works for text files only -- each subclass will probably need to @override
     * this method and implement something suitable for the file type.
     * @return the Long size of the file associated with teh SpeedReaderResource, or -1L if an IOException is thrown
     */
    protected long getResourceLength() {
        try {
            return Files.size(this.FILEPATH);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return -1L;
    }
    
    protected Word getCurrentWord() {
        return this.CURRWORD;
    }
    
    /**
     * Returns the previous Word in the SpeedReaderResource or a new Word with
     * values BOF, line -1, word -1,indicating beginning of file
     * @param currWord The current Word in the queue
     * @return currWord.getPreviousWord() or a new Word indicating BOF
     */
    protected Word getPreviousWord() {
        this.CURRWORD = this.CURRWORD.getPreviousWord();
        if(this.CURRWORD == null || this.CURRWORD.textMatches(BOF)) {
            this.CURRWORD = this.FIRSTWORD;
        }
        return this.CURRWORD;
    }
    
    /**
     * Splits the input into words, delineated by whitespace and returns the next word in our reader.
     *
     * @return The next word in the SpeedReaderResource's input.
     */
    protected Word getNextWord() {
        if(this.CURRWORD == this.FIRSTWORD) {
            this.CURRWORD = this.CURRWORD.getNextWord();
            return this.FIRSTWORD;
        } else if(this.CURRWORD.getNextWord().textMatches(EOF)) {
            return this.CURRWORD;
        } else {
            this.CURRWORD = this.CURRWORD.getNextWord();
            return this.CURRWORD;
        }
    }
    
    /**
     * Sets the current Word to word
     * @param word the Word
     */
    protected void setCurrentWord(Word word) {
        this.CURRWORD = word;
    }
    

    protected void setCurrentWord(Bookmark bm) {
        if(!this.BOOKMARKS.isEmpty()) {
            for(int i = 0; i < this.BOOKMARKS.size(); i++) {
                Bookmark look = this.BOOKMARKS.get(i);
                if(look.matches(bm))
                    this.CURRWORD = bm.WORD;
            }
        }
    }
    
    /**
     * Creates a new Bookmark using the current word, line, and position in the stream
     * @return True or False - true if a new Bookmark was set, false if the attempted bookmark was a duplicate.
     */
    protected boolean setBookmark() {
        if(this.CURRWORD.textMatches(BOF))
            return false;
        
        Bookmark bm;
        bm = new Bookmark(this.CURRWORD);
        if(!this.BOOKMARKS.isEmpty()) {
            Enumeration<Bookmark> e = getBookmarks();
            while(e.hasMoreElements()) {
                Bookmark oldbm = e.nextElement();
                if(bm.getLine() == oldbm.getLine() && bm.getPosition() == oldbm.getPosition()) {
                    //Already have this bookmarked, skip it.
                    if(SpeedReader.inDebug())
                        System.out.println("DEBUG: Duplicate Bookmark attempted, skipping.");
                    
                    return false;
                }
            }
        }
        this.BOOKMARKS.addElement(bm);
        return true;
    }
    
    public Enumeration<Bookmark> getBookmarks() {
        return this.BOOKMARKS.elements();
    }
    
    public Bookmark getLastBookmark() throws NullPointerException {
        if(!this.BOOKMARKS.isEmpty())
            return this.BOOKMARKS.elementAt(this.BOOKMARKS.capacity() - 1);
        else
            return null;
    }
    public String toString() {
        String info = "\nSpeedReaderResource: ";
        info += this.getClass().getCanonicalName();
        info += "\nResource Type: " + this.FILETYPE;
        info += "\nResource Path: " + this.FILEPATH.toAbsolutePath();
        info += "\nResource Size: " + getPrettySize(this.FILEPATH);
        info += "\n";
        return info;
    }
    
    /**
     * A Class to encapsulate a Word as a Bookmark
     * @author Paul Munly <paul@paulmunly.com>
     */
    protected class Bookmark {
        
        private Word WORD;
        
        /**
         * Creates a new Bookmark at word
         * @param word the Word to bookmark
         */
        public Bookmark(Word word) {
            this.WORD = word;
            if(SpeedReader.inDebug())
                System.out.println("DEBUG: Created a new bookmark at: "+toString());
        }
        
        protected boolean matches(Bookmark bm) {
            if(bm.getWord().matches(this.WORD))
                return true;
            else
                return false;
        }
        
        protected int getLine() {
            return this.WORD.getLine();
        }
        
        public int getPosition() {
            return this.WORD.getPosition();
        }
        
        public Word getWord() {
            return this.WORD;
        }
        
        public String toString() {
            return new String("" + getWord());
        }
    }
    
    /**
     * A class to represent a word within the SpeedReaderResource. This will ease the
     * processing of bookmarking and moving forward/backward through the file.
     * @author Paul Munly <paul@paulmunly.com>
     */
    protected class Word {
        
        private int LINE;
        private int POSITION;
        private String TEXT;
        private boolean PAUSEPHRASE;
        private final Pattern PUNCTPAUSE = Pattern.compile("[a-zA-Z0-9]*[\\.,:;?!]");   //TODO: Update Patter to match things like !" and .)
        private Word NEXTWORD;
        private Word PREVWORD;
        
        /**
         * Creates a new Word with default empty values.
         */
        protected Word() {
            setLine(0);
            setPosition(0);
            setText("");
            setPausePhrase(false);
        }
        
        protected Word(String text, int line, int position){
            setLine(line);
            setPosition(position);
            setText(text);
            //Here we set the flag which indicates this word requires a longer than normal pause due to some form of punctuation
            Matcher m = this.PUNCTPAUSE.matcher(text);
            setPausePhrase(m.matches());
        }
        
        protected int getLine() {
            return this.LINE;
        }
        
        protected void setPreviousWord(Word prev) {
            this.PREVWORD = prev;
            prev.setNextWord(this);
        }
        
        protected Word getPreviousWord() throws NullPointerException {
            return this.PREVWORD;
        }
        
        private void setNextWord(Word next) {
            this.NEXTWORD = next;
        }
        
        protected Word getNextWord() throws NullPointerException {
            return this.NEXTWORD;
        }
        
        private void setLine(int line) {
            this.LINE = line;
        }
        
        protected int getPosition() {
            return this.POSITION;
        }
        
        private void setPosition(int position) {
            this.POSITION = position;
        }
        
        protected String getText() {
            return this.TEXT;
        }
        
        private void setText(String text) {
            this.TEXT = text;
        }
        
        protected boolean isPausePhrase() {
            return this.PAUSEPHRASE;
        }
        
        private void setPausePhrase(boolean pausephrase) {
            this.PAUSEPHRASE = pausephrase;
        }
        
        /**
         * Checks to see if Word look is a Text/Line/Position duplicate of this Word
         * @param look the Word to check
         * @return true if a duplicate match, false if not
         */
        protected boolean matches(Word look) {
            if(look.getText().compareTo(getText()) == 0 &&
                    look.getLine() == getLine() &&
                    look.getPosition() == getPosition())
                return true;
            else
                return false;
        }
        
        protected boolean textMatches(String text) {
            if(text.compareTo(getText()) == 0)
                return true;
            else
                return false;
        }
        
        protected int length() {
            return this.TEXT.length();
        }
        
        public String toString() {
            return new String("Line: " + getLine() + ", Position: " + getPosition()
                                       + ", Word: " + getText()
                                       + ", Previous: " + getPreviousWord().getText());
        }
    }
}