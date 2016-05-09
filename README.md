dist-graphvis
=============

Distributed graph visualisation tool.

It implements a distributed version of the Fruchterman-Reingold force-directed layout algorithm using Giraph. It was a small group research project for British Telecommunications plc done at the University of York.

Usage
-----

If Hadoop is not at /usr/local/hadoop-2.2.0, make sure you update Hadoop's path in build.xml

To build a JAR file use

	ant jar

The jar file is generated in lib/

The algorithm takes CSV files (with no column headers) and preprocessed GML
and GraphML files (one edge per line and nothing else). Example inputs can be found in examples/

Run as a Giraph job, like this

	export HADOOP_CLASSPATH=$HADOOP_CLASSPATH:./dist-graphvis.jar:./giraph-1.1.0-SNAPSHOT-for-hadoop-2.2.0-jar-with-dependencies.jar
	/usr/local/hadoop/bin/hadoop \
	org.apache.giraph.GiraphRunner \
	graphvis.engine.FruchtermanReingoldGraphVis \
	-eif graphvis.io.CSVEdgeInputFormat \
	-eip example.csv \
	-vof graphvis.io.SVGVertexOutputFormat \
	-op output/graphvis \
	-mc graphvis.engine.GraphvisMasterCompute \
	-w 1 \
	-yj giraph-1.1.0-SNAPSHOT-for-hadoop-2.2.0-jar-with-dependencies.jar,dist-graphvis.jar

Note that the jar files are in the same directory as the shell script. Giraph seems to need it.

Testing
-------

	ant test

Test reports are generated in build/
