import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class FilesFinder
{
	private static String SEARCH_DIR = "L:/AIA/Standardschreiben";
	private static String SEARCH_EXTENSIONS = "dot";
	
	public static void main(String[] args) throws IOException
	{
		if (args.length > 0) {
			SEARCH_DIR = args[0];
		}
		
		if (args.length > 1) {
			SEARCH_EXTENSIONS = args[1];
		}
		
		System.out.println("FilesFinder sucht nach " + SEARCH_EXTENSIONS + "-Dateien in " + SEARCH_DIR + "...");

		List<File> result = searchFilesRecursively(new File(SEARCH_DIR), SEARCH_EXTENSIONS);
		
		StringBuffer sb = new StringBuffer("Treffernummer;Datei" + System.getProperty("line.separator"));
		AtomicInteger counter = new AtomicInteger(1);

		result.forEach(f -> {
			try { sb.append(counter.getAndIncrement()).append(";").append(f.getCanonicalFile()).append(System.getProperty("line.separator"));
			} catch (IOException e) { e.printStackTrace(); }
		});

		File output = new File("FileSearchResult.csv");
		writeStringToFile(output, sb.toString());
		System.out.println(result.size() + " Dateien gefunden und die Trefferliste in die Datei " + output.getName() + " geschrieben!");
		System.out.println("Fertig.");
	}
	
    public static void writeStringToFile(File file, String fileData) throws IOException {
        try (
                OutputStream fos = new FileOutputStream(file);
                Writer osw = new OutputStreamWriter(fos, "UTF-8");
                BufferedWriter writer = new BufferedWriter(osw);
        ) {
            writer.write(fileData);
        }
    }

	
	
	private static final FilenameFilter DIRECTORY_FILTER = new FilenameFilter() {
		@Override public boolean accept(File dir, String name) {
			return new File(dir, name).isDirectory();
		}
	};

	public static List<File> findFiles(final File mainFolder, 
			                           final List<String> subdirsToSearch,
                                       final List<String> subdirsToIgnore,
			                           final String fileExtensionToSearch,
			                           final String fileExtensionToIgnore,
			                           final String partOfFileName) 
	{
		if (mainFolder == null || mainFolder.isFile() || ! mainFolder.exists())
		{
			throw new RuntimeException("Mainfolder is not valid!");
		}
		return searchFilesRecursively(mainFolder, new CustomizedFileFilter(subdirsToSearch, 
				                                                           subdirsToIgnore, 
				                                                           fileExtensionToSearch, 
				                                                           fileExtensionToIgnore,
				                                                           partOfFileName));
	}	
	

	/**
	 * Returns list of x-files found in the target folder and its subfolders (and
	 * their subfolders...) where x is any file extension.
	 * 
	 * @param folder
	 * @param fileExtensions
	 * @return file list
	 */
	public static List<File> searchFilesRecursively(final String folder, 
											        final String... fileExtensions) 
	{
		return searchFilesRecursively(new File(folder), fileExtensions);
	}

	public static List<File> searchFilesRecursively(final File folder, final String... fileExtensions) 
	{
		List<String> extensions = new ArrayList<>();
		for (String extension : fileExtensions) 
		{
			if (extension.startsWith(".")) {
				extensions.add(extension);
			} else {
				extensions.add("." + extension);
			}
		}

		return searchFilesRecursively(folder, new FilenameFilter() 
		{
			@Override
			public boolean accept(File dir, String name) {
				if (new File(dir, name).isDirectory())
					return false;
				if (extensions.stream().filter(extension -> extension.equals(".*")).findFirst().isPresent()) {
					return true;
				}
				if (extensions.stream().filter(extension -> name.endsWith(extension)).findFirst().isPresent()) {
					return true;
				}
				return false;
			}
		});
	}
	
	public static List<File> searchFilesRecursively(final File folder, 
			                             		     final FilenameFilter fileFilter)
	{
		final List<File> toReturn = new ArrayList<File>();
		
		if (! folder.exists()) {
			System.err.println("Folder does not exist: " + folder.getAbsolutePath());
			return toReturn;
		}
		
		final List<File> foundChildren = new ArrayList<File>();
		foundChildren.addAll(Arrays.asList(folder.listFiles(fileFilter)));
		
		List<File> subdirs = Arrays.asList( folder.listFiles(DIRECTORY_FILTER) );
		addSubdirsIfNotAlreadyContained(foundChildren, subdirs);
		
		
		for (File child : foundChildren) 
		{
			if ( child.isDirectory() )
			{
				toReturn.addAll( searchFilesRecursively(child, fileFilter) );					
			}
			else
			{
				toReturn.add(child);
			}
		}
		return toReturn;
	}
	
	
	private static void addSubdirsIfNotAlreadyContained(List<File> foundChildren, List<File> subdirs)
	{
		subdirs.stream().filter(subdir -> ! foundChildren.contains(subdir))
		                .forEach(subdir -> foundChildren.add(subdir));
	}


	public static class CustomizedFileFilter implements FilenameFilter
	{
		private List<String> subdirsToSearch;
		private List<String> subdirsToIgnore;
		private String fileExtensionToSearch;
		private String fileExtensionToIgnore;
		private String partOfFileName;
		
		public CustomizedFileFilter(final List<String> subdirsToSearch,
									final List<String> subdirsToIgnore, 
									final String aFileExtensionToSearch, 
									final String aFileExtensionToIgnore, 
									final String aPartOfFileName) 
		{
			this.subdirsToSearch = unifyPaths(subdirsToSearch);
			this.subdirsToIgnore = unifyPaths(subdirsToIgnore);
			this.partOfFileName = aPartOfFileName;
			
			this.fileExtensionToSearch = aFileExtensionToSearch;
			if (fileExtensionToSearch != null && ! fileExtensionToSearch.startsWith("."))
			{
				this.fileExtensionToSearch = "." + fileExtensionToSearch;
			}

			this.fileExtensionToIgnore = aFileExtensionToIgnore;
			if (fileExtensionToIgnore != null && ! fileExtensionToIgnore.startsWith("."))
			{
				this.fileExtensionToIgnore = "." + fileExtensionToIgnore;
			}
		}
		
		@Override
		public boolean accept(File dir, String name) 
		{

			final File file = new File(dir, name);
			
			if (file.isDirectory())  {
				return true;
			}

			if (subdirsToSearch != null) {
				boolean isSubdirToSearch = false;
				for (String subdir : subdirsToSearch) 
				{
					if (getUnifiedPath(dir).contains(subdir)) 
						isSubdirToSearch = true;
				}
				
				if ( ! isSubdirToSearch ) {
					 return false;
				}
			}
			
			if (subdirsToIgnore != null) {
				boolean isSubdirToSearch = true;
				for (String subdir : subdirsToIgnore) 
				{
					if (getUnifiedPath(dir).contains(subdir)) 
						isSubdirToSearch = false;
				}
				
				if ( ! isSubdirToSearch ) {
					 return false;
				}
			}
	
			if (fileExtensionToSearch != null && ! name.endsWith(fileExtensionToSearch))  {				
				return false;
			}
			
			if (fileExtensionToIgnore != null && name.endsWith(fileExtensionToIgnore))  {				
				return false;
			}
			
			if (partOfFileName != null && ! name.contains(partOfFileName))  {				
				return false;
			}

			return true;
		}
		
		private String getUnifiedPath(File dir) {
			String path = dir.getAbsolutePath();
			return getUnifiedPath(path);
		}
		
		/**
		 * Transforms possibly occurring Windows specific path separators into uniquely valid path separators.   
		 */
		private String getUnifiedPath(final String path) {
			return path.replace("\\", "/");   //apache.common.StringUtils.replace(path, "\\", "/");
		}
        
		private List<String> unifyPaths(final List<String> pathList)
		{
			if (pathList == null) return null;
			
			final List<String> toReturn = new ArrayList<String>();
			
			for (String path : pathList)
			{
				toReturn.add( getUnifiedPath(path) );
			}
			
			return toReturn;
		}
	
	}
}