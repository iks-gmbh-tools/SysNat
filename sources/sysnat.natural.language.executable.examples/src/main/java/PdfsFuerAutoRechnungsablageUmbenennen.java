import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.iksgmbh.sysnat.common.helper.FileFinder;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;
import com.iksgmbh.sysnat.helper.docval.filereader.SysNatPdfReader;

public class PdfsFuerAutoRechnungsablageUmbenennen
{
	private static final String SEP = "----------------------------------------------------";
	private static final File okResultFile = new File("./ok.txt");
	private static final File errResultFile = new File("./error.txt");
	private static final String identifierList = "INVOICE • DATE, СЧЕТ-ФАКТУРА • ДАТА, FACTURE • DATE, FATURA • DATA, FACTURA • FECHA, DATA • FATTURA, DATUM • RECHNUNG";
	
	private static File sourceDir;
	
	public static void main(String[] args) throws IOException
	{
		if (args.length != 4) {
			System.err.println("Fehler: zum Aufruf von PdfsFuerAutoRechnungsablageUmbenennen immer 4 Parameter verwenden!");
			return;
		}

		if (args[2].equalsIgnoreCase("step1")) 
		{
			System.out.println("PdfsFuerAutoRechnungsablageUmbenennen");
			System.out.println(SEP);
			System.out.println("Plausi-Prüfung: " + args[3]);
			
			okResultFile.delete();
			errResultFile.delete();
			
			boolean ok = analyse(args[0], args[3]);
			if (ok) checkTargetDir1(args[1]);
		} 
		else 
		{			
			doYourJob(args[0], args[1]);
		}
	}
	
	private static void checkTargetDir1(String targetDirPath) throws IOException
	{
		File targetDir = new File(targetDirPath);
		
		if (targetDir.getCanonicalPath().equals(sourceDir.getCanonicalPath())) {
			System.out.println("Quellverzeichnis und Zielverzeichnis sind gleich!");
			return;
		}
	}

	private static boolean checkTargetDir2(String targetDirPath) throws IOException
	{
		File targetDir = new File(targetDirPath);
		if (targetDir.exists()) {
			if (targetDir.list().length > 0 && ! targetDir.getCanonicalPath().equals(sourceDir.getCanonicalPath())) {
				System.err.println("Fehler: Zielverzeichnis existiert bereits und wird nicht überschrieben!");
				return false;
			}
		}
		return true;
	}

	private static boolean analyse(String sourceDirPath, String plausiRuleString) throws IOException
	{		
		sourceDir = new File(sourceDirPath);
		
		if (! sourceDir.exists()) {
			System.err.println("Fehler: Quellverzeichnis existiert nicht (" + sourceDir.getCanonicalPath() + ")!");
			return false;
		}
		System.out.println("Quellverzeichnis: " + sourceDir.getCanonicalPath());
		List<String> identifier = SysNatStringUtil.toList(identifierList, ",");
		List<String> plausiRules = SysNatStringUtil.toList(plausiRuleString, ",");
		
		HashMap<String, String> result = new HashMap<>();
		List<File> pdfFiles = FileFinder.searchFilesRecursively(sourceDir, "pdf", "PDF");
		System.out.println("Anzahl gefundener PDFs: " + pdfFiles.size());
		
		pdfFiles.forEach(f -> searchAktenzeichen(f, identifier, result, plausiRules));
		
		HashMap<String, String> okFiles = new HashMap<>();
		result.keySet().stream().filter(key -> ! result.get(key).startsWith("Fehler: ")).forEach(key -> okFiles.put(key, result.get(key)));
		createResultFile(okFiles, okResultFile); 
		System.out.println("Anzahl PDFs mit erkanntem Aktenzeichen: " + okFiles.size());
		
		HashMap<String, String> errFiles = new HashMap<>();
		result.keySet().stream().filter(key -> result.get(key).startsWith("Fehler: ")).forEach(key -> errFiles.put(key, result.get(key)));
		if (errFiles.size() > 0) {
			createResultFile(errFiles, errResultFile);
			System.out.println("Anzahl PDFs OHNE erkanntem Aktenzeichen: " + errFiles.size());
		}
				
		System.out.println(SEP);
		return true;
	}

	private static void createResultFile(HashMap<String, String> result, File targetFile)
	{
		StringBuffer sb = new StringBuffer();
		result.keySet().forEach(key -> sb.append(key).append(" # ").append(result.get(key)).append(System.getProperty("line.separator")));
		SysNatFileUtil.writeFile(targetFile, sb.toString());
	}

