hSVMa
=====
Automation of hSVM classifier

It returns:
+ **hSVM dataset** - types of resources are DBpedia ontology classes built on hSVM algorithm
+ **Fusion dataset (selective)** - used for DBpedia 2015 release - types of resources are DBpedia ontology classes build on the a statistical type inference algorithm and hSVM algorithm (language independent)
+ **Fusion dataset** - types of resources are DBpedia ontology classes build on the a statistical type inference algorithm and hSVM algorithm (language independent)
+ **Inference dataset** - merge of LHD core and LHD fusion (selective) datasets.

## Usage binary release hSVMa:
We provide binary release hSVMa.jar.

### hSVM program

This program runs the whole process of hSVM algorithm from the beginning to hSVM dataset as output. First, it builds classification ontology. Second, it sets up SVM models according to built classification ontology. Third, it generates training datasets for each type of classifier (abstract, cat) and each SVM. Fourth, it trains the classification SVM models and finally it classifies entities. It outputs [lang].debug.hSVM and [lang].hSVM.nt.

#### Usage

  ./run-hSVM.sh &

#### Requirements

+ DBpedia Ontology (ontology.owl; available on the github)
+ stopwords.en.txt (available on the github)
+ parameters-hSVM2.txt (available on the github)
+ Datasets:
  + STI dataset, e.g. en.lhd.sti.2015.nt.gz,
  + DBpedia Short Abstracts (for the set language, e.g. enwiki-20150205-short-abstracts.nt.gz),
  + DBpedia Categories (for the set language, e.g. enwiki-20150205-article-categories.nt.gz), 
  + DBpedia Mapping-based Types (for english, e.g. enwiki-20150205-instance-types.nt.gz), 
  + DBpedia Mapping-based Types transitive (for other languages, e.g. nlwiki-20150205-instance-types-transitive.nt.gz)


#### Setting parameters

You can set up parameters via parameters-hSVM2.txt:
+ min_instances_for_class - number of minimum entities DBpedia class must have in order to be further considered (by default 100)
+ sti_types_dataset - STI dataset (e.g. en.lhd.sti.2015.nt.gz)
+ DBpedia_ontology - e.g. ontology.owl
+ lang_short_abstracts - DBpedia dataset containing short abstracts for entities (DBpedia Short Abstracts), e.g. enwiki-20150205-short-abstracts.nt.gz
+ lang_article_categories - DBpedia dataset containing categories for entities (DBpedia Categories), e.g. enwiki-20150205-article-categories.nt.gz
+ lang_instance_types - DBpedia Mapping-based Types for English (e.g. enwiki-20150205-instance-types.nt.gz), DBpedia Mapping-based Types transitive for other languages (e.g. nlwiki-20150205-instance-types-transitive.nt.gz)

### STI/hSVM selective fusion program

This program applies selective fusion algorithm (used for DBpedia 2015 release) on results of STI and hSVM. It outputs LHD fusion dataset ([lang].hSVMSTI.nt).

#### Usage

  ./run-STIhSVM-selective-fusion.sh &

#### Requirements
+ DBpedia Ontology (ontology.owl; available on the github)
+ hypoutput.log.dbpedia.manualexclusion.nt - available from LHD framework in [lang].temp.2015.tar.gz
+ parameters-hSVM2STI.txt (available on the github)
+ [lang].sti.debug - debugging output of STI is available from LHD framework in [lang].temp.2015.tar.gz
+ [lang].debug.hSVM - debugging output of hSVM is output of hSVM program
+ STI dataset, e.g. en.lhd.sti.2015.nt.gz
+ sti_debug_stiUnsure - debugging information for selective fusion, e.g. en.sti.debug_stiUnsure_0.15.csv (available on the github)

#### Setting parameters

You can set up parameters via parameters-hSVM2STI.txt:
+ hypoutput_log_dbpedia_manualexclusionen, e.g. hypoutput.log.dbpedia.manualexclusion.nt
+ sti_debug, e.g. en.sti.debug
+ debug_hSVM, e.g. en.debug.hSVM
+ DBpedia_ontology, e.g. ontology.owl
+ sti_types_dataset, e.g. en.lhd.sti.2015.nt.gz
+ sti_debug_stiUnsure, e.g. en.sti.debug_stiUnsure_0.15.csv

### STI/hSVM fusion program

This program applies fusion algorithm on results of STI and hSVM. It outputs LHD fusion dataset (res/xx.hSVMSTI.nt).

#### Usage

  ./run-STIhSVM-fusion.sh &

#### Requirements
+ DBpedia Ontology (ontology.owl; available on the github)
+ hypoutput.log.dbpedia.manualexclusion.nt - available from LHD framework in [lang].temp.2015.tar.gz
+ parameters-hSVM2STI.txt (available on the github)
+ [lang].sti.debug - debugging output of STI is available from LHD framework in [lang].temp.2015.tar.gz
+ [lang].debug.hSVM - debugging output of hSVM is output of hSVM program
+ STI dataset, e.g. en.lhd.sti.2015.nt.gz

#### Setting parameters

You can set up parameters via parameters-hSVM2STI.txt:
+ hypoutput_log_dbpedia_manualexclusionen, e.g. hypoutput.log.dbpedia.manualexclusion.nt
+ sti_debug, e.g. en.sti.debug
+ debug_hSVM, e.g. en.debug.hSVM
+ DBpedia_ontology, e.g. ontology.owl
+ sti_types_dataset, e.g. en.lhd.sti.2015.nt.gz

### Merge of LHD core and LHD fusion datasets program
This program applies merge of LHD core and LHD fusion datasets. It outputs LHD inference dataset ([lang].lhd.inference.nt).

#### Usage

  ./run-core-fusion-merge.sh &

#### Requirements
+ LHD core dataset, e.g. en.lhd.core.2015.nt.gz
+ LHD fusion dataset, e.g. en.lhd.fusion.2015.nt.gz

#### Setting parameters

You can set up parameters via parameters-hSVM2STI.txt:
+ lhd_core, e.g. en.lhd.core.2015.nt.gz
+ lhd_fusion, e.g. en.lhd.fusion.2015.nt.gz