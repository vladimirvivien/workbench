import java.util.regex.*;

public class RegExStringReplace {
	public static void main (String[] args){
		// NOTE: pattern is for targeted string, not for entire subject. 
		// this would fail a matches() test. But is perfect for iterative find()'s.
		Pattern pattern = Pattern.compile("[b|B|p|p]ig\\s+[b|B]lack"); 
		String str = "The big black dog jumps over the big black wooden fence while running away from the Big Black Cat";
		Matcher matcher = pattern.matcher(str);
		
		while (matcher.find()){
			System.out.println ("Match found at [" + matcher.start() + "," + matcher.end() + "]");
			System.out.println ("String matched: " + matcher.group());
			System.out.println ("------");
		}
	}
}