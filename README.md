# LectorPlus
**Lector** is an extraction tool originated from a joint research project between Roma Tre University and University of Alberta. The tool is able to extract facts from English Wikipedia article text, learning the expressions (i.e. phrases) that are commonly used to describe instances of relations between named entities in the text. It reaches an estimated precision of 95% in its first version ([PDF](https://www.dropbox.com/s/ruoaxzelvzq5c8y/a9-cannaviccio.pdf?dl=1)):

	Matteo Cannaviccio, Denilson Barbosa and Paolo Merialdo
	"Accurate Fact Harvesting from Natural Language Text in Wikipedia with Lector."   
	In Proceedings of the 19th International Workshop on Web and Databases (WebDB '16). 
	ACM, New York, NY, USA, Article 9, 6 pages. DOI: https://doi.org/10.1145/2932194.2932203

**LectorPlus** is an extension in which the tool has been applied to different languages, other than English. It is able to extract facts for Spanish, Italian, French and German version of Wikipedia and focuses primarily on DBPedia as a reference Knowledge Graph.

## Approach
Each execution of the tool performs a first pass over the whole dump harvesting the phrases that are commonly used to describe instances of DBPedia properties in the text (e.g. `[Book] written by [Writer]` describes an instance of the property `writer`). Then, in a second pass, the tool uses the harvested phrases to extracts new instances of such properties involving named entities that were not related before.


## Getting Started

To execute LectorPlus on your machine you should have installed:
- JDK 1.8
- [Apache Maven](https://maven.apache.org/)
- command line tool:  **wget** and **git**

### Clone the project

First of all, clone the project in your local folder using:
```
git clone https://github.com/miccia4/LectorPlus.git
```

### Setting up the environment

The tool takes as input a Wikipedia XML dump and outputs several NTriples files with the triples that have been extracted.
So far, the system is released for five different languages of Wikipedia (English, Spanish, German, Italian and French) but it can be adapted for other versions as well.

- In order to run the tool on a specific Wikipedia dump, please edit the file:
	 ```
	 dumps.properties
	 ```
	it lists the specific URLs of the input dumps. We already filled it with the dumps of May 2017 in all the languages above, but other versions can be easily harvested from https://dumps.wikimedia.org/. Just copy and paste the relative URLs. 

- In order to simplify the download and the picking up of all the necessary files (e.g. the dumps above) we provide a script which creates the folders and set up the complete environment used by LectorPlus. 
	
	Run once our install script:
	```
	sh install.sh
	```
	It will take some time (many GB to downlaod!) but at the end it will create and fill the folder `/data` described below. The script can be executed many times, it will check for the presence of the files before download them again.

#### Structure of the folder `/data`
The folder `/data` contains a list of sub-folders and includes all the necessary files. The languages inside parenthesis means that the content of the folder is repeated for all those languages.

	|-- input (en es it de fr):									
	|		|-- wikipedia: it contains the initial dump of Wikipedia
	|		|-- dbpedia: it contains the Mappingbased Objects dump of DBPedia (used as a reference)
	|
	|-- languages (en es it de fr): it contains the properties of each language used by the parser
	|
	|-- lists (en es it de fr): used by the parser to filter out undesired named entities
	|		|-- currencies.tsv
	|		|-- nationalities.tsv
	|		|-- professions.tsv
	|
	|-- models (en): OpenNLP models that are used from the English parser.
	|		|-- en-lemmatizer.dict
	|		|-- en-pos-maxent.bin
	|		|-- en-token.bin
	|
	|-- sources (en es it de fr): other important files used in the process
	|		|-- type: it contains the instance types, or other dictionaries (when present)
	|		|-- redirect.tsv: it contains tsv files used to solve redirect names during the parsing
	|
	|-- spotlight (en es it de fr): contain the models to run DBpedia Spotlight for each langage
	|		|-- dbpedia-spotlight-latest.jar

Other folders are created at run-time:

	|-- index: it will contains the Lucene index of DBPedia MappingBased objects, redirects and types
	|
	|-- lector: it will contains a csv file with the phrases used by LectorPlus, for each language
	|
	|-- <output>: it will contains the NTriples output, , for each language
	
### Build and run

After created the folder `/data` you are ready to build a executable version of LectorPlus, using:
```
maven clean install
```

and running it using the following command:

```
sh run.sh <output_folder_name> <yes/no>
```
It takes the complete path of the output folder as a parameter and executes the extraction from all the Wikipedia dumps listed in `dumps.properties` file.

The second parameter, `<yes/no>` is used to choose if LectorPlus uses or not *DBpedia Spotlight* for the Entity Detection step. DBpedia Spotlight is executed locally as a different process, but its life cycle will be managed by the main LectorPlus process. 

#### Output folder
The `<output_folder_name>` is the name of the data-subfolder that will contain the the compressed NTriples files produced by the tool. The files produced are:

 - facts.bz2 = contains the facts extracted by LectorPlus
 - facts_ont.bz2 = contains the facts extracted from the first sentence of the articles (nationality, country).
 
Both the files are paired with the relative provenance file (draft).

## Details and contacts
More details can be found in the paper above and at the Lector homepage: http://www.dia.uniroma3.it/db/lector/
If you have any questions, please feel free to contact the authors.

- Matteo Cannaviccio (cannaviccio@uniroma3.it)
- Denilson Barbosa (denilson@ualberta.ca)
- Paolo Merialdo (merialdo@uniroma3.it)