	private static void doYourJob(String sourceDirPath, String targetDirPath) throws IOException
	{
		sourceDir = new File(sourceDirPath);
		if (! checkTargetDir2(targetDirPath)) return;

		final File targetDir = new File(targetDirPath);
		boolean copyMode = ! targetDir.getCanonicalPath().equals(sourceDir.getCanonicalPath());

		List<String> filesToHandle  = SysNatFileUtil.readTextFile(okResultFile);
		for (String line : filesToHandle) {
			handle(line, targetDir, copyMode);
		}
		
		if (copyMode) {
			System.out.println("Zielverzeichnis: " + targetDir);
			System.out.println("Anzahl kopierter PDFs mit Aktenzeichen als Namen: " + filesToHandle.size());
		} else {
			System.out.println("Anzahl in Aktenzeichen umbenannter PDFs: " + filesToHandle.size());
		}
		System.out.println(SEP);
	}

	private static void handle(String line, File targetDir, boolean copyMode) throws IOException
	{
		String[] splitResult = line.split("#");
		File file = new File(splitResult[0].trim());
		if (copyMode) 
		{			
			String newFilePath = file.getCanonicalPath().replace(sourceDir.getCanonicalPath(),targetDir.getCanonicalPath())
					                                    .replace(file.getName(),splitResult[1].trim() + ".PDF");
			SysNatFileUtil.copyBinaryFile(file, newFilePath);
		} else {
			String newFilePath = file.getCanonicalPath().replace(file.getName(),splitResult[1].trim() + ".PDF");
			file.renameTo(new File(newFilePath));
		}
	}

	private static void searchAktenzeichen(File f, List<String> identifier, HashMap<String, String> result, List<String> plausiRules)
	{
		try {
			result.put(f.getCanonicalPath(), getAktenzeichenFromPdfContent(f.getCanonicalPath(), identifier, plausiRules));
		} catch (Exception e) {
			result.put(f.getAbsolutePath(), "Fehler: " + e.getMessage());
		}
	}

	private static String getAktenzeichenFromPdfContent(String filePath, List<String> identifier, List<String> plausiRules)
	{
		String firstPageContent;
		try {
			firstPageContent = SysNatPdfReader.doYourJob(filePath).get(0).getPageContentAsString();
		} catch (Exception e) {
			return "Fehler: Datei nicht lesbar";
		}
		
		List<String> lines = SysNatStringUtil.toList(firstPageContent, System.getProperty("line.separator"));
		String lastLine = null;
		boolean match = false;
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			match = identifier.stream().filter(e -> line.startsWith(e.trim())).findFirst().isPresent(); 
			if (match) break;
			lastLine = line;
		}

		if (! match) {
			return "Fehler: Kein Aktenzeichen findbar";
		}
		
		if (lastLine.contains("-")) 
		{
			String[] splitResult = lastLine.split("-");
			int size = splitResult.length;
			lastLine = splitResult[size-1].trim();
		}
		
		return performPlausiChecks(lastLine.trim(), plausiRules);
	}

	private static String performPlausiChecks(String aktenzeichenCandidate, List<String> plausiRules)
	{
		boolean ok = plausiRules.stream().filter(r -> doesMatch(r.trim(), aktenzeichenCandidate)).findFirst().isPresent();
		if (! ok) {
			return "Fehler: gefundenes Aktenzeichen (" + aktenzeichenCandidate + ") entspricht keiner PlausiPrüfung!"; 
		}
		return aktenzeichenCandidate;
	}

	private static boolean doesMatch(String rule, String aktenzeichenCandidate)
	{
		char firstRuleDigit = rule.toCharArray()[0]; 
		char firstAzDigit = aktenzeichenCandidate.toCharArray()[0];
		String az = aktenzeichenCandidate;
		String ru = rule;
		
		if (Character.isDigit(firstRuleDigit)) 
		{
			if (! Character.isDigit(firstAzDigit)) {
				return false;
			}
			// no char compare here
		} 
		else 
		{
			if (firstAzDigit != firstRuleDigit) {
				return false;
			}
			az = az.substring(1);
			ru = ru.substring(1);
		}
		
		return isNumber(az) && isLengthOk(az, toLengthRange(ru));
	}

	private static boolean isNumber(String aktenzeichenCandidate)
	{
		List<Character> characterList = aktenzeichenCandidate.chars().mapToObj(i -> (char) i).collect(Collectors.toList());
		return ! characterList.stream().map(c -> (char)c).filter(c -> ! Character.isDigit(c)).findFirst().isPresent();
	}

	private static LengthRange toLengthRange(String rule)
	{
		String[] splitResult = rule.split("-");
		LengthRange toReturn = new LengthRange();
		toReturn.min = Integer.valueOf(splitResult[0]);
		toReturn.max = Integer.valueOf(splitResult[1]);
		
		return toReturn;
	}

	private static boolean isLengthOk(String aktenzeichenCandidate, LengthRange lengthRange)
	{
		return aktenzeichenCandidate.length() <= lengthRange.max && aktenzeichenCandidate.length() >= lengthRange.min;
	}
	
	static class LengthRange {
		int min;
		int max;
	}
	

}
