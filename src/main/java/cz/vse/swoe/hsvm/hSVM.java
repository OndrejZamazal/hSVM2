package cz.vse.swoe.hsvm;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.jena.riot.RDFDataMgr;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;

import libsvm.svm;
import libsvm.svm_model;

/**
 * Created by svabo 2015.
 */

class ProbClass3 {
	//prob is fin_prob after the run of the algorithm
	double prob = 0;
	double prob_sum = 0;
	int counter = 0;
	
	public ProbClass3(double prob) {
		this.prob_sum=prob;		
		if (prob!=0)
			this.counter=1;
	}
	
	public void addProb(double prob) {
		this.prob_sum+=prob;
		this.counter++;
	}
	
	public String toString() {		
		return this.prob_sum+":"+this.counter+":"+prob;
	}
}

public class hSVM {
	
	protected OntologyCache ontCache = null;
	protected LibSVMClassifier lc = null;
	protected ResourceAttributesLucene res = null;
	HashMap<String, ProbClass3> types = null;
	ProbClass2 fin_type= null;
	HashMap<String,svm_model> models_sa = null;
	HashMap<String,svm_model> models_cat = null;
	HashSet<String> all_types = null;
	
	final private File indexDir1Sa = new File("testindex1Sa");
	final private File indexDir1Cat = new File("testindex1Cat");
	final private File indexDir2 = new File("testindex2");
	
