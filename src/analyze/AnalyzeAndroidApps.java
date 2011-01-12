/**
 * 
 */
package analyze;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;

import edu.umd.cs.findbugs.BugCollectionBugReporter;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BugReporterObserver;
import edu.umd.cs.findbugs.DetectorFactory;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.FindBugs2;
import edu.umd.cs.findbugs.PluginException;
import edu.umd.cs.findbugs.Priorities;
import edu.umd.cs.findbugs.Project;
import edu.umd.cs.findbugs.StringAnnotation;
import edu.umd.cs.findbugs.config.UserPreferences;

/**
 * @author sdienst
 *
 */
public class AnalyzeAndroidApps {

	private static final class IntentStringObserver implements
			BugReporterObserver {
		String intent = null;

		@Override
		public void reportBug(BugInstance inst) {
			StringAnnotation annotation = (StringAnnotation) inst.getAnnotations().get(0);
			
			intent=annotation.getValue();
		}

		public String getIntent(){ return intent;}
	}
	/**
	 * @param args
	 * @throws PluginException 
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws PluginException, IOException, InterruptedException {
		File dir = new File("d:/android/jars/PRODUCTIVITY");
		for(File d:dir.listFiles()){
			File f = new File(d,"classes.dex");
			f=new File("D:\\android\\jars\\PRODUCTIVITY\\-1084558843411678631\\classes.dex");
			System.out.println(findIntents(f));
			return;
		}
	}


	public static String findIntents(File jarfile) throws PluginException, IOException, InterruptedException{
		AnalyzeAndroidApps a = new AnalyzeAndroidApps();
		FindBugs2 engine = a.setUpEngine(jarfile.toString());
		IntentStringObserver obs = new IntentStringObserver();
		engine.getBugReporter().addObserver(obs);
		engine.execute();
		return obs.getIntent();
	}
    private FindBugs2 setUpEngine(String fileUriToJar) throws MalformedURLException, PluginException {
    	//Plugin.addCustomPlugin(new URL("file://findintents.jar"));
    	
        FindBugs2 engine = new FindBugs2();
        Project project = new Project();
        project.setProjectName("analyze apps");
        engine.setProject(project);

        DetectorFactoryCollection detectorFactoryCollection = DetectorFactoryCollection.instance();
        engine.setDetectorFactoryCollection(detectorFactoryCollection);

        BugCollectionBugReporter bugReporter = new BugCollectionBugReporter(project);
        bugReporter.setPriorityThreshold(Priorities.LOW_PRIORITY);
        bugReporter.setErrorVerbosity(BugReporter.SILENT);
        
        engine.setBugReporter(bugReporter);
        UserPreferences preferences = UserPreferences.createDefaultUserPreferences();
        preferences.setEffort(UserPreferences.EFFORT_MIN);
        
        DetectorFactory mydetector = DetectorFactoryCollection.instance().getFactory("FindIntentsViaCFG");
        preferences.enableAllDetectors(false);
        preferences.enableDetector(mydetector, true);
        preferences.getFilterSettings().clearAllCategories();
        
        engine.setUserPreferences(preferences);
        // disable TypeDataflow and ValueNumberDataflow, we don't user their results anyway
        engine.setAnalysisFeatureSettings(preferences.getAnalysisFeatureSettings());
        
        project.addFile(fileUriToJar);

        project.addAuxClasspathEntry("D:/android/android-sdk-windows/platforms/android-8/android.jar ");
        return engine;
    }
}
