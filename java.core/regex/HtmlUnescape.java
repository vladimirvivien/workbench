import java.util.regex.*;
import java.util.*;

public class HtmlUnescape {
	public static void main(String[] args){
		HashMap<String,String> codes = new HashMap<String,String>();
		codes.put("&lt;", "<");
		codes.put("&gt;", ">");
		codes.put("&#34;", "\"");
		// add codes as needed, see http://www.w3.org/TR/html4/sgml/entities.html for full list.
		
		String html = "&lt;html&gt;&lt;head&gt;&lt;title&gt;Hello&lt;/title&gt;&lt;/head&gt;&lt;body&gt;&lt;h1&gt;This is a short page to say &#34;Hello&#34;&lt;/h1&gt;&lt;/body&gt;&lt;/html&gt;";
		
		Matcher matcher = Pattern.compile("&#*\\w\\w\\w?\\w?;").matcher(html);
		StringBuffer matchBuffer = new StringBuffer();
		while(matcher.find()){
			matcher.appendReplacement(matchBuffer, codes.get(matcher.group()));
		}
		matcher.appendTail(matchBuffer);
		System.out.println (matchBuffer.toString());
	}
}