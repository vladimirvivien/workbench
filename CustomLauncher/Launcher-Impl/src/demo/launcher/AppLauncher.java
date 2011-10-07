package demo.launcher;

import demo.launcher.api.Launcher;

public class AppLauncher implements Launcher {
	public int launch(Object ... args) {
		String result = org.apache.commons.lang3.text.WordUtils.capitalize((String)args[0]);
		System.out.println (result);
		return 0;
	}

}
