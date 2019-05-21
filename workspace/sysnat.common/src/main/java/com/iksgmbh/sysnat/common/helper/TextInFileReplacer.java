
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
import java.util.ArrayList;
import java.util.List;
 
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
 
public class TextInFileReplacer 
{
    private static int totalReplacements = 0;
    private static int totalFileWithReplacements = 0;
    private static boolean toSearchStringFound = false;

    public static void main(String[] args) 
    {
          if (args.length != 3 ) {
                 System.out.println("Problem: Genau drei Argumente werden erwartet, es sind aber nur " + args.length + " vorhanden.");
                 return;
          }

          final String targetFolder = args[0];
          final String toSearch = args[1];
          final String replacement = args[2];

          doYourJob(targetFolder, toSearch, replacement);
    }

    public static String doYourJob(final String targetFolder, 
                                        final String toSearch, 
                                        final String replacement) 
    {
          totalReplacements = 0;
          totalFileWithReplacements = 0;
          
          List<File> files = FileFinder.searchFilesRecursively(targetFolder, ".*");
          
          if (files.size() == 0) {
                 return "Es wurde in '" + targetFolder + "' keine Dateien gefunden.";
          }
          
          files.forEach(file -> searchAndReplace(file, toSearch, replacement));
          
          String result = "Es wurden in " + totalFileWithReplacements + " von insgesamt " + files.size() 
                          + " gefundenen Dateien insgesamt " + totalReplacements 
                          + " Ersetzungen des Textes '" + toSearch + "' durchgeführt.";
          
           if (totalReplacements == 0) {
                 result = "Es wurden in keine der " + files.size() 
                   + " gefundenen Dateien der Text '" + toSearch + "' gefunden und daher keine Ersetzungen durchgeführt.";
          }
          
          System.out.println(result);
          return result;
    }

    private static void searchAndReplace(final File textFile, 
                                              final String toSearch, 
                                              final String replacement) 
    {
          List<String> oldContent = SysNatFileUtil.readTextFile(textFile);
          List<String> newContent = new ArrayList<>();
          toSearchStringFound = false;
          
          for (String line : oldContent) 
          {
                 if (line.contains(toSearch)) {
                       toSearchStringFound = true;
                       totalReplacements++;
                       newContent.add(line.replaceAll(toSearch, replacement));
                 } else {
                       newContent.add(line);
                 }
          }
          
          if (toSearchStringFound) {
                 SysNatFileUtil.writeFile(textFile, newContent);
                 totalFileWithReplacements++;
          }
          
    }
}
 
