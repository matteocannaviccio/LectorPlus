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

The tool takes as input a Wikipedia XML dump (in one of the language above) and outputs several NTriples files with the triples that have been extracted. 

- In order to run the tool on specific versions of Wikipedia please edit the file:
	 ```
	 dumps.properties
	 ```
	it lists the specific URLs of the input Wikipedia dumps. We already filled it with the complete dumps of May 2017 in all the languages above but other versions can be easily linked from https://dumps.wikimedia.org/.

- Also, in order to simplify the download of those dumps and the picking up of the other necessary files we provide a script which creates the folders and set up the environment used by LectorPlus. 
	
	Run once our install script:
	```
	sh install.sh
	```
	It will take some time<sup>*</sup> (many GB to downlaod) but at the end it will create the root folder `/data` described below.
	<sup>*</sup>: Note that the English Wikipedia dump only is ~14GB! 

#### Structure of the folder `/data`
The folder `/data` contains a list of sub-folders and includes all the necessary files. The languages inside parenthesis means that the content of the folder is repeated for all of them.

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
	
### Build and run

After created the folder `/data` you are ready to build a executable version of LectorPlus, using:
```
maven clean install
```

and running it using the following command:

```
sh run.sh <output_folder_complete_path>
```
It takes the complete path of the output folder as a parameter and executes the extraction from all the Wikipedia dumps listed in `dumps.properties` file.
The output folder will contain all the compressed NTriples files produced by the tool.


## Details and contacts
More details can be found in the paper above and at the Lector homepage: http://www.dia.uniroma3.it/db/lector/
If you have any questions, please feel free to contact the authors.

- Matteo Cannaviccio (cannaviccio@uniroma3.it)
- Denilson Barbosa (denilson@ualberta.ca)
- Paolo Merialdo (merialdo@uniroma3.it)
