import java.io.*;
import java.nio.channels.*;

public class CopyFile {
    public static void main(String[] args) throws Exception {
	if(args.length != 2){
	    System.out.println ("Usage: CopyFile sourceFile targetFile");
	    System.exit(-1);
  	}

	File sourceFile = new File(args[0]);
	if(!sourceFile.exists()) throw new Exception("SourceFile not found");
	File targetFile = new File(args[1]);

	FileInputStream sourceStream = new FileInputStream(sourceFile);
	FileChannel sourceChannel = sourceStream.getChannel();
	FileOutputStream targetStream = new FileOutputStream(targetFile);
	FileChannel targetChannel = targetStream.getChannel();

	try{
	   // sourceChannel.transferTo(0,sourceChannel.size(),targetChannel);
	   targetChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
	}finally{
	    sourceChannel.close();
	    targetChannel.close();	
	}
    }
}
