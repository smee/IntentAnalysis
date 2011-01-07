package detectors;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;
import edu.umd.cs.findbugs.Priorities;

/**
 * 
 */

/**
 * @author sdienst
 *
 */
public class FindIntentCreations extends BytecodeScanningDetector {

	private final BugReporter bugreporter;
	
	public FindIntentCreations(BugReporter br){
		this.bugreporter = br;
	}
	@Override
	public void sawOpcode(int seen) {
		if(seen != INVOKESPECIAL)
			return;
		String calledClassName = getClassConstantOperand();
		String calledMethodName = getNameConstantOperand();
//		String calledMethodSig = getSigConstantOperand();
		
		if(calledMethodName.equals("<init>") && 
				calledClassName.equals("android/content/Intent")){
			BugInstance warning = new BugInstance(this, "CREATE_INTENT", Priorities.NORMAL_PRIORITY)
				.addClassAndMethod(this)
				.addSourceLine(this);
			bugreporter.reportBug(warning);
		}
	}
}
