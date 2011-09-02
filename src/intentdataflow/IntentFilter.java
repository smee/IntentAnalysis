/**
 * 
 */
package intentdataflow;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.generic.Type;

/**
 * @author sdienst
 * 
 */
public class IntentFilter {
	Set<String> actions = new HashSet<String>(),
			categories = new HashSet<String>(),
			dataTypes = new HashSet<String>(),
			dataSchemes = new HashSet<String>();
	Map<Object, Object> dataPaths = new HashMap<Object, Object>();

	public IntentFilter() {
	}

	public void copy(IntentFilter o) {
		this.actions.addAll(o.actions);
		this.categories.addAll(o.categories);
		this.dataTypes.addAll(o.dataTypes);
		this.dataSchemes.addAll(o.dataSchemes);
		this.dataPaths.putAll(o.dataPaths);
	}


	public final void addAction(String action) {
		this.actions.add(action);
	}

	public final void addDataType(String type) {
		this.dataTypes.add(type);
	}

	public final void addDataScheme(String scheme) {
		this.dataSchemes.add(scheme);
	}

	public final void addDataAuthority(String host, String port) {
		// TODO
	}

	public final void addDataPath(String path, Integer i) {
		this.dataPaths.put(path,i);
	}

	public final void addCategory(String category) {
		this.categories.add(category);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("{");
		sb.append(", :actions ").append(Intent.toClojure(actions));
		sb.append(", :categories ").append(Intent.toClojure(categories));
		sb.append(", :datatypes ").append(Intent.toClojure(dataTypes));
		sb.append(", :schemes ").append(Intent.toClojure(dataSchemes));
		sb.append(", :paths ").append(Intent.toClojure(dataPaths));
		sb.append("}");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((actions == null) ? 0 : actions.hashCode());
		result = prime * result
				+ ((categories == null) ? 0 : categories.hashCode());
		result = prime * result
				+ ((dataPaths == null) ? 0 : dataPaths.hashCode());
		result = prime * result
				+ ((dataSchemes == null) ? 0 : dataSchemes.hashCode());
		result = prime * result
				+ ((dataTypes == null) ? 0 : dataTypes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IntentFilter other = (IntentFilter) obj;
		if (actions == null) {
			if (other.actions != null)
				return false;
		} else if (!actions.equals(other.actions))
			return false;
		if (categories == null) {
			if (other.categories != null)
				return false;
		} else if (!categories.equals(other.categories))
			return false;
		if (dataPaths == null) {
			if (other.dataPaths != null)
				return false;
		} else if (!dataPaths.equals(other.dataPaths))
			return false;
		if (dataSchemes == null) {
			if (other.dataSchemes != null)
				return false;
		} else if (!dataSchemes.equals(other.dataSchemes))
			return false;
		if (dataTypes == null) {
			if (other.dataTypes != null)
				return false;
		} else if (!dataTypes.equals(other.dataTypes))
			return false;
		return true;
	}

	public void init(Constant[] args, Type[] argumentTypes,
			ConstantPool constantPool) {
		if (Intent.isString(argumentTypes[0])) {
			this.actions.add(args[0].getConstantString());
		}else this.copy((IntentFilter)args[0].getValue());
		
		if (args.length > 1){
			if (Intent.isString(argumentTypes[1])) {
				this.dataTypes.add(args[1].getConstantString());
			} else throw new RuntimeException("Don't know how to handle constructor calls to Intentfilter with arguments of type"+Arrays.asList(argumentTypes));
			}

	}
}
