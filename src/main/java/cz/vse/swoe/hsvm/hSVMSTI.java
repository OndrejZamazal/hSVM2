package cz.vse.swoe.hsvm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.jena.riot.RDFDataMgr;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * Created by svabo 2015.
 */

public class hSVMSTI {
	
	HashMap<String, HashMap<String,ProbClass3>> lhdtype_to_sti = null; //mapovani resource type na sti types
	HashMap<String, String> resource_lhdtype = null;
	
	OntologyCache ontCache = null;
	
	public hSVMSTI(String input, String debug_input) {
		try {				
			//init OntologyCache with MLClassifier3a ontology (we do not use directly DBpedia ontology for propagation)
			ontCache = new OntologyCache("SubClassesMLClassifier.txt");
			
			//1. mapovani resource na lhd type. 
			resource_lhdtype = new HashMap<String, String>();
			
			//resource to lhd_type
			Model model = ModelFactory.createDefaultModel();
			model = RDFDataMgr.loadModel(input) ;
			
			for (Iterator<Statement> it = model.listStatements(); it.hasNext(); ) {
                final Statement stmt = it.next();
                String res=stmt.getSubject().getURI();;
				String lhdtype=stmt.getObject().toString();
				resource_lhdtype.put(res, lhdtype);
			}
			model.close();
			
			//2. mapovani lhd type na sti typy
			lhdtype_to_sti = new HashMap<String, HashMap<String,ProbClass3>>();
			lhdtype_to_sti = this.read_debug_results(debug_input);
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private HashMap<String, HashMap<String,ProbClass3>> read_debug_results(String file) {
		try {
			HashMap<String, HashMap<String,ProbClass3>> results = new HashMap<String, HashMap<String,ProbClass3>>();
			
			BufferedReader vstup = null;
			int position=0; //1=type,2=list,3=pruned,4=selected
			vstup = new BufferedReader(new FileReader(file));				
			String s="";
			HashMap<String,ProbClass3> list=new HashMap<String,ProbClass3>();
			String lhdtype="";			
			while ((s = vstup.readLine()) != null) {				
				if(s.trim().equals("# type"))	 {
					//ulozit do HashMapy
					if(!lhdtype.equals("")) {
						results.put(lhdtype, list);
					}
					position=1;
					lhdtype="";
				}
				else if(s.trim().equals("#list of  candidate mapped types,frequency,confidence"))	 {
					position=2;
					list=new HashMap<String,ProbClass3>();
				}
				else if(s.trim().equals("#Pruned set of types,frequency,confidence"))	 {
					position=3;
					//to me ted nezajima
				}
				//else if(s.trim().equals("#Selected mapping, confidence"))	 {
				else if(s.trim().equals("#Selected mapping, confidence"))	 {
					position=4;
					//to me ted nezajima
				}
				else if(position==1) {
					//lhdtype=s.trim().replaceAll("http://dbpedia.org/resource/", "");					
					lhdtype=s.trim();
				}
				else if(position==2) {
					s = s.trim();
					String type = s.substring(0,s.indexOf(","));
					String value = s.substring(s.lastIndexOf(",")+1);
					list.put(type.trim(), new ProbClass3(new Double(value).doubleValue()));					
				}
				else if(position==3) {
					;
				}
				else if(position==4) {
					;
				}
			}
			//zpracovat posledni zaznam: ulozit do HashMapy
			results.put(lhdtype, list);
			
			vstup.close();
			return results;
		}
		catch(Exception e) {			
			e.printStackTrace();
			return null;
		}
	}
	
	private ProbClass2 get_type_with_maximum_average(HashMap<String, ProbClass3> current_results) {
		String max_type="";
		double max_prob=0.0;
		double prob=0.0;
		for(String type : current_results.keySet()) {			
			//prob=this.types.get(type).prob_sum/this.types.get(type).counter;
			prob=current_results.get(type).prob_sum;
			((ProbClass3)current_results.get(type)).prob=prob;
			
			if (prob>max_prob) {
				max_prob=prob;
				max_type=type;
			}
		}
			
		return(new ProbClass2(max_type,max_prob,max_prob));
	}
	
	//11-06-15, fuse only where sti is unsure variant 2 using e.g. en.lhd.inference.2015.nt.gz as mapping to sti file
	public void fuse_unsure_sti2(String input_file, String mapping_to_sti_file, String whitelist_file) {
		//TODO: according to combine_sti_ml_with_propagation()
		try {
			//lhd_type to sti_type mapping
			Model model = ModelFactory.createDefaultModel();
			model = RDFDataMgr.loadModel(mapping_to_sti_file) ;
			HashMap<String,String> resource_stitype_mapping = new HashMap<String,String>();
			
			for (Iterator<Statement> it = model.listStatements(); it.hasNext(); ) {
                final Statement stmt = it.next();
                String res=stmt.getSubject().getURI();;
				String sti_type=stmt.getObject().toString();
				resource_stitype_mapping.put(res, sti_type.substring(sti_type.lastIndexOf("/")+1));
			}
			model.close();
			//whitelist
			BufferedReader f2 = new BufferedReader(new FileReader(whitelist_file));
			HashSet<String> whitelist = new HashSet<String>();
			while(true)
			{
				String r = f2.readLine();
				
				if(r == null) break;
				//System.out.println(line);
				r=r.substring(0,r.indexOf(","));
				//System.out.println(r);
				//System.out.println(lhd_type+";"+sti_type);
				whitelist.add(r);
				
			}
			f2.close();

			//process all resource from debug_hSVM_input file
			BufferedReader fp = new BufferedReader(new FileReader(input_file));
			ArrayList<String> hSVM_types = new ArrayList<String>(); 
			HashMap<String, ProbClass3> hSVM_results = null;
			HashMap<String, ProbClass3> sti_results = null;
			String resource = "";
			
			PrintWriter toFile = new PrintWriter(new FileWriter("en.hSVMSTI.nt", false));
			int only_sti_type = 0;
			
			while(true)
			{
				String line = fp.readLine();
				
				if(line == null) break;
				//System.out.println(line);
				
				if(line.matches("#.*")) {
					//getting names of classes
					StringTokenizer st = new StringTokenizer(line.replaceAll("#", "")," ");
					while(st.hasMoreTokens()) {
						hSVM_types.add(st.nextToken());
					}					
					continue;
				}
								
				//init all types from hSVM results
				hSVM_results = new HashMap<String, ProbClass3>();
				resource = line.substring(1, line.lastIndexOf(">"));
				line = line.substring(line.lastIndexOf(">")+2);
				StringTokenizer st = new StringTokenizer(line," ");
				int i=0;
				while(st.hasMoreTokens()) {					
					String prob = st.nextToken();
					hSVM_results.put(hSVM_types.get(i), new ProbClass3(new Double(prob)));
					i++;
				}
				
				//sti - combine and propagate sti confidences to ml
				sti_results = new HashMap<String, ProbClass3>();
				
				if(!whitelist.contains(this.resource_lhdtype.get(StringEscapeUtils.unescapeEcmaScript(resource)))) {
					only_sti_type++;
					String result=resource_stitype_mapping.get(StringEscapeUtils.unescapeEcmaScript(resource));
					String localName=resource.substring(resource.lastIndexOf("/")+1);
					resource=resource.substring(0, resource.lastIndexOf("/")+1);
					
					toFile.println("<"+resource+localName+"> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/"+result+"> .");
					toFile.flush();
					
					continue;
				}
				
				//02-02-15, sometimes there is no mapping to types
				if (this.lhdtype_to_sti.get(this.resource_lhdtype.get(resource))!=null)
					sti_results = this.lhdtype_to_sti.get(this.resource_lhdtype.get(resource));
				
				//propagate:
				for(String type : sti_results.keySet()) {					
					if(hSVM_results.containsKey(type.trim())) {
						hSVM_results.get(type).addProb(sti_results.get(type).prob_sum);					
						//propagation to subclasses
						if (this.ontCache.subtypesforSupertypes.containsKey(type)) {
							//apply for get_subclasses(pc2.target_class)
							for(String subclass : this.ontCache.subtypesforSupertypes.get(type)) {
								//System.out.println("apply for subclass "+subclass);
								if (hSVM_results.containsKey(subclass)) {
									hSVM_results.get(subclass).addProb(sti_results.get(type).prob_sum);
								}
								else {									
									//hSVM_results.put(subclass, sti_results.get(type));
									hSVM_results.put(type, new ProbClass3(sti_results.get(type).prob_sum));
								}
							}
						}
					}
					else {
						hSVM_results.put(type, new ProbClass3(sti_results.get(type).prob_sum));
						//propagation to subclasses
						if (this.ontCache.subtypesforSupertypes.containsKey(type)) {
							//apply for get_subclasses(pc2.target_class)
							for(String subclass : this.ontCache.subtypesforSupertypes.get(type)) {
								//System.out.println("apply for subclass "+subclass);
								if (hSVM_results.containsKey(subclass)) {									
									hSVM_results.get(subclass).addProb(sti_results.get(type).prob_sum);
								}
								else {
									hSVM_results.put(subclass, new ProbClass3(sti_results.get(type).prob_sum));
								}
							}
						}
					}
				}				
				ProbClass2 fin_type= this.get_type_with_maximum_average(hSVM_results);
				String result=fin_type.target_class;
				String localName=resource.substring(resource.lastIndexOf("/")+1);
				resource=resource.substring(0, resource.lastIndexOf("/")+1);

				toFile.println("<"+resource+localName+"> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/"+result+"> .");
				toFile.flush();
			}
			System.out.println(only_sti_type);
			fp.close();
			toFile.close();
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
	}

}
