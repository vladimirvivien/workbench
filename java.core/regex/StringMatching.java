
public class StringMatching {
	private static final String PATTERN = ".*dog.*fence";
	public static void main (String[] args){
		String str = "The big black dog jumps over the wooden fence";
		if(str.matches(PATTERN)){
			System.out.println ("The dog jumped");
		}else{
			System.out.println ("The cat meaow");
		}
	}
}