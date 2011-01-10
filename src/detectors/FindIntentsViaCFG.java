/**
 * 
 */
package detectors;

import intentdataflow.Constant;
import intentdataflow.ConstantFrame;
import intentdataflow.Intent;
import intentdataflow.IntentSimulatorDataflow;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InvokeInstruction;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.Priorities;
import edu.umd.cs.findbugs.ba.CFG;
import edu.umd.cs.findbugs.ba.DataflowAnalysisException;
import edu.umd.cs.findbugs.ba.Location;
import edu.umd.cs.findbugs.ba.XFactory;
import edu.umd.cs.findbugs.ba.XMethod;
import edu.umd.cs.findbugs.bcel.CFGDetector;
import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.classfile.Global;
import edu.umd.cs.findbugs.classfile.IAnalysisCache;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;

/**
 * @author sdienst
 *
 */
public class FindIntentsViaCFG extends CFGDetector {
	private static Set<Short> invokeOpcodes=new HashSet<Short>(
			Arrays.asList(
					Constants.INVOKEVIRTUAL, 
					Constants.INVOKEVIRTUAL_QUICK, 
					Constants.INVOKEVIRTUAL_QUICK_W, 
					Constants.INVOKEVIRTUALOBJECT_QUICK));

	private static Set<String> intentInvokers = new HashSet<String>(
		Arrays.asList (
				// Activity
				"startIntentSender","startActivity","startActivityForResult","startActivityFromChild","startActivityIfNeeded","queryIntentActivities",
				// PackageManager
				"queryBroadcastReceivers", "getActivityIcon",
				"getActivityLogo", "queryIntentServices", "resolveActivity", "resolveService"));
	
	private Set<Intent> intents = new HashSet<Intent>();
	private BugReporter bugreporter;
	
	public FindIntentsViaCFG(BugReporter reporter){
		this.bugreporter = reporter;
	}
	/* (non-Javadoc)
	 * @see edu.umd.cs.findbugs.bcel.CFGDetector#visitMethodCFG(edu.umd.cs.findbugs.classfile.MethodDescriptor, edu.umd.cs.findbugs.ba.CFG)
	 */
	@Override
	protected void visitMethodCFG(MethodDescriptor methodDescriptor, CFG cfg)
			throws CheckedAnalysisException {
		IAnalysisCache analysisCache = Global.getAnalysisCache();
		
		ConstantPoolGen cpg = analysisCache.getClassAnalysis(ConstantPoolGen.class, methodDescriptor.getClassDescriptor());
		IntentSimulatorDataflow intentDataflow = analysisCache.getMethodAnalysis(IntentSimulatorDataflow.class, methodDescriptor);
		
		for (Iterator<Location> i = cfg.locationIterator(); i.hasNext();) {
			Location location = i.next();
			short opcode = location.getHandle().getInstruction().getOpcode();
			if (invokeOpcodes.contains(opcode)) {
				XMethod method = XFactory.createXMethod((InvokeInstruction)location.getHandle().getInstruction(), cpg);
				if(intentInvokers.contains(method.getName()))
					inspect(methodDescriptor, cpg, intentDataflow, location);
			}
		}

	}

	private void inspect(MethodDescriptor methodDescriptor,
			ConstantPoolGen cpg, IntentSimulatorDataflow intentDataflow,
			Location location) throws DataflowAnalysisException {
		
		ConstantFrame constantFrame = intentDataflow.getFactAtLocation(location);
		for (int i = 0; i <constantFrame.getNumSlots(); i++) {
			Object value = constantFrame.getStackValue(i).getValue();
			if(value instanceof Intent){
				intents.add((Intent) value);
				break;
			}
		}
	}
	
	@Override
	public void finishPass() {
		for (Intent intent : intents) {
			BugInstance warning = new BugInstance(this, "CREATE_INTENT", Priorities.NORMAL_PRIORITY);
			warning.addString(intent.toString());
			warning.addClass("dummy");
			bugreporter.reportBug(warning);
			System.out.println(intent);
		}
	}

}
