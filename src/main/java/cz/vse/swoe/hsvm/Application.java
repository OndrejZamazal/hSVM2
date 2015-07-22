package cz.vse.swoe.hsvm;

import cz.vse.swoe.hsvm.hSVMrun;

/**
 * Created by svabo 2015.
 */

public class Application {

	public static void main(String[] args) {
		if (args.length==0) {
			System.out.println("Please provide number for progam as argument you want to run: \n 1" +
		" for hSVM (output: hSVM dataset); \n 2 for STI/hSVM fusion (output: LHD fusion dataset); " +
		"\n 3 for STI/hSVM selective fusion (output: LHD fusion dataset used for DBpedia 2015); "+
		" \n 4 for merging LHD core and LHD fusion datasets (output: LHD inference dataset).");
		}
		else if (args[0].equals("1")) { //hSVMrun
			hSVMrun h = new hSVMrun(true);
			h.applyhSVM();
		}
		else if (args[0].equals("2")) { //hSVMSTIrun
			hSVMSTIrun h = new hSVMSTIrun(false);
			h.set_parameters("parameters-hSVM2STI.txt");
			h.mergehSVMSTI(h.hypoutput_log_dbpedia_manualexclusion, h.sti_debug, h.debug_hSVM, h.DBpedia_ontology);
		}
		else if (args[0].equals("3")) { //hSVMSTIrun - mergehSVMSTI_unsure_sti
			hSVMSTIrun h = new hSVMSTIrun(false);
			h.set_parameters("parameters-hSVM2STI.txt");
			h.mergehSVMSTI_unsure_sti(h.hypoutput_log_dbpedia_manualexclusion, h.sti_debug, h.debug_hSVM, h.DBpedia_ontology, h.sti_types_dataset, h.sti_debug_stiUnsure);
		}
		else if (args[0].equals("4")) { //hSVMSTIrun - mergehSVMSTI_unsure_sti
			CorehSVMSTIrun h = new CorehSVMSTIrun(false);
			h.set_parameters("parameters-hSVM2STI.txt");
			h.mergeCoreAndhSVMSTI(h.lhd_core, h.lhd_fusion);
		}
		else {
			System.out.println("Please provide number for program as argument you want to run: \n 1" +
		" for hSVM (output: hSVM dataset); \n 2 for STI/hSVM fusion (output: LHD fusion dataset); " +
		"\n 3 for STI/hSVM selective fusion (output: LHD fusion dataset used for DBpedia 2015); "+
		" \n 4 for merging LHD core and LHD fusion datasets (output: LHD inference dataset).");
		}
	}

}
