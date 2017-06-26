package it.uniroma3.extractor.util.io;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

public class Compressed {
    
    /**
     * Returns a BufferedReader for a compressed file.
     * 
     * @param path
     * @return
     */
    public static BufferedReader getBufferedReaderForCompressedFile(String path){
	BufferedReader br = null;
	try {
	    FileInputStream fin = new FileInputStream(path);
	    BufferedInputStream bis = new BufferedInputStream(fin);
	    CompressorInputStream input = new CompressorStreamFactory().createCompressorInputStream(bis);
	    br = new BufferedReader(new InputStreamReader(input));
	} catch (FileNotFoundException | CompressorException e) {
	    e.printStackTrace();
	}
	return br;
    }
}