	//TODO: some parameters, e.g. DBpedia datasets to work on etc.
	public hSVM() {
		try {
			ontCache =  new OntologyCache("SubClassesMLClassifier.txt");
			lc = new LibSVMClassifier("svmModelsSubclasses.txt");
			res = new ResourceAttributesLucene(false, "res/uniqueAttributesSa.txt", "res/uniqueAttributesCat.txt");
			//loading models sa and cat										
			this.models_sa = new HashMap<String, svm_model>();
			this.models_cat = new HashMap<String, svm_model>();
			for(String m_name : lc.class_names.keySet()) {
				this.models_sa.put(m_name, svm.svm_load_model("res/LibSVM-model/sa-"+m_name+".model"));			
				this.models_cat.put(m_name, svm.svm_load_model("res/LibSVM-model/cat-"+m_name+".model"));
			}
			
			this.types = new HashMap<String, ProbClass3>();
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private String getAllTypes() {
		StringBuffer output= new StringBuffer("#List of all types\n");
		for(String type : this.types.keySet()) {
			output.append(type+","+this.types.get(type).prob +"\n");
		}
		return output.toString();
	}
	
	//21-04-15, kratsi verze vypisu
	private String getAllTypes2() {
		
		StringBuffer output= new StringBuffer("");
		for(String m_name : this.lc.class_names.keySet()) {
			for(String s : this.lc.class_names.get(m_name)) {
				DecimalFormat df = new DecimalFormat("#.###");
				output.append(Double.valueOf(df.format(this.types.get(s).prob))+" ");
				//output.append(s+":"+Double.valueOf(df.format(this.types.get(s).prob))+"\n");
			}
		}
		return output.toString().trim();
	}
	
	public void process_resources_all_sti2(String input_file, String smoothing, String strategy) {
		try {
			PrintWriter toFile, toFile1, toFile2;
			toFile = new PrintWriter(new FileWriter(input_file.substring(0, input_file.indexOf("."))+".hSVM.nt", false));
			toFile2 = new PrintWriter(new FileWriter(input_file.substring(0, input_file.indexOf("."))+".debug.hSVM", false));
			
			StringBuffer output= new StringBuffer("");
			for(String m_name : this.lc.class_names.keySet()) {
				for(String s : this.lc.class_names.get(m_name)) {
					output.append(s+" ");
				}
			}
			toFile2.append("#"+output.toString().trim()+"\n");
			
			Model model = ModelFactory.createDefaultModel();
			model = RDFDataMgr.loadModel(input_file) ;
			
			for (Iterator<Statement> it = model.listStatements(); it.hasNext(); ) {
                final Statement stmt = it.next();
                String resource=stmt.getSubject().getURI();
                //System.out.println(stmt.getSubject().getLocalName());
                //10-06-15
                String URIpart = resource.substring(0,resource.lastIndexOf("/")+1);
                String localName=resource.substring(resource.lastIndexOf("/")+1);
                //init
				this.types.clear();
				this.fin_type=null;
				
				//classify
				this.computeTypesProbability(resource, smoothing, strategy);
				String result=this.fin_type.target_class;
				localName=StringEscapeUtils.escapeEcmaScript(localName);
				//output
				toFile.println("<"+URIpart+localName+"> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/"+result+"> .");
				toFile.flush();
				//debugging (verbose) output
				toFile2.print("<"+URIpart+localName+"> ");
				toFile2.print(this.getAllTypes2());
				toFile2.println();
				toFile2.flush();
            }
			model.close();
			toFile.close();
			toFile2.close();			
			this.res.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private ProbClass2 get_type_with_maximum_average(String strategy) {
		String max_type="";
		double max_prob=0.0;
		double prob=0.0;
		for(String type : this.types.keySet()) {			
			prob=this.types.get(type).prob_sum/this.types.get(type).counter;
			((ProbClass3)this.types.get(type)).prob=prob;
			
			//alpha strategy
			if (strategy.equals("alpha")) {
				if(ontCache.subtypesforSupertypes.containsKey(type)) 
					continue;
				else if (prob>max_prob) {
					max_prob=prob;
					max_type=type;
				}
			}
			
			//nastaveni beta strategy superTypes se uvazuji vsechny
			else if (strategy.equals("beta")) {
				if (prob>max_prob) {
					max_prob=prob;
					max_type=type;
				}
			}
		}
		
		return(new ProbClass2(max_type,max_prob,max_prob));
	}
	
	public void computeTypesProbability(String resource, String smoothing, String strategy) {
		try {
			//init
			this.types.clear();
			this.fin_type = null;
			
			res.get_categories_and_short_abstracts_for_resource_lucene(resource);					
			String sa = res.createOneBowRepresentationSa();
			String cat = res.createOneBowRepresentationCat();
			//iterate over both variants of models and predict by all models here (input resource and output all probabilities for all classes)
			for(String m_name : lc.class_names.keySet()) {
				ArrayList<ProbClass2> sa_classified_classes = new ArrayList<ProbClass2>();
				HashMap<String,Double> sa_types_to_prob = new HashMap<String,Double>(); 				
				ArrayList<ProbClass2> cat_classified_classes = new ArrayList<ProbClass2>();
				HashMap<String,Double> cat_types_to_prob = new HashMap<String,Double>();
				
				//take classes and their probabilities from predict method (implement it in LibSVMClassifier)
				lc.predict(m_name, sa, this.models_sa.get(m_name), smoothing);
				sa_classified_classes = lc.classified_classes;
				sa_types_to_prob = lc.types_to_prob;

				lc.predict(m_name, cat, this.models_cat.get(m_name), smoothing);				
				cat_classified_classes = lc.classified_classes;
				cat_types_to_prob = lc.types_to_prob;
				
				//propagate:
				for(ProbClass2 pc2 : sa_classified_classes) {
					if (this.types.containsKey(pc2.target_class)) {
						double avg =0.0;
						if (cat_types_to_prob.containsKey(pc2.target_class))
							avg = (pc2.nprob1+cat_types_to_prob.get(pc2.target_class))/2;
						else 
							avg = pc2.nprob1;
						((ProbClass3)this.types.get(pc2.target_class)).addProb(avg);
						if (ontCache.subtypesforSupertypes.containsKey(pc2.target_class)) {
							//apply for get_subclasses(pc2.target_class)
							for(String superType : ontCache.subtypesforSupertypes.get(pc2.target_class)) {
								if (this.types.containsKey(superType)) {
									((ProbClass3)this.types.get(superType)).addProb(avg);
								}
								else {
									this.types.put(superType, new ProbClass3(avg));
								}
							}
						}
					}
					else {
						double avg =0.0;
						if (cat_types_to_prob.containsKey(pc2.target_class))
							avg = (pc2.nprob1+cat_types_to_prob.get(pc2.target_class))/2;
						else 
							avg = pc2.nprob1;
						this.types.put(pc2.target_class, new ProbClass3(avg));
						if (ontCache.subtypesforSupertypes.containsKey(pc2.target_class)) {
							//apply for get_subclasses(pc2.target_class)
							for(String superType : ontCache.subtypesforSupertypes.get(pc2.target_class)) {
								if (this.types.containsKey(superType)) {
									((ProbClass3)this.types.get(superType)).addProb(avg);
								}
								else {
									this.types.put(superType, new ProbClass3(avg));
								}
							}
						}
					}
				}								
				sa_classified_classes.clear();
				cat_classified_classes.clear();
				sa_types_to_prob.clear();
				cat_types_to_prob.clear();
			}
			//vyber the highest value - beta strategie:
			this.fin_type = this.get_type_with_maximum_average(strategy);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
	}

}
