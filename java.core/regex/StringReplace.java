
public class StringReplace {
	private static final String PATTERN = ".*\\d\\d\\d*\\sfences";
	public static void main(String[] args){
		String str = "The big black dog jumped 88 fences";
		if(str.matches(PATTERN)){
			System.out.println (str.replaceAll(PATTERN, "over all barriers"));
		}else{
			System.out.println ("No dogs found.");
		}
	}
}