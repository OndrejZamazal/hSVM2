package cz.vse.swoe.hsvm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.jena.riot.RDFDataMgr;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;

public class CorehSVMSTIrun {
	
	public String lhd_core = "en.lhd.core.2015.nt.gz";
	public String lhd_fusion = "en.lhd.inference.2015.nt.gz";	
	
	public CorehSVMSTIrun(boolean res) {
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
	
	public void mergeCoreAndhSVMSTI(String core, String hSVMSTI) {
		try {
			PrintWriter toFile = new PrintWriter(new FileWriter("en.lhd.instance.types.nt", false));
			
			//take core, it is preferable
			Model model = ModelFactory.createDefaultModel();
			model = RDFDataMgr.loadModel(core) ;
			HashSet<String> resource_coretype_mapping = new HashSet<String>();
			
			for (Iterator<Statement> it = model.listStatements(); it.hasNext(); ) {
	            final Statement stmt = it.next();
	            String res=stmt.getSubject().getURI();			
				//resource_coretype_mapping.add(res);				
	            resource_coretype_mapping.add(StringEscapeUtils.unescapeEcmaScript(res));
	            if(core.matches("^en\\..*")) {
	            	String localname = res.replaceAll(".*dbpedia.org/resource/", "");
	            	toFile.println("<http://dbpedia.org/resource/"+StringEscapeUtils.escapeEcmaScript(localname).replaceAll("\\\\'", "'").replaceAll("\\\\/", "/")+"> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <"+stmt.getObject()+"> .");
	            }
	            else if(core.matches("^de\\..*")) {
	            	String localname = res.replaceAll(".*dbpedia.org/resource/", "");
	            	toFile.println("<http://de.dbpedia.org/resource/"+StringEscapeUtils.escapeEcmaScript(localname).replaceAll("\\\\'", "'").replaceAll("\\\\/", "/")+"> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <"+stmt.getObject()+"> .");
	            }
	            else if(core.matches("^nl\\..*")) {
	            	String localname = res.replaceAll(".*dbpedia.org/resource/", "");
	            	toFile.println("<http://nl.dbpedia.org/resource/"+StringEscapeUtils.escapeEcmaScript(localname).replaceAll("\\\\'", "'").replaceAll("\\\\/", "/")+"> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <"+stmt.getObject()+"> .");
	            }
				/*
	            URI uri = URI.create(StringEscapeUtils.unescapeEcmaScript(res));
				String localname = uri.getPath().replaceAll("/resource/", "");
				if(uri.getHost().equals("dbpedia.org")) {
					toFile.println("<http://dbpedia.org/resource/"+StringEscapeUtils.escapeEcmaScript(localname).replaceAll("\\\\'", "'")+"> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <"+stmt.getObject()+"> .");
				}
				else if(uri.getHost().equals("de.dbpedia.org")) {
					toFile.println("<http://de.dbpedia.org/resource/"+StringEscapeUtils.escapeEcmaScript(localname).replaceAll("\\\\'", "'")+"> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <"+stmt.getObject()+"> .");
				}
				else if(uri.getHost().equals("nl.dbpedia.org")) {
					toFile.println("<http://nl.dbpedia.org/resource/"+StringEscapeUtils.escapeEcmaScript(localname).replaceAll("\\\\'", "'")+"> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <"+stmt.getObject()+"> .");
				}*/
				//toFile.println("<"+stmt.getSubject().getNameSpace()+StringEscapeUtils.escapeEcmaScript(stmt.getSubject().getLocalName()).replaceAll("\\\\'", "'")+"> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <"+stmt.getObject()+"> .");
	            //toFile.println("<"+stmt.getSubject().getNameSpace()+StringEscapeUtils.escapeEcmaScript(stmt.getSubject().getLocalName()).replaceAll("\\\\'", "'")+"> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <"+stmt.getObject()+"> .");
				//System.out.println(stmt.getSubject()+"$"+stmt.getSubject().getNameSpace()+"$"+stmt.getSubject().getLocalName()+"$"+stmt.getSubject().getLocalName().replaceAll("\\\\'", "'"));
				toFile.flush();
			}
			model.close();
			
			
			//take hSVM/STI fusion dataset, only if we do not have type from core
			Model model1 = ModelFactory.createDefaultModel();
			model1 = RDFDataMgr.loadModel(hSVMSTI) ;			
			
			for (Iterator<Statement> it = model1.listStatements(); it.hasNext(); ) {
	            final Statement stmt = it.next();
	            String res=stmt.getSubject().getURI();
	            if(!resource_coretype_mapping.contains(StringEscapeUtils.unescapeEcmaScript(res))) {	            					
	            	//toFile.println(stmt);
	            	//toFile.println("<"+res+"> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <"+stmt.getObject()+"> .");
	            	if(core.matches("^en\\..*")) {
		            	String localname = res.replaceAll(".*dbpedia.org/resource/", "");
		            	toFile.println("<http://dbpedia.org/resource/"+StringEscapeUtils.escapeEcmaScript(localname).replaceAll("\\\\'", "'").replaceAll("\\\\/", "/")+"> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <"+stmt.getObject()+"> .");
		            }
		            else if(core.matches("^de\\..*")) {
		            	String localname = res.replaceAll(".*dbpedia.org/resource/", "");
		            	toFile.println("<http://de.dbpedia.org/resource/"+StringEscapeUtils.escapeEcmaScript(localname).replaceAll("\\\\'", "'").replaceAll("\\\\/", "/")+"> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <"+stmt.getObject()+"> .");
		            }
		            else if(core.matches("^nl\\..*")) {
		            	String localname = res.replaceAll(".*dbpedia.org/resource/", "");
		            	toFile.println("<http://nl.dbpedia.org/resource/"+StringEscapeUtils.escapeEcmaScript(localname).replaceAll("\\\\'", "'").replaceAll("\\\\/", "/")+"> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <"+stmt.getObject()+"> .");
		            }
	            	/*
	            	URI uri = URI.create(StringEscapeUtils.unescapeEcmaScript(res));
					String localname = uri.getPath().replaceAll("/resource/", "");
					if(uri.getHost().equals("dbpedia.org")) {
						toFile.println("<http://dbpedia.org/resource/"+StringEscapeUtils.escapeEcmaScript(localname).replaceAll("\\\\'", "'")+"> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <"+stmt.getObject()+"> .");
					}
					else if(uri.getHost().equals("de.dbpedia.org")) {
						toFile.println("<http://de.dbpedia.org/resource/"+StringEscapeUtils.escapeEcmaScript(localname).replaceAll("\\\\'", "'")+"> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <"+stmt.getObject()+"> .");
					}
					else if(uri.getHost().equals("nl.dbpedia.org")) {
						toFile.println("<http://nl.dbpedia.org/resource/"+StringEscapeUtils.escapeEcmaScript(localname).replaceAll("\\\\'", "'")+"> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <"+stmt.getObject()+"> .");
					}*/
	            	//toFile.println("<"+stmt.getSubject().getNameSpace()+StringEscapeUtils.escapeEcmaScript(stmt.getSubject().getLocalName()).replaceAll("\\\\'", "'")+"> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <"+stmt.getObject()+"> .");
	            	toFile.flush();
	            }
			}
			model1.close();
			
			toFile.close();
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
				
				if(key.equals("lhd_core")) this.lhd_core=value;
				if(key.equals("lhd_fusion")) this.lhd_fusion=value;
			}
			fp.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
			
	}
	
	public static void main(String[] args) {
		CorehSVMSTIrun h = new CorehSVMSTIrun(false);
		h.set_parameters("parameters-hSVM2STI.txt");
		//h.mergeCoreAndhSVMSTI("en.lhd.core.2015.nt.gz", "en.lhd.inference.2015.nt.gz");
		//h.mergeCoreAndhSVMSTI("de.lhd.core.2015.nt.gz", "de.lhd.inference.2015.nt.gz");
		h.mergeCoreAndhSVMSTI("nl.lhd.core.2015.nt.gz", "nl.lhd.inference.2015.nt.gz");
	}

}
