# LectorPlus
**Lector** is an extraction tool originating from a joint research project between Roma Tre University and University of Alberta. The tool is able to extract facts from English Wikipedia article text, learning the expressions that are commonly used to describe instances of relations between named entities in the text. It reaches an estimated precision of 95% in its first version. 

**LectorPlus** is an extension in which we adapted the tool to extract facts for the Spanish, the Italian, the French and the German version of Wikipedia.

More information is available about the project at the Lector homepage: http://www.dia.uniroma3.it/db/lector/


## Getting Started

### Environment
To execute LectorPlus on your machine you should install:
- [Apache Maven](https://maven.apache.org/)
- Java 1.8
- command line tool:  **wget** and **git**

### Clone the project

First of all, clone the project in your local folder using:
```
git clone https://github.com/miccia4/LectorPlus.git
```

### Setting up the environment

The tool takes as input a Wikipedia (XML) (in one of the language above) and outputs an NTriples file (one for each language) with the triples that have been extracted. 

- In order to run LectorPlus on a specific version of Wikipedia (XML) dump please edit the file `dumps.properties` which contains the URLs of the input Wikipedia dump.

- In order to simplify the download of the dumps and the picking up of the other necessary files we provide a script which creates the folders and set up the file system used by LectorPlus. Run once our install script:
	```
	sh install.sh
	```
	It will take some time, at the end it will create the root folder `/data` described below.

#### Structure of `/data` folder
The `/data` folder contains a list of sub-folders that include all the necessary files:

	|-- languages: it contains the properties used by the parser
	|-- lists (en es it de fr): used by the parser to filter out undesired NE
	|		|-- currencies.tsv	
	|		|-- nationalities.tsv
	|		|-- professions.tsv
	|-- models (en): OpenNLP models that are used from the English parser.
	|		|-- en-lemmatizer.dict
	|		|-- en-pos-maxent.bin
	|		|-- en-token.bin
	|-- sources (en es it de fr): other important files used in the process
	|		|-- type
	|		|-- redirect.tsv
	|-- input (en es it de fr):
	|		|-- wikipedia: it contains the XML dump of Wikipedia
	|		|-- dbpedia: it contains the Mappingbased Objects dump of DBPedia

### Build and run

After succesfully created the `/data` folder you are ready to build a executable version of LectorPlus, using:
```
maven clean install
```

and running it using the following command:

```
sh run.sh
```

## Details and contacts
More details can be found in the paper:

>  "Accurate Fact Harvesting from Natural Language Text in Wikipedia with Lector."   
>  by Matteo Cannaviccio, Denilson Barbosa and Paolo Merialdo.   

The paper was presented at the "19th International Workshop on the Web and Databases (WebDB 2016)" 
(http://webdb2016.technion.ac.il/program.html).

If you have any questions, please feel free to contact the authors.

- Matteo Cannaviccio (cannaviccio@uniroma3.it)
- Denilson Barbosa (denilson@ualberta.ca)
- Paolo Merialdo (merialdo@uniroma3.it)


## License
This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details
