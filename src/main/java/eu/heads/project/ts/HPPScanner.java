/*******************************************************************************
 * Copyright (c) 2014, CriativaSoft, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 *******************************************************************************/
package eu.heads.project.ts;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

// TODO: DOCS
// NOTA: Informar que ele utiliza :  LibraryIndex.getExecutor() eque usa thread's 
public abstract class HPPScanner {
    
    private final static Logger LOG = Logger.getLogger(HPPScanner.class.getName()); 
    
	protected abstract File[] getFilesToParse(File folder);
	
    /**
     * Configure parser for this file.
     * @param parser
     */
    protected abstract void configParser(HPPSourceParser parser, File currentFile);
    
    
    
    
    	

    boolean firstPass = true;
    /**
     * 
     * @param folder
     * @return
     */
    public void scan(File folder, StringBuilder res){
        
        if(!folder.exists() || ! folder.isDirectory()) throw new IllegalArgumentException("path is not a directory !");
        
        
        File[] files = getFilesToParse(folder);
        
        List<HPPSourceParser> runningParser = new LinkedList<HPPSourceParser>();
        
        for (File file : files) {
            File sourcefile = file;
            
            LOG.fine("LibraryScanner :: scanFolder = " + folder + ", file: " + sourcefile.getName());
            
            if(sourcefile.exists()){
            	HPPSourceParser parser = new HPPSourceParser(res);
            	
                configParser(parser, sourcefile);
                
                new FileScanTask(parser, sourcefile).run();
                
                runningParser.add(parser);
                
            }     
        }
        
        
    }    
    
    
    private static final class FileScanTask implements Runnable{
    	
    	private HPPSourceParser parser;
		private File file;

		public FileScanTask(HPPSourceParser parser, File file) {
			super();
			this.parser = parser;
			this.file = file;
		}

		@Override
		public void run() {
			parser.parse(file);
		}
    	
    }

}
