package cz.vse.swoe.hsvm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.jibx.schema.codegen.extend.DefaultNameConverter;
import org.jibx.schema.codegen.extend.NameConverter;

import cz.vse.swoe.lucene.Indexer;
import cz.vse.swoe.lucene.Indexer.Tuple;

/**
 * Created by svabo 2015.
 */

class Counter {
	int freq = 0;
	
	public Counter() {
		this.freq=1;
	}
	
	public String toString() {
		return Integer.toString(freq);
	}
}

public class ResourceAttributesLucene {
	
	//all attributes from arff files
	protected HashMap<String, Integer> unique_att_names_cat = new HashMap<String, Integer>();
	
	protected HashMap<String, Integer> unique_att_names_abstract = new HashMap<String, Integer>();
	 
	//classes for each model
	protected HashMap<String, ArrayList<String>> classesForModelsCat = new HashMap<String, ArrayList<String>>();
	
	protected HashMap<String, ArrayList<String>> classesForModelsAbstract = new HashMap<String, ArrayList<String>>();
	
	protected String inputInstanceCat;
	
	protected String inputInstanceAbstract;
	
	protected String shortAbstractEn = "";
	
	protected String cats = "";
	
	final private File indexDir1Sa = new File("testindex1Sa");
	final private File indexDir1Cat = new File("testindex1Cat");
	final private File indexDir2 = new File("testindex2");
	public Indexer indexer;
	
	public ResourceAttributesLucene(boolean indexLucene, String unique_sa, String unique_cat) {
		try {
			//nacist unique atts for sa from uniqueAttributesSa.txt
			//nacist unique atts for cat from uniqueAttributesCat.txt
			BufferedReader input1 = new BufferedReader(new FileReader(unique_sa));
			BufferedReader input2 = new BufferedReader(new FileReader(unique_cat));
			
			while(true) {
				String line = input1.readLine();
				if(line == null) break;
				
				StringTokenizer st = new StringTokenizer(line,",");
				int i=0;
				while(st.hasMoreTokens()) {
					this.unique_att_names_abstract.put(st.nextToken().trim(), i);
					i++;
				}
				
			}
			input1.close();
			
			while(true) {
				String line = input2.readLine();
				if(line == null) break;
				
				StringTokenizer st = new StringTokenizer(line,",");
				int i=0;
				while(st.hasMoreTokens()) {
					this.unique_att_names_cat.put(st.nextToken().trim(), i);
					i++;
				}
				
			}
			input2.close();
			
			if (indexLucene) {
				FileUtils.deleteDirectory(indexDir1Sa);
				FileUtils.deleteDirectory(indexDir1Cat);
				FileUtils.deleteDirectory(indexDir2);
		        indexDir1Sa.mkdirs();
		        indexDir1Cat.mkdirs();
		        indexDir2.mkdirs();
			}
	        indexer = new Indexer(indexDir1Sa, indexDir1Cat, indexDir2);
	        if(indexLucene) {
		        InputStream is1 = new BZip2CompressorInputStream(new FileInputStream("article_categories_en.nt.bz2"), true);  
	            InputStream is2 = new BZip2CompressorInputStream(new FileInputStream("short_abstracts_en.nt.bz2"), true);
	            indexer.index1Sa(new InputStream[]{is1});
	        }
            
            indexer.initSearcher1Sa();
            indexer.initSearcher1Cat();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void get_categories_and_short_abstracts_for_resource_lucene(String resource) {
		String shortAbstractEn="",cats="";
    	//getting categories and short_abstract:
    	String categories="";
    	for (Tuple tuple : indexer.search1Sa(resource, 1000)) {
    		shortAbstractEn=tuple.object.toString();
        }
    	for (Tuple tuple : indexer.search1Cat(resource, 1000)) {
    		categories+=" "+tuple.object.toString();	
    	}
    	categories=categories.trim().replaceAll("http://dbpedia.org/resource/Category:","").replaceAll("_", " ").replaceAll("\n", " ").replaceAll("@en", "").toLowerCase().replaceAll("'","").replaceAll("\\\"", "").replaceAll(resource.replaceAll("http://dbpedia.org/resource/", "").replaceAll("[-\\+\\*/%\\(\\)]", ""), "").replaceAll("[0-9]*","");
	    //delimiters according to Weka
	    categories=categories.replaceAll(",", " ").replaceAll("\\.", " ").replaceAll(";", " ").replaceAll(":", " ").replaceAll("\\(", " ").replaceAll("\\)", " ").replaceAll("\\?", " ").replaceAll("!", " ");
	    cats="";
    	StringTokenizer st = new StringTokenizer(categories," ");
    	while (st.hasMoreTokens()) {
    		String s = st.nextToken();
    		NameConverter nc = new DefaultNameConverter();
    		cats=cats+" "+nc.depluralize(s);
    	}
    	cats=cats.trim();
	    
    	shortAbstractEn=shortAbstractEn.replaceAll("'","").replaceAll("\"", "").replaceAll(resource.replaceAll("http://dbpedia.org/resource/", "").replaceAll("[-\\+\\*/%\\(\\)]", ""), "").replaceAll("[0-9]*","").replaceAll("\n", " ").replaceAll("@en", "").toLowerCase().replaceAll("\\\"", "").replaceAll("\\\\", "");
	    shortAbstractEn=shortAbstractEn.replaceAll(",", " ").replaceAll("\\.", " ").replaceAll(";", " ").replaceAll(":", " ").replaceAll("\\(", " ").replaceAll("\\)", " ").replaceAll("\\?", " ").replaceAll("!", " ");
	    this.cats=cats;
	    this.shortAbstractEn=shortAbstractEn;
    }
	
	public String createOneBowRepresentationSa() {
		StringBuffer sb = new StringBuffer();
		//sa
		Map<Integer,Counter> atts = new TreeMap<Integer,Counter>();
		
		String[] splitted = this.shortAbstractEn.split("[ \\r\\n\\t.,;:\\\'\\\"()?!\"]");
		for(int i=0; i<splitted.length;i++) {
			String s = splitted[i];
			s = s.toLowerCase();
			if(this.unique_att_names_abstract.containsKey(s)) {
				int index = this.unique_att_names_abstract.get(s);
				if(atts.containsKey(index))
					atts.get(index).freq++;
				else
					atts.put(index, new Counter());
			}
		}
		sb.append("0");
		for(Integer i : atts.keySet()) {
			sb.append(" "+i+":"+atts.get(i).freq);
		}
		return sb.toString();
	}
	
	public String createOneBowRepresentationCat() {
		StringBuffer sb = new StringBuffer();
		//sa
		TreeMap<Integer,Counter> atts = new TreeMap<Integer,Counter>();
		String[] splitted = this.cats.split("[ \\r\\n\\t.,;:\\\'\\\"()?!\"]");
		for(int i=0; i<splitted.length;i++) {
			String s = splitted[i];
			s = s.toLowerCase();
			if(this.unique_att_names_cat.containsKey(s)) {
				int index = this.unique_att_names_cat.get(s);
				if(atts.containsKey(index))
					atts.get(index).freq++;
				else
					atts.put(index, new Counter());
			}						
		}
		sb.append("0");
		for(Integer i : atts.keySet()) {
			sb.append(" "+i+":"+atts.get(i).freq);
		}
		return sb.toString();
	}
	
	public void close() {
		try {
			indexer.close();
			indexer.closeSearcher1Sa();
			indexer.closeSearcher1Cat();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
	}

}
