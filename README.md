Disclaimer
=========

This is a very simple and naïve implementation of an in memory inverted index, built initially for a technical test but maintained later for an internal course on Lucene/Solr. This is not production ready, only have been used for education purposes: to explain without to much problem how an inverted index works and some of the features desired in a full text search engine.

Features
--------
  * Naïve inverted index implementation
  * Support for stopwords removing
  * Ranking of results using simple TF-IDF formula with simple support for phrase boosting.

Build instructions
==================

To build the `jar`

`mvn package`

To execute the examples provided:

`java -cp target/index-1.0-SNAPSHOT-jar-with-dependencies.jar com.talosdigital.InvertedIndex`

A small set of tests are provided, which can bee executed using:

`mvn test`
