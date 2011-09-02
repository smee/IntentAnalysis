/**
 * 
 */
package detectors;

import intentdataflow.ConstantFrame;
import intentdataflow.Intent;
import intentdataflow.IntentFilter;
import intentdataflow.IntentSimulatorDataflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import edu.umd.cs.findbugs.classfile.ClassDescriptor;
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
				// Activity TODO these are special, categories should include the DEFAULT category, too
				"startIntentSender","startActivity","startActivityForResult","startActivityFromChild","startActivityIfNeeded",
				// Context
				"startService", "stopService","bindService","unbindService","sendBroadcast", "sendOrderedBroadcast", "sendStickyBroadcast", "sendStickyOrderedBroadcast"));
	
	private static Set<String> intentQueries = new HashSet<String>(
			Arrays.asList (
					// PackageManager
					"queryBroadcastReceivers", "getActivityIcon", "queryIntentActivities",
					"getActivityLogo", "queryIntentServices", "resolveActivity", "resolveService"));
	private static Set<String> registerFilter = new HashSet<String>( Arrays.asList ("registerReceiver"));
	
	private Map<String,List> intents = new HashMap();
	private Map<String,List> intentsQueried = new HashMap();
	private Map<String,List> receiversRegistered = new HashMap();
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
				
				String methodName = method.getName();
				if(intentInvokers.contains(methodName))
					addInvokation(intents,methodName,inspect(methodDescriptor, cpg, intentDataflow, location));
				else if(intentQueries.contains(methodName))
					addInvokation(intentsQueried,methodName,inspect(methodDescriptor, cpg, intentDataflow, location));
				else if(registerFilter.contains(methodName))
					addInvokation(receiversRegistered,methodName,inspect(methodDescriptor, cpg, intentDataflow, location));
			}
		}

	}
	private void addInvokation(Map<String, List> resultCollector, String methodName, Collection<?> collectedObjects) {
		
		List allIntents= resultCollector.get(methodName);
		if(allIntents == null){
			allIntents=new ArrayList();
			resultCollector.put(methodName,allIntents);
		}
		allIntents.addAll(collectedObjects);
	}
	@Override
	public void visitClass(ClassDescriptor classDescriptor)
			throws CheckedAnalysisException {
		// exclude google's ad library, is obscenely obfuscated
		// and generates a lot of false intent matches
		String pname = classDescriptor.getPackageName();
		if(!pname.startsWith("com.admob.android.ads") && !pname.startsWith("com.google.ads"))
			super.visitClass(classDescriptor);
	}

	private Collection inspect(MethodDescriptor methodDescriptor,
			ConstantPoolGen cpg, IntentSimulatorDataflow intentDataflow,
			Location location) throws DataflowAnalysisException {
		
		Set<Object> intentsAndFilters = new HashSet<Object>();
		
		ConstantFrame constantFrame = intentDataflow.getFactAtLocation(location);
		for (int i = 0; i <constantFrame.getStackDepth(); i++) {
			Object value = constantFrame.getStackValue(i).getValue();
			if(value instanceof Intent || value instanceof IntentFilter){
				intentsAndFilters.add(value);
				break;
			}
		}
		return intentsAndFilters;
	}
	
	@Override
	public void finishPass() {
		StringBuilder sb = new StringBuilder("{:called {");
		sb.append(printExtractedObject(intents));
		sb.append("} :queried {");
		sb.append(printExtractedObject(intentsQueried));
		sb.append("} :registered {");
		sb.append(printExtractedObject(receiversRegistered));
		sb.append("}}");
		
		BugInstance warning = new BugInstance(this, "CREATE_INTENT", Priorities.NORMAL_PRIORITY);
		warning.addString(sb.toString());
		warning.addClass("dummy");
		bugreporter.reportBug(warning);
	}
	private String printExtractedObject(Map<String, List> objects) {
		StringBuilder sb = new StringBuilder();
		for(String method:objects.keySet()){
			sb.append(":").append(method).append(" [");
			for (Object o : objects.get(method)) sb.append(o).append(" ");
			sb.append("], ");
		}
		return sb.toString();
	}

}
