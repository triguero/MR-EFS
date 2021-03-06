package org.apache.mahout.classifier.feature_selection.mapreduce;

import com.google.common.base.Preconditions;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.mahout.classifier.feature_selection.builder.FSgenerator;
import org.apache.mahout.classifier.basic.data.Dataset;
import org.apache.mahout.classifier.feature_selection.mapreduce.partial.StrataID;
import org.apache.mahout.keel.Algorithms.Instance_Generation.Basic.Prototype;
import org.apache.mahout.keel.Algorithms.Instance_Generation.Basic.PrototypeSet;
import org.apache.mahout.keel.Algorithms.Instance_Generation.utilities.KNN.KNN;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * This Mapred allows to run more than one reducers.
 * 
 */
public class MajorityIterativeReducer<KEYIN,VALUEIN,KEYOUT,VALUEOUT> extends Reducer<KEYIN,VALUEIN,KEYOUT,VALUEOUT> {
  
  private boolean noOutput;
  
  protected FSgenerator fs_algorithm;
  
  private Dataset dataset;
  protected String header;

  protected int []Majority;
  protected int mappers=0;
  protected int strata;
  private int firstId = 0;

  
  /**
   * 
   * @return if false, the mapper does not estimate and output predictions
   */
  protected boolean isNoOutput() {
    return noOutput;
  }
  
  protected FSgenerator getFSgeneratorBuilder() {
    return fs_algorithm;
  }
  
  protected Dataset getDataset() {
    return dataset;
  }
  
  @Override
  protected void setup(Context context) throws IOException, InterruptedException {
    super.setup(context);
    
    Configuration conf = context.getConfiguration();
    
    configure(!Builder.isOutput(conf), Builder.getFSgeneratorBuilder(conf), Builder.loadDataset(conf), Builder.getHeader(conf));
  }
  
  /**
   * Useful for testing
   */
  protected void configure(boolean noOutput, FSgenerator fs_algorithm, Dataset dataset, String header) {
    Preconditions.checkArgument(fs_algorithm != null, "FSgenerator not found in the Job parameters");
    this.noOutput = noOutput;
    this.fs_algorithm = fs_algorithm;
    this.dataset = dataset;
    this.header = header;
  }

  
	  /**
	   * Generic reducer, it only adds all the RSs.
	   */
  
	protected void reduce(KEYIN id, Iterable<VALUEIN> rs, Context context)
			throws IOException, InterruptedException {
		// TODO Apéndice de método generado automáticamente
	
		//System.out.println("Si paso por aquí: "+id);
		//strata = (StrataID) id;

		for(VALUEIN value: rs){
			MapredOutput prueba = (MapredOutput) value;
			boolean [] seleccionadas= prueba.getSelectedFeatures();
			mappers++;
			
			if(Majority==null){
				Majority= new int[seleccionadas.length];
				Arrays.fill(Majority,0);
			}
			int selecs=0;
			for(int i=0; i<seleccionadas.length;i++){
				if(seleccionadas[i]){
					Majority[i]++;
					selecs++;
				}
			}
			
			System.out.println("Seleccionadas en mapper "+id+": "+selecs);
			context.progress();
	

		}



	
	}


	 protected void cleanup(Context context) throws IOException, InterruptedException {
		 
		    System.out.println("escribo la selección final.");
		    StrataID key = new StrataID();

		    key.set(strata, firstId + 1);
		    
		    boolean [] finalSelection = new boolean[Majority.length];
		    
		    
            FileOutputStream f = new FileOutputStream("pesos.txt");
            DataOutputStream fis = new DataOutputStream((OutputStream) f);
          //  System.out.println("Cadena="+cadena);
            
			for(int i=0; i<Majority.length;i++){
				if(Majority[i]>(mappers*0.75)) finalSelection[i]=true; //3/4
				else finalSelection[i]=false;
				
				// imprimir también los pesos.
				fis.writeBytes(Majority[i]+"\n");
	            
	            	
			}
			fis.close();
			
			
			MapredOutput salida= new MapredOutput(finalSelection,Majority);
			context.write((KEYOUT) key, (VALUEOUT) salida);
	 }
}


