# Recipes Tutorial

## Description

This tutorial is an application that reads recipes from flat file and prints them out to the standard output.
The goal of the tutorial is to show how to write a custom record reader to read a non standard data format.

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
$>mvn exec:java -PrunRecipesTutorial
```

### From Your IDE

* Import the `easybatch-tutorials` project in your IDE
* Resolve maven dependencies
* Navigate to the `org.easybatch.tutorials.intermediate.recipes` package
* Run the `org.easybatch.tutorials.intermediate.recipes.Launcher` class without any argument
