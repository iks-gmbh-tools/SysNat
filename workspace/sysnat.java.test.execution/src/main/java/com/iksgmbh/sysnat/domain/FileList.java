package com.iksgmbh.sysnat.domain;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileList 
{
	private List<File> list;
	private String fileListName;
	
	public FileList() {
		list = new ArrayList<>();
	}
	
	public void add(File file)  {
		list.add(file);
	}
	
	public int size()  {
		return list.size();
	}

	public String getName()  {
		return fileListName;
	}

	public List<File> getFiles()  {
		return list;
	}
	
	public void setName(String aFileListName) {
		this.fileListName = aFileListName;
	}

}
