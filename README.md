MR-EFS
====

This repository includes the MapReduce implementations used in [1].
This implementation is based on Apache Mahout 0.8 library. The Apache Mahout (http://mahout.apache.org/) project's goal is to build an environment for quickly creating scalable performant machine learning applications.

Prerequisites:
- Hadoop 2.5.
- ant

Associated paper:

- D. Peralta, S. Del Río, S. Ramírez-Gallego, I. Triguero, J.M. Benítez, F. Herrera. Evolutionary Feature Selection for Big Data Classification: A MapReduce Approach.


Compile the whole project with ANT:
<pre>
$ ant
</pre>
Put the dataset folder into the HDFS system:
<pre>
hadoop fs -put datasets/
</pre>
Generate descriptor file needed by the mahout code. (Check: ...classifier.df.tools.Describe.java).
<pre>
$ hadoop jar Model.jar org.apache.mahout.classifier.df.tools.Describe -p  datasets/page-blocks-10-fold/page-blocks-10-1tra.data  -f  datasets/page-blocks-10-fold/page-blocks.info -d  10 N L
</pre>


<pre>
hadoop jar Model.jar org.apache.mahout.classifier.feature_selection.mapreduce.FeatureSelectionModel -h

Usage:                                                                          
 [--data <path> --dataset <dataset> --header <header> --output <path>]          
Options                                                                         
  --data (-d) path           Data path                                          
  --dataset (-ds) dataset    The path of the file descriptor of the dataset     
  --header (-he) header      Header of the dataset in Keel format               
  --output (-o) path         Output path, will contain the set of selected      
                             features  
</pre>

Example of use:

To compute the number of mappers, we have to check the number of bytes of the training file:
<pre>
$ ls -l datasets/page-blocks-10-fold/page-blocks-10-1tra.data 
 -rw-rw-r-- 1 isaac isaac 221580 jul 15  2013 datasets/page-blocks-10-fold/page-blocks-10-1tra.data 
</pre>

If we want to have 4 maps, we should divide this number by 4 (55395).


<pre>
hadoop jar Model.jar org.apache.mahout.classifier.feature_selection.mapreduce.FeatureSelectionModel -Dmapred.max.split.size=55395 -d  -d datasets/page-blocks-5-fold/page-blocks-5-1tra.data  -he datasets/page-blocks-5-fold/page-blocks.header  -ds datasets/page-blocks-5-fold/page-blocks.info  -o output-FS-pageblocks
</pre>


Build the preprocessed dataset for classification purposes:

<pre>
hadoop jar Model.jar org.apache.mahout.classifier.feature_selection.mapreduce.FSconstructor -i datasets/page-blocks-5-fold/page-blocks-5-1tra.data -fs output-FS-pageblocks/seleccionadas.txt -ds datasets/page-blocks-5-fold/page-blocks.info -he datasets/page-blocks-5-fold/page-blocks.header -o output-FSconstructor
</pre>





