package classes;

import java.util.HashSet;
import java.util.Set;

public class AllowedPackages {
	
//	private ArrayList<String> allowed = new ArrayList<String>();
	private HashSet<String> allowed = new HashSet<String>();
	
	public AllowedPackages () {
		allowed.add("com.android.phone");
		allowed.add("com.android.contacts");
		allowed.add("com.google.android.maps");
		allowed.add("com.android.internal.app.ResolverActivity");
		allowed.add("com.access2");
	}
	

	public HashSet<String> getAllowed() {
		return allowed;
	}

	public void setAllowed(HashSet<String> allowed) {
		this.allowed = allowed;
	}
	
	public void addPackage (String pack) {
		allowed.add(pack);
	}

}
