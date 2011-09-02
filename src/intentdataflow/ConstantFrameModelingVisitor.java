/*
 * Bytecode Analysis Framework
 * Copyright (C) 2005, University of Maryland
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package intentdataflow;

import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.generic.BIPUSH;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.ICONST;
import org.apache.bcel.generic.IINC;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.LDC2_W;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.SIPUSH;

import edu.umd.cs.findbugs.ba.AbstractFrameModelingVisitor;
import edu.umd.cs.findbugs.ba.AnalysisContext;
import edu.umd.cs.findbugs.ba.DataflowAnalysisException;

/**
 * Visitor to model the effect of bytecode instructions on ConstantFrames.
 * 
 * <p>
 * For now, only String constants are modeled. In the future we can add other
 * kinds of constants.
 * </p>
 * 
 * @see edu.umd.cs.findbugs.ba.constant.ConstantAnalysis
 * @author David Hovemeyer
 */
public class ConstantFrameModelingVisitor extends AbstractFrameModelingVisitor<Constant, ConstantFrame> {

    public ConstantFrameModelingVisitor(ConstantPoolGen cpg) {
        super(cpg);
    }

    @Override
    public Constant getDefaultValue() {
        return Constant.NOT_CONSTANT;
    }

    @Override
    public void visitIINC(IINC obj) {
        // System.out.println("before iinc: " + getFrame());
        int v = obj.getIndex();
        int amount = obj.getIncrement();
        ConstantFrame f = getFrame();
        Constant c = f.getValue(v);
        if (c.isConstantInteger())
            f.setValue(v, new Constant(c.getConstantInt() + amount));
        else
            f.setValue(v, Constant.NOT_CONSTANT);
        // System.out.println("after iinc: " + getFrame());
    }

    @Override
    public void visitICONST(ICONST obj) {
        Number value = obj.getValue();
        Constant c = new Constant(value);
        getFrame().pushValue(c);
    }

    @Override
    public void visitBIPUSH(BIPUSH obj) {
        Number value = obj.getValue();
        Constant c = new Constant(value);
        getFrame().pushValue(c);
    }

    @Override
    public void visitSIPUSH(SIPUSH obj) {
        Number value = obj.getValue();
        Constant c = new Constant(value);
        getFrame().pushValue(c);
    }

    @Override
    public void visitLDC(LDC obj) {
        Object value = obj.getValue(getCPG());
        Constant c = new Constant(value);
        getFrame().pushValue(c);
    }

    @Override
    public void visitLDC2_W(LDC2_W obj) {
        Object value = obj.getValue(getCPG());
        Constant c = new Constant(value);
        getFrame().pushValue(c);
        getFrame().pushValue(c);
    }

    @Override
    public void visitNEW(NEW obj) {
    	ObjectType loadClassType = obj.getLoadClassType(cpg);
    	if("android.content.Intent".equals(loadClassType.getClassName())){
    		getFrame().pushValue(new Constant(new Intent()));
    	}else if("android.content.IntentFilter".equals(loadClassType.getClassName())){
        		getFrame().pushValue(new Constant(new IntentFilter()));
    	}else
    		modelInstruction(obj, getNumWordsConsumed(obj), getNumWordsProduced(obj), new Constant(obj.getLoadClassType(getCPG())));
    }
    @Override
    public void visitGETFIELD(GETFIELD obj) {
    	super.visitGETFIELD(obj);
    }
    @Override
	public void visitINVOKESPECIAL(INVOKESPECIAL obj) {
		ObjectType loadClassType = obj.getLoadClassType(cpg);
		String methodName = obj.getMethodName(cpg);
		if (methodName.equals("<init>")) {
			if (loadClassType.getClassName().equals("android.content.Intent")) {
				Constant[] args = getCallParameters(obj);
				Constant constant = popFrameTop();//the instance was put on the stack twice (new/dup), simulate it
				if (args.length > 0)
					((Intent) constant.getValue()).init(args,
							obj.getArgumentTypes(cpg), cpg.getConstantPool());
			} else if (loadClassType.getClassName().equals("android.content.IntentFilter")) {
				Constant[] args = getCallParameters(obj);
				Constant constant = popFrameTop();//the instance was put on the stack twice (new/dup), simulate it
				if (args.length > 0)
					((IntentFilter) constant.getValue()).init(args,
							obj.getArgumentTypes(cpg), cpg.getConstantPool());
			}
		}else
			super.visitINVOKESPECIAL(obj);

	}

