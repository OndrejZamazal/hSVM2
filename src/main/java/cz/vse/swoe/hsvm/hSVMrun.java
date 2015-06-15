package cz.vse.swoe.hsvm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.commons.io.FileUtils;

/**
 * Created by svabo 2015.
 */

public class hSVMrun {
	
	private int min_instances_for_class = 100;
	private String sti_types_dataset = "nl.lhd.inference.2015.nt.gz";
	private String DBpedia_ontology = "ontology.owl";
	private String lang_short_abstracts = "enwiki-20150205-short-abstracts.nt.gz";
	private String lang_article_categories = "enwiki-20150205-article-categories.nt.gz";
	private String lang_instance_types = "enwiki-20150205-instance-types.nt.gz";
	
	public hSVMrun(boolean res) {
		try {
			if (res) {
				FileUtils.deleteDirectory(new File("res"));
				new File("res/LibSVM-model").mkdirs();
				new File("res/LibSVM-unique").mkdirs();
				new File("res/arff-complete").mkdirs();
			}
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void finish() {
		try {
			FileUtils.deleteDirectory(new File("res"));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	void set_parameters(String input_file) {
		try {
			BufferedReader fp = new BufferedReader(new FileReader(input_file));
			
			while(true)
			{
				String line = fp.readLine();
				
				if(line == null) break;
				//System.out.println(line);
				
				if(line.matches("#.*")) {
					continue;
				}
				String key = line.substring(0, line.indexOf("|"));
				String value = line.substring(line.indexOf("|")+1);
				
				if(key.equals("min_instances_for_class")) this.min_instances_for_class=new Integer(value).intValue();
				if(key.equals("sti_types_dataset")) this.sti_types_dataset=value;
				if(key.equals("DBpedia_ontology")) this.DBpedia_ontology=value;
				if(key.equals("lang_short_abstracts")) this.lang_short_abstracts=value;
				if(key.equals("lang_article_categories")) this.lang_article_categories=value;
				if(key.equals("lang_instance_types")) this.lang_instance_types=value;
			}
			fp.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
			
	}
	
	public void applyhSVM() {
		//parameters:
		this.set_parameters("parameters-hSVM2.txt");
		boolean indexing = true;
		
		//workflow:
		//1. indexing phase:
		
		HierarchicalModelGeneration hmg = new HierarchicalModelGeneration(indexing, DBpedia_ontology, lang_short_abstracts, lang_article_categories, lang_instance_types);

		//2. building ontology for hierarchical SVM model:
		hmg.traverse_classes_hierarchy_start(true,min_instances_for_class);
		hmg.adjustOntology("dbpedia-excerpt.owl");
		hmg.init_ontology_cache("dbpedia-excerpt-cleaned.owl");
		//3. setting up hierarchical SVM models:
		hmg.subclassesSplitIntoSVMmodels("dbpedia-excerpt-cleaned.owl");
		//4. generating training datasets for each SVM model:
		hmg.generate_arff_files("svmModelsSubclasses.txt");
		
		//5. preprocessing content of arff files, creating unique vectors of attributes for cat and sa separately and transforming into LibSVM format: 
		hmg.filter_arff_files("res/svmModelsSubclasses.txt");
		
		//6. training SVM models using LibSVM:
		
		LibSVMClassifier lc = new LibSVMClassifier("svmModelsSubclasses.txt");
		lc.train_model();
		
		//7. running hSVM
		
		hSVM hSVM = new hSVM();

		hSVM.process_resources_all_sti2(sti_types_dataset,"target_types_number", "beta");
		
		//8. removing working directory:
		//this.finish();
	}
	
	public static void main(String[] args) throws Exception {		
		hSVMrun h = new hSVMrun(true);
		h.applyhSVM();
	}

}
