import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.util.List;

import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;

public class LinesToaddStringInCodeTransformer
{
	public static void main(String[] args) throws Exception
	{
		String text = Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor).toString();
		List<String> lines = SysNatStringUtil.toList(text, "\n");
		lines.forEach(line -> System.out.println("    content.add(\"" + line.replace("\"", "\\\"") + "\");"));
	}
}
