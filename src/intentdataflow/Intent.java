package intentdataflow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.generic.Type;

public class Intent {

	String action;
	String uri;
	String mimetype;

	Map<Constant, Constant> extras;
	private String calledClass;
	private Set<String> categories;

	public Intent() {
		this.extras = new HashMap<Constant, Constant>();
		this.categories = new HashSet<String>();
	}

	public void setExtra(Constant name, Constant value) {
		this.extras.put(name, value);
	}

	public void setAction(String action) {
		this.action = action;
	}

	public boolean isExplicit() {
		return calledClass != null;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Intent[");
		if (isExplicit())
			sb.append(calledClass);
		else {
			sb.append("action=").append(action).append(", uri=").append(uri)
					.append(", mimetype=").append(mimetype);
			if (!extras.isEmpty())
				sb.append(", extras=" + extras);
			sb.append(", categories=").append(categories);
			sb.append("]");
		}
		return sb.toString();
	}

	public void addCategory(String cat) {
		this.categories.add(cat);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = prime * result
				+ ((calledClass == null) ? 0 : calledClass.hashCode());
		result = prime * result
				+ ((categories == null) ? 0 : categories.hashCode());
		result = prime * result + ((extras == null) ? 0 : extras.hashCode());
		result = prime * result
				+ ((mimetype == null) ? 0 : mimetype.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
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
		Intent other = (Intent) obj;
		if (action == null) {
			if (other.action != null)
				return false;
		} else if (!action.equals(other.action))
			return false;
		if (calledClass == null) {
			if (other.calledClass != null)
				return false;
		} else if (!calledClass.equals(other.calledClass))
			return false;
		if (categories == null) {
			if (other.categories != null)
				return false;
		} else if (!categories.equals(other.categories))
			return false;
		if (extras == null) {
			if (other.extras != null)
				return false;
		} else if (!extras.equals(other.extras))
			return false;
		if (mimetype == null) {
			if (other.mimetype != null)
				return false;
		} else if (!mimetype.equals(other.mimetype))
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

	public void callConstructor(Constant[] args, Type[] argumentTypes, ConstantPool constantPool) {
		if(argumentTypes.length==0)
			return;
		// TODO recognize setters for all values
		if ("Ljava/lang/String;".equals(argumentTypes[0].getSignature())) {
			this.action = args[0].getConstantString();
		}
		if (args.length > 1)
			if ("Landroid/net/Uri;".equals(argumentTypes[1].getSignature())) {
				this.uri = args[1].getConstantString();
			} else if ("Ljava/lang/Class;".equals(argumentTypes[1].getSignature())) {
				this.calledClass = (String) ((ConstantClass) args[1].getValue()).getConstantValue(constantPool);
			}

	}

}
