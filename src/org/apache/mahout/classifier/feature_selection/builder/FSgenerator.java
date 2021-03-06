package org.apache.mahout.classifier.feature_selection.builder;

import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.mahout.classifier.basic.data.Data;
import org.apache.mahout.classifier.basic.data.Dataset;
import org.apache.mahout.classifier.feature_selection.utils.PGUtils;
import org.apache.mahout.classifier.feature_selection.*;
import org.apache.mahout.keel.Algorithms.Instance_Generation.Basic.PrototypeSet;
import org.apache.mahout.keel.Algorithms.Preprocess.Feature_Selection.evolutionary_algorithms.CHC.wrapper.CHCBinaryLVO;
import org.apache.mahout.keel.Algorithms.Preprocess.Feature_Selection.nonevolutionary_algorithms.FOCUS.FocusIncon;
import org.apache.mahout.keel.Algorithms.Preprocess.Feature_Selection.nonevolutionary_algorithms.LVF.LVFIncon;
import org.apache.mahout.keel.Algorithms.Preprocess.Feature_Selection.nonevolutionary_algorithms.LVW.LVWLVO;
import org.apache.mahout.keel.Algorithms.Preprocess.Feature_Selection.nonevolutionary_algorithms.RELIEF_F.Relieff;
import org.apache.mahout.keel.Dataset.Attributes;
import org.apache.mahout.keel.Dataset.InstanceAttributes;
import org.apache.mahout.keel.Dataset.InstanceSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FSgenerator  {
  
  private static final Logger log = LoggerFactory.getLogger(FSgenerator.class);	
  int nClasses, nLabels;

  public String FSmethod = "CHC";
  
  public String header;
 // LVFIncon lvf;
 // Relieff relief;
 // FocusIncon focus;
  
  CHCBinaryLVO chc;
  LVWLVO lvwlov;
  
	//  strata[i].print();
	  
  public FSgenerator() {
  }
  
  public FSgenerator(String alg)
  {
	  this.FSmethod = alg;
  }
  
  public void setNLabels(int nLabels) {
    this.nLabels = nLabels;
  }


  public void setHeader(String header){
	  this.header=header;
  }
  public void build(Data data, Context context) throws Exception {
    //We do here the algorithm's operations

	Dataset dataset = data.getDataset();
	log.info("FS: data size = "+data.size());

	nClasses = dataset.nblabels();
	
	//Gets the number of input attributes of the data-set
	int nInputs = dataset.nbAttributes() - 1;
	
	//It returns the class labels
	String clases[] = dataset.labels();
	

	context.progress();
	
	PrototypeSet train = new PrototypeSet(data,context);
	
	
	// CHC L.O.O
	// chc = new CHCBinaryLVO(train.toInstanceSet(),1, 123456789,0.05,0.5,200,20,context);

	// CHC Validacion Cruzada:
	PrototypeSet validation = new PrototypeSet();
	PrototypeSet Clases [] = new PrototypeSet[nClasses];

	  
	for(int i=0; i<nClasses;i++){ 
		  Clases[i] = new PrototypeSet(train.getFromClass(i).clone());
		  
		  System.out.println("Clases["+i+"]: "+Clases[i].size());
		  if(Clases[i].size()>0){ // tiene que haber prototipos de la clase.
			  int numberOfPrototypes = (int)Math.round(Clases[i].size()*0.10);// 10%
			  if(numberOfPrototypes < 1){numberOfPrototypes=1;}
			  
			  for(int j=0; j< numberOfPrototypes; j++){
				  validation.add(Clases[i].getRandom());
			  }
		  }else{
			  System.err.println("No hay prototipos de la clase: "+ i);
		  }
		  
		  
		  
	}
	validation.randomize();
	
	//validation.print();
	
	 chc = new CHCBinaryLVO(train.toInstanceSet(),validation.toInstanceSet(),1, 123456789,0.05,0.50,1000,40,context, header);

	 
	
	//focus = new FocusIncon(train.toInstanceSet(),1, 0.2);

	// relief = new Relieff(train.toInstanceSet(),1, 123456789,0.2,100);
	
	//lvf = new LVFIncon (train.toInstanceSet(),  1, 123456789, 0, 770);


	//lvwlov =new LVWLVO(train.toInstanceSet(),  1, 123456789, 0, 1000,context);
}



  public boolean[] applyFS() {
 
	// return	focus.getFeatures();
	  return	chc.getFeatures();
	 // return lvwlov.getFeatures();
	  
  }


}
