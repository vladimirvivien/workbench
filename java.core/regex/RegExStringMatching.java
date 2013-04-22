
import java.util.regex.*;

public class RegExStringMatching {
	public static void main (String[] args){
		Pattern pattern = Pattern.compile(".*dog.*fence");
		String str = "The big black dog jumps over the wooden fence";
		Matcher matcher = pattern.matcher(str);
		
		if(matcher.matches()){
			System.out.println ("The dog jumped");
		}else{
			System.out.println ("The cat meaow");
		}
	}
}