<center><img src="https://rawgit.com/termmerge/nlpcore/master/assets/logo.svg" width="200"></center>

[![Build Status](https://travis-ci.org/termmerge/nlpcore.svg?branch=master)](https://travis-ci.org/termmerge/nlpcore)

# TermMerge (NLPCore Service)
Core Kernel Server that orchestrates all the low-level, fault-tolerant and highly-intensive data crunching work needed 
for running Natural Language Processing tasks, including queries about **word convergences**. From famous NLP tasks like 
POS and NER tagging to custom, homegrown implementations like Word Convergences and Semantic Tagging, to anything in 
between, NLPCore does it and does it with strong scalability and redundancy.

Service consumers can:
* Hook up to TermMerge's NLPCore Service using **WebSockets** to get live analytics on reported word convergences
* Query for word convergences based on given properties like **convergence radius** (aka cloud of words that are 
_ steps correlated away with another word)
* Issue out heavy computation-based work and consume the results in either a big dump using HTTP or stream using 
WebSockets

## Dependencies
* [Apache Zookeeper](http://zookeeper.apache.org) - abstraction for orchestrating distributed tasks (regardless of whether those tasks are partitioned across processes, servers or even networks)
* [Apache Curator](http://curator.apache.org) - abstraction over Apache Zookeeper for making cluster orchestration tasks like leader election, distributed locks and group membership very trivial
* [Apache Kafka](https://kafka.apache.org) - distributed and exposed message platform that is alot like a first-in, first-out transaction log
* [Apache TinkerPop](http://tinkerpop.apache.org) - eases graph-based computation that runs across supported graph-based querying engines and databases like Neo4J.

## Network Architecture
<center><img src="https://rawgit.com/termmerge/nlpcore/master/assets/network_architecture.svg"></center>

* **Interface Group** nodes can: 
  * Accept and give out both HTTP and Websocket requests/responses
  * Issue out QuorumMessage Requests 

* **Compute Group** nodes turn into simple computation nodes that can run multiple tasks including:
  * Continously poll Kafka and retrieve usage analytics about streamed word convergences
  * Do graph-based computation on word convergences
  * Use core Natural Language Processing tasks like tokenization, word splitting, part of speech tagging, lemmatization, named entity recognition, constituency parsing, dependency parsing, coreference resolution and many more! These tasks are delegated to the [Stanford CoreNLP library](http://stanfordnlp.github.io/CoreNLP/) under the hood.
  * Access stored [WordNet](https://wordnet.princeton.edu) and [FrameNet](https://framenet.icsi.berkeley.edu) models

Communication between the interface group and compute group are currently implemented by using Apache Kafka as the communication medium. This is because we are already using Apache Kafka for storing reported word convergences. Apache Kafka allows us to provide a buffering medium in case requests come in quicker than we can serve them especially considering that NLP tasks tend to be very intensive.
