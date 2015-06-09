package org.apache.mahout.classifier.feature_selection.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Writable;
import org.apache.mahout.classifier.basic.utils.Utils;
import org.apache.mahout.keel.Algorithms.Instance_Generation.Basic.PrototypeSet;

import com.google.common.io.Closeables;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;


// Print a PrototypeSet! .

/**
 * Print a reduced set as a PrototypeSet.
 */
public class MapredOutput implements Writable, Cloneable {

  private boolean[] selectedFeatures;  // conjunto reducido.

  private int[] pesosFeatures;

  public MapredOutput() {
  }

  // constructor básico
  public MapredOutput(boolean[] selected, int [] pesos) { //, int[] predictions
    this.selectedFeatures = selected;
    this.pesosFeatures=pesos;
  }
 

  public boolean[] getSelectedFeatures() {
    return selectedFeatures;
  }
  
  public int[] getPesos() {
	    return pesosFeatures;
	  }

  

  @Override
  public void readFields(DataInput in) throws IOException {
    boolean readCorrect = in.readBoolean();
    int size=0;
    
    if (readCorrect) {
    	size=in.readInt();
    	selectedFeatures=new boolean[size];
    	for(int i=0; i<size;i++){
    		selectedFeatures[i]= in.readBoolean();

    	}
    	
    }
    
    readCorrect = in.readBoolean(); // comprobamos si hay pesos, si los hay se leen.
    if (readCorrect) {
    	pesosFeatures=new int[size];

    	for(int i=0; i<size;i++){
    		pesosFeatures[i]= in.readInt();
    	}
    }
	

  }

  @Override
  public void write(DataOutput out) throws IOException {
    out.writeBoolean(selectedFeatures != null);
    if (selectedFeatures != null) {
      out.writeInt(selectedFeatures.length);
  	
      for(int i=0; i<selectedFeatures.length;i++){
		out.writeBoolean(selectedFeatures[i]);
	  }
           
      
    }
    
    out.writeBoolean(pesosFeatures != null);

    if (pesosFeatures != null) {

	    for(int i=0; i<pesosFeatures.length;i++){
			out.writeInt(pesosFeatures[i]);
		  }
    }

  }

  @Override
  public MapredOutput clone() {
    return new MapredOutput(selectedFeatures, pesosFeatures); //, predictions
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof MapredOutput)) {
      return false;
    }

    MapredOutput mo = (MapredOutput) obj;

    return ((selectedFeatures == null && mo.getSelectedFeatures() == null) || (selectedFeatures != null && selectedFeatures.equals(mo.getSelectedFeatures()))); //&& Arrays.equals(predictions, mo.getPredictions()
  }
  
  public static boolean[] load(Configuration conf, Path rsPath, int longitud) throws IOException {
	    FileSystem fs = rsPath.getFileSystem(conf);
	    Path[] files;
	    if (fs.getFileStatus(rsPath).isDir()) {
	      files = Utils.listOutputFiles(fs, rsPath);
	    } else {
	      files = new Path[]{rsPath};
	    }

	    boolean[] features= new boolean[longitud];
	    
	    System.out.println("Size: "+longitud);
	    for (Path path : files) {
	      FSDataInputStream dataInput = new FSDataInputStream(fs.open(path));
	      try {
	      	for(int i=0; i<longitud;i++){
	      		features[i]= dataInput.readBoolean();
	        	System.out.print(features[i]+", ");

	      	}
	        
	      } finally {
	        Closeables.closeQuietly(dataInput);
	      }
	    }

	    return features;
	    
	  }

  
  /**
   * Load para cuadno se le da el fichero pesos.txt, y aquí se hace el 'reduce' a mano.
   * @param conf
   * @param rsPath
   * @param longitud
   * @return
   * @throws IOException
   */
  public static boolean[] load2(Configuration conf, Path rsPath, int longitud, int numSelectedFeatures) throws IOException {
	    FileSystem fs = rsPath.getFileSystem(conf);
	    Path[] files;
	    if (fs.getFileStatus(rsPath).isDir()) {
	      files = Utils.listOutputFiles(fs, rsPath);
	    } else {
	      files = new Path[]{rsPath};
	    }

	    boolean[] features= new boolean[longitud];
	    int [] pesos= new int[longitud];
	    
	    System.out.println("Size: "+longitud);
	    System.out.println("Num Selected: "+numSelectedFeatures);
	    for (Path path : files) {
	      FSDataInputStream dataInput = new FSDataInputStream(fs.open(path));
	      
	      try {
	      	for(int i=0; i<longitud;i++){
	      		
	      		pesos[i]=Integer.parseInt(dataInput.readLine());
	      		
	      		System.out.println(pesos[i]);
	      		
				if(pesos[i]>numSelectedFeatures) features[i]=true; 
				else features[i]=false;
				
				
	      		//features[i]= dataInput.readBoolean();
	        	System.out.print(features[i]+", ");

	      	}
	        
	      } finally {
	        Closeables.closeQuietly(dataInput);
	      }
	    }

	    return features;
	    
	  }
  

}

