# Job scheduling Tutorial

## Description

This tutorial is an application that schedule a batch job to run repeatedly 10 seconds.
The goal is to show how to use `easybatch-quartz` module APIs.

## Pre-requisite

* JDK 1.7+
* Maven
* Git (optional)
* Your favorite IDE (optional)

## Get source code

### Using git

`git clone https://github.com/EasyBatch/easybatch-tutorials.git`

### Downloading a zip file

Download the [zip file](https://github.com/EasyBatch/easybatch-tutorials/archive/master.zip) containing the source code and extract it.

## Run the tutorial

### From the command line

Open a terminal in the directory where you have extracted the source code of the project, then proceed as follows:

```
$>cd easybatch-tutorials
$>mvn install
$>mvn exec:java -PrunQuartzTutorial
```

If everything is ok, you will see in the console that the job will run every 10 seconds.

### From Your IDE

* Import the `easybatch-tutorials` project in your IDE
* Resolve maven dependencies
* Navigate to the `org.easybatch.tutorials.advanced.quartz` package
* Run the `org.easybatch.tutorials.advanced.quartz.Launcher` class without any argument

If everything is ok, you will see in the console that the job will run every 10 seconds.
