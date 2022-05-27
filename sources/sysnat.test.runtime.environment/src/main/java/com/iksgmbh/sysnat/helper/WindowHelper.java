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
package com.iksgmbh.sysnat.helper;

import java.util.ArrayList;
import java.util.List;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;

public class WindowHelper 
{
   static interface User32 extends StdCallLibrary {
	      User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class);

      interface WNDENUMPROC extends StdCallCallback {
         boolean callback(Pointer hWnd, Pointer arg);
      }

      boolean EnumWindows(WNDENUMPROC lpEnumFunc, Pointer userData);
      int GetWindowTextA(Pointer hWnd, byte[] lpString, int nMaxCount);
      Pointer GetWindow(Pointer hWnd, int uCmd);
   }
   
   public static void main(String[] args)
   {
	   getAllWindowNames().stream().filter(e -> e.startsWith("Neuer Tab -")).forEach(System.out::println);
	   getAllWindowNames().stream().filter(e -> e.contains("hrome")).forEach(System.out::println);
	   System.out.println(getAllWindowNames().size());
//	   List<String> allWindowNames1 = getAllWindowNames();
//	   try {Thread.sleep(5000);	} catch (InterruptedException e1) {}
//	   List<String> allWindowNames2 = getAllWindowNames();
//	   allWindowNames1.stream().filter(e -> ! allWindowNames2.contains(e)).forEach(System.out::println);
	}

   private static List<String> getAllWindowNames() {
      final List<String> windowNames = new ArrayList<String>();
      final User32 user32 = User32.INSTANCE;
      user32 .EnumWindows(new User32.WNDENUMPROC() {

         @Override
         public boolean callback(Pointer hWnd, Pointer arg) {
            byte[] windowText = new byte[512];
            user32.GetWindowTextA(hWnd, windowText, 512);
            String wText = Native.toString(windowText).trim();
            if (!wText.isEmpty()) {
               windowNames.add(wText);
            }
            return true;
         }
      }, null);

      return windowNames;
   }
   
   public static List<String> getListOfAvailableWindows() {
      return getAllWindowNames();
   }

   public static int getNumberOfAvailableWindows() {
      return getAllWindowNames().size();
   }
}