	private Constant popFrameTop() {
		try {
			return getFrame().popValue();
		} catch (DataflowAnalysisException e) {
			AnalysisContext.logError("Could not pop from call stack ", e);
			return null;
		}
	}
	private Constant getTopValue() {
		try {
			return getFrame().getTopValue();
		} catch (DataflowAnalysisException e) {
			AnalysisContext.logError("Could not pop from call stack ", e);
			return null;
		}
	}
	private Constant[] getCallParameters(InvokeInstruction obj){
		try{
			final int numArguments = getFrame().getNumArguments(obj, cpg);
			
			Constant[] args = new Constant[numArguments];
			for (int i = numArguments-1; i >=0 ; i--) {
				args[i] = getFrame().popValue();
			}
			return args;
		} catch (DataflowAnalysisException e) {
			AnalysisContext.logError("Could not simulate call "+obj, e);
			// FIXME handle better?
			return new Constant[0];
		}
	}
	@Override
	public void visitINVOKEVIRTUAL(INVOKEVIRTUAL obj) {
		ObjectType loadClassType = obj.getLoadClassType(cpg);
		String methodName = obj.getMethodName(cpg);

		if (loadClassType.getClassName().equals("android.content.Intent")) {
			if (methodName.equals("putExtra")) {
				// Intent.putExtra(...)
				Constant[] args = getCallParameters(obj);
				Constant c = popFrameTop();
				((Intent) c.getValue()).setExtra(args[0].getConstantString(), args[1].getConstantString());
				getFrame().pushValue(c);
			} else if (methodName.equals("setAction")) {
				// Intent.putExtra(...)
				Constant[] args = getCallParameters(obj);
				Constant c = popFrameTop();
				((Intent) c.getValue()).setAction(args[0].getConstantString());
				getFrame().pushValue(c);
			} else if (methodName.equals("addCategory")) {
				// Intent.putExtra(...)
				Constant[] args = getCallParameters(obj);
				Constant c = popFrameTop();
				((Intent) c.getValue()).addCategory(args[0].getConstantString());
				getFrame().pushValue(c);
			} else if (methodName.equals("setData")) {
				// Intent.putExtra(...)
				Constant[] args = getCallParameters(obj);
				Constant c = popFrameTop();
				((Intent) c.getValue()).setData(args[0].getConstantString());
				getFrame().pushValue(c);
			} else if (methodName.equals("setType")) {
                // Intent.putExtra(...)
                Constant[] args = getCallParameters(obj);
                Constant c = popFrameTop();
                ((Intent) c.getValue()).setType(args[0].getConstantString());
                getFrame().pushValue(c);
            } else if (methodName.equals("setDataAndType")) {
                // Intent.putExtra(...)
                Constant[] args = getCallParameters(obj);
                Constant c = popFrameTop();
                ((Intent) c.getValue()).setData(args[0].getConstantString());
                ((Intent) c.getValue()).setType(args[1].getConstantString());
                getFrame().pushValue(c);
            }else if (methodName.equals("setClassName")) {
                // Intent.putExtra(...)
                Constant[] args = getCallParameters(obj);
                Constant c = popFrameTop();
                ((Intent) c.getValue()).setClassname(args,obj.getArgumentTypes(cpg),cpg.getConstantPool());
                getFrame().pushValue(c);
            }else if (methodName.equals("setComponent")) {
                // Intent.putExtra(...)
                Constant[] args = getCallParameters(obj);
                Constant c = popFrameTop();
                ((Intent) c.getValue()).setComponent(args[0],obj.getArgumentTypes(cpg),cpg.getConstantPool());
                getFrame().pushValue(c);
            }
		}else if (loadClassType.getClassName().equals("android.content.IntentFilter")) {
			if (methodName.equals("addAction")) {
				Constant[] args = getCallParameters(obj);
				Constant c = popFrameTop();
				((IntentFilter) c.getValue()).addAction(args[0].getConstantString());
				getFrame().pushValue(c);
			} else if (methodName.equals("addCategory")) {
				Constant[] args = getCallParameters(obj);
				Constant c = popFrameTop();
				((IntentFilter) c.getValue()).addCategory(args[0].getConstantString());
				getFrame().pushValue(c);
			} else if (methodName.equals("addDataType")) {
				Constant[] args = getCallParameters(obj);
				Constant c = popFrameTop();
				((IntentFilter) c.getValue()).addDataType(args[0].getConstantString());
				getFrame().pushValue(c);
			} else if (methodName.equals("addDataScheme")) {
				// Intent.putExtra(...)
				Constant[] args = getCallParameters(obj);
				Constant c = popFrameTop();
				((IntentFilter) c.getValue()).addDataScheme(args[0].getConstantString());
				getFrame().pushValue(c);
			} else if (methodName.equals("addDataAuthority")) {
				// Intent.putExtra(...)
				Constant[] args = getCallParameters(obj);
				Constant c = popFrameTop();
				((IntentFilter) c.getValue()).addDataAuthority(args[0].getConstantString(),args[1].getConstantString());
				getFrame().pushValue(c);
			} else if (methodName.equals("addDataPath")) {
				// Intent.putExtra(...)
				Constant[] args = getCallParameters(obj);
				Constant c = popFrameTop();
				((IntentFilter) c.getValue()).addDataPath(args[0].getConstantString(), args[1].getConstantInt());
				getFrame().pushValue(c);
			} 
		} else if (loadClassType.getClassName().equals("java.lang.Class")
				&& methodName.equals("getName")) {
			// java.lang.Class.getName()
			Constant clz = getTopValue();
			popFrameTop();
			getFrame().pushValue(
					new Constant(((ConstantClass) clz.getValue())
							.getConstantValue(cpg.getConstantPool())));
		} else {
			super.visitINVOKEVIRTUAL(obj);
		}
	}


	@Override
	public void visitINVOKESTATIC(INVOKESTATIC obj) {
    	ObjectType loadClassType = obj.getLoadClassType(cpg);
    	String methodName = obj.getMethodName(cpg);
		if (loadClassType.getClassName().equals("android.net.Uri") && methodName.equals("parse")) {
    		// android.net.Uri.parse(...)
			Constant[] args = getCallParameters(obj);
			getFrame().pushValue(new Constant(args[0].getValue()));
		} else if (loadClassType.getClassName().equals("android.content.IntentFilter") && methodName.equals("create")) {
    		// android.content.IntentFilter.create(action,datatype)
			Constant[] args = getCallParameters(obj);
			IntentFilter intentfilter = new IntentFilter();
			intentfilter.addAction(args[0].getConstantString());
			intentfilter.addDataType(args[1].getConstantString());
			getFrame().pushValue(new Constant(intentfilter));
		} else
			super.visitINVOKESTATIC(obj);
	}
}
