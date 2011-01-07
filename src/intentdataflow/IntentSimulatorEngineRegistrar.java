/**
 * 
 */
package intentdataflow;

import edu.umd.cs.findbugs.classfile.IAnalysisCache;
import edu.umd.cs.findbugs.classfile.IAnalysisEngineRegistrar;

/**
 * @author sdienst
 *
 */
public class IntentSimulatorEngineRegistrar implements IAnalysisEngineRegistrar {

	/* (non-Javadoc)
	 * @see edu.umd.cs.findbugs.classfile.IAnalysisEngineRegistrar#registerAnalysisEngines(edu.umd.cs.findbugs.classfile.IAnalysisCache)
	 */
	@Override
	public void registerAnalysisEngines(IAnalysisCache analysisCache) {
		new IntentSimulatorDataflowFactory().registerWith(analysisCache);
	}

}
