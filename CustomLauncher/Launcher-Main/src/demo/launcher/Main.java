package demo.launcher;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.net.URLClassLoader;

import demo.launcher.api.Launcher;

public class Main {
	private static String CLASSPATH_DIR = "lib";
	private static String LIB_EXT = ".jar";
	private static String LAUNCHER_CLASS = "demo.launcher.AppLauncher";
	
	private static ClassLoader cl;
	static{
		try {
			cl = getClassLoaderFromPath(
				new File(CLASSPATH_DIR), 
				Thread.currentThread().getContextClassLoader()
			);
			Thread.currentThread().setContextClassLoader(cl);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Returns a ClassLoader that for the provided path.
	private static ClassLoader getClassLoaderFromPath(File path, ClassLoader parent) throws Exception {
		// get jar files from jarPath
		File[] jarFiles = path.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.getName().endsWith(Main.LIB_EXT);
			}
		});
		URL[] classpath = new URL[jarFiles.length];
		for (int j = 0; j < jarFiles.length; j++) {
			classpath[j] = jarFiles[j].toURI().toURL();
		}
		return new URLClassLoader(classpath, parent);
	}
	
	public static void main(String[] args) throws Exception{
		Launcher launcher = Launcher.class.cast(
			Class.forName(LAUNCHER_CLASS, true, cl).newInstance()
		);
		launcher.launch(new Object[]{"this string is capitalized"});
	}
}
