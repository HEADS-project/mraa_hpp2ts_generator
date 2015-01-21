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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import eu.heads.project.ts.utils.ExtFileFilter;

public class CPPHeaderScanner extends HPPScanner {

	
	public static void main(String[] args) throws FileNotFoundException {
		String folder = ".";
		String dest = "mraa.d.ts";
		if (args.length>0){
			folder = args[0];
		}
		if (args.length>1){
			dest = args[1];
		}
	
		StringBuilder b  =new StringBuilder();
		
		b.append("/**\n * Created by barais on 20/01/15.\n */ \n declare module \"mraa\" { \n \t module mraa {\n");
		new CPPHeaderScanner().scan(new File(folder), b);
		b.append("\t} \n }\n}\n");
		FileOutputStream fo = new FileOutputStream(new File(dest));
		PrintWriter po = new PrintWriter(fo);
		po.write(b.toString());
		po.flush();
		po.close();
		System.out.println("End to parse save in " + dest);
	}

	public CPPHeaderScanner() {
		super();
		
	}

	@Override
	protected File[] getFilesToParse(File folder) {
		// check /src directory
		File src = new File(folder, "src");
		if (src.exists()) {
			folder = src;
		}

		File[] fs1 =  folder.listFiles(new ExtFileFilter("hpp","types.h"));
		return (fs1);
	}

	@Override
	protected void configParser(HPPSourceParser parser, File currentFile) {
		// TODO Auto-generated method stub

	}


}
