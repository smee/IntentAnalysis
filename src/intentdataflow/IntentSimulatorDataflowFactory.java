package intentdataflow;

import org.apache.bcel.generic.MethodGen;

import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.classfile.IAnalysisCache;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;
import edu.umd.cs.findbugs.classfile.engine.bcel.AnalysisFactory;

/**
 * Analysis engine to produce IntentSimulatorDataflow objects for an analyzed method.
 * 
 * @author sdienst
 */
public class IntentSimulatorDataflowFactory extends AnalysisFactory<IntentSimulatorDataflow> {
    public IntentSimulatorDataflowFactory() {
        super("analysis of intent creation", IntentSimulatorDataflow.class);
    }

    /* (non-Javadoc)
     * @see edu.umd.cs.findbugs.classfile.engine.bcel.AnalysisFactory#registerWith(edu.umd.cs.findbugs.classfile.IAnalysisCache)
     */
    @Override
    public void registerWith(IAnalysisCache analysisCache) {
    	analysisCache.registerMethodAnalysisEngine(IntentSimulatorDataflow.class, this);
    }


    /* (non-Javadoc)
     * @see edu.umd.cs.findbugs.classfile.IAnalysisEngine#analyze(edu.umd.cs.findbugs.classfile.IAnalysisCache, java.lang.Object)
     */
    public IntentSimulatorDataflow analyze(IAnalysisCache analysisCache, MethodDescriptor descriptor) throws CheckedAnalysisException {
        MethodGen methodGen = getMethodGen(analysisCache, descriptor);
        if (methodGen == null)
            return null;
        IntentSimulatorAnalysis analysis = new IntentSimulatorAnalysis(methodGen, getDepthFirstSearch(analysisCache, descriptor));
        IntentSimulatorDataflow dataflow = new IntentSimulatorDataflow(getCFG(analysisCache, descriptor), analysis);
        
        dataflow.execute();

        return dataflow;
    }
}
