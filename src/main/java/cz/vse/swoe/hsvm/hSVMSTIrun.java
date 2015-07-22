package cz.vse.swoe.hsvm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.apache.commons.io.FileUtils;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import cz.vse.swoe.hsvm.hSVMSTI;

/**
 * Created by svabo 2015.
 */

public class hSVMSTIrun {
	
	public String hypoutput_log_dbpedia_manualexclusion = "en.hypoutput.log.dbpedia.manualexclusion.nt";
	public String sti_debug = "en.sti.debug";
	public String debug_hSVM = "en.debug.hSVM";
	public String DBpedia_ontology = "ontology.owl";
	public String sti_types_dataset = "en.lhd.inference.2015.nt.gz";
	public String sti_debug_stiUnsure = "en.sti.debug_stiUnsure_0.15.csv";
	
	public hSVMSTIrun(boolean res) {
		try {
			if (res) {
				//FileUtils.deleteDirectory(new File("res"));
				new File("res").mkdir();
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
	
	public void init_ontology_cache(String ontology) {
		try {
			PrintWriter toFile;
			toFile = new PrintWriter(new FileWriter("res/SubClassesMLClassifier.txt"));
			
			OntModel ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF);			
			ont.read(ontology, "RDF/XML" );
			for (OntClass cls : ont.listClasses().toList()) {
				//System.out.println(cls);
				int n = cls.listSubClasses(false).toList().size();
				//System.out.println(n);
				if(n>0) {
					toFile.print(cls.getLocalName()+" ");
					StringBuilder sb = new StringBuilder();
					for(OntClass cls1 : cls.listSubClasses(false).toList()) {
						sb.append(cls1.getLocalName()+" ");
					}
					toFile.println(sb.toString().trim());
				}
				toFile.flush();
			}
			toFile.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void mergehSVMSTI(String lhd_types, String debug_sti_input, String debug_hSVM_input, String ontology) {
		//parameters goes arguments of mergehSVMSTI method 		
		//String lhd_types, String debug_sti_input, String debug_hSVM_input, String ontology
		//workflow:
		//1. preprocess DBpedia ontology
		this.init_ontology_cache(ontology);
		//2. merge hSVM input with STI input
		hSVMSTI h = new hSVMSTI(lhd_types, debug_sti_input);
		h.fuse(debug_hSVM_input);
		//3. removing working directory:
		//this.finish();
	}
	
	//11-06-15, merge only if sti is unsure 
	public void mergehSVMSTI_unsure_sti(String lhd_types, String debug_sti_input, String debug_hSVM_input, String ontology, String mapping_to_sti_file, String whitelist_file) {
		//parameters goes arguments of mergehSVMSTI method 		
		//String lhd_types, String debug_sti_input, String debug_hSVM_input, String ontology
		//workflow:
		//1. preprocess DBpedia ontology
		this.init_ontology_cache(ontology);
		//2. merge hSVM input with STI input
		hSVMSTI h = new hSVMSTI(lhd_types, debug_sti_input);
		//h.fuse_unsure_sti(debug_hSVM_input, mapping_to_sti_file, whitelist_file);
		h.fuse_unsure_sti2(debug_hSVM_input, mapping_to_sti_file, whitelist_file);
		//3. removing working directory:
		//this.finish();
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
				
				if(key.equals("hypoutput_log_dbpedia_manualexclusion")) this.hypoutput_log_dbpedia_manualexclusion=value;
				if(key.equals("sti_debug")) this.sti_debug=value;
				if(key.equals("debug_hSVM")) this.debug_hSVM=value;
				if(key.equals("DBpedia_ontology")) this.DBpedia_ontology=value;
				if(key.equals("sti_types_dataset")) this.sti_types_dataset=value;
				if(key.equals("sti_debug_stiUnsure")) this.sti_debug_stiUnsure=value;
			}
			fp.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
			
	}
	
	public static void main(String[] args) {
		hSVMSTIrun h = new hSVMSTIrun(false);
		h.set_parameters("parameters-hSVM2STI.txt");
		h.mergehSVMSTI_unsure_sti(h.hypoutput_log_dbpedia_manualexclusion, h.sti_debug, h.debug_hSVM, h.DBpedia_ontology, h.sti_types_dataset, h.sti_debug_stiUnsure);
		//h.mergehSVMSTI(h.hypoutput_log_dbpedia_manualexclusion, h.sti_debug, h.debug_hSVM, h.DBpedia_ontology);
	}

}
