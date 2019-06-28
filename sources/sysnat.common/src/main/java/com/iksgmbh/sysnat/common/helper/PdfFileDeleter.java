/*
 * Copyright 2018 IKS Gesellschaft fuer Informations- und Kommunikationssysteme mbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.iksgmbh.sysnat.common.helper;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;


/**
 * Utility, to delete all Pdf Contract files in the user's download directory
 * 
 * @author  Reik Oberrath
 */
public class PdfFileDeleter extends FileDeleter
{
	public static void main(String[] args) throws IOException 
	{
		System.out.println("");
		final File folder = SysNatFileUtil.getDownloadDir();
		int counter = 0;
		
		String partOfFileName = null;;
		List<File> toDelete = findFiles(folder, null, null, ".pdf", partOfFileName);
		//toDelete.addAll( findFiles(folder, null, null, ".zip", partOfFileName) );

		if (toDelete.isEmpty()) {
			System.out.println("Es wurden keine Vertrag-PDF-Dateien in " + folder.getCanonicalPath() + " gefunden." );
		} else {			
			for (File pdfFile : toDelete) {
				pdfFile.delete();
				counter++;
			}
			System.out.println("Es wurden " + counter + " Vertrag-PDF-Dateien aus " + folder.getCanonicalPath() + " entfernt." );
		}
		
	}
}