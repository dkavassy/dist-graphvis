dist-graphvis
=============

Distributed graph visualisation on top of Hadoop/Giraph

To compile

1. If Hadoop is not at /usr/local/hadoop-2.2.0, make sure you update Hadoop's path in build.xml

2. To build a JAR file use

    ant jar

The jar file is generated into lib/.

The algorithm takes CSV files (with no column headers) and preprocessed GML
and GraphML files (one edge per line and nothing else). Example inputs can be found in examples/.

Testing

	ant test

Test reports are generated into build/.