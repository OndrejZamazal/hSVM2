package cz.vse.swoe.hsvm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

/**
 * Created by svabo 2015.
 */

public class OntologyCache {
	
	HashMap<String, HashSet<String>> subtypesforSupertypes = null;
	
	public OntologyCache(String input_file) {
		try {
			this.subtypesforSupertypes = new HashMap<String, HashSet<String>>();
			BufferedReader fp = new BufferedReader(new FileReader("res/"+input_file));
			
			while(true)
			{
				String line = fp.readLine();
				if(line == null) break;
				
				StringTokenizer st = new StringTokenizer(line," ");
				String superType = st.nextToken();				
				HashSet<String> subtypes = new HashSet<String>();
				while(st.hasMoreTokens()) {
					subtypes.add(st.nextToken());
				}
				this.subtypesforSupertypes.put(superType, subtypes);
			}
			fp.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}	
	
	public static void main(String[] args) {
		OntologyCache ontologyCache = new OntologyCache("SubClassesMLClassifier.txt");
		System.out.println(ontologyCache.subtypesforSupertypes);
	}

}
