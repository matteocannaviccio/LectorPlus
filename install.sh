#!/bin/bash

export LANGUAGES=(en es it de fr)

# download the project
echo "Downalod LectorPlus project ..."
git clone https://matteo4@bitbucket.org/matteo4/lectorplus.git

# install the project
echo "Install dependencies ..." 
cd lectorplus
mvn clean install

# create folders
mkdir data/input/wikipedia
mkdir data/input/dbpedia
mkdir data/input/lists
mkdir data/input/models
mkdir data/input/sources

for ((i=0;i<${#LANGUAGES[@]};++i)); do

	LANGUAGE=${LANGUAGES[i]}

	###############    Download Wikipedia   #####################
	echo "Downloading wikipedia dump in ${LANGUAGE}..."
	if [ ${LANGUAGE} = "en" ]; then
		wget "https://dumps.wikimedia.org/enwiki/20170220/enwiki-20170220-pages-articles1.xml-p000000010p000030302.bz2" > dump.xml.bz2
	fi
	if [ ${LANGUAGE} = "es" ]; then
    	wget "https://dumps.wikimedia.org/eswiki/20170220/eswiki-20170220-pages-articles1.xml-p000000005p000229076.bz2" > dump.xml.bz2
    fi 
 	if [ ${LANGUAGE} = "it" ]; then
		wget "https://dumps.wikimedia.org/itwiki/20170220/itwiki-20170220-pages-articles1.xml-p000000002p000442893.bz2" > dump.xml.bz2
	fi
	if [ ${LANGUAGE} = "de" ]; then
		wget "https://dumps.wikimedia.org/dewiki/20170220/dewiki-20170220-pages-articles1.xml-p000000001p000425449.bz2" > dump.xml.bz2
	fi
    if [ ${LANGUAGE} = "fr" ]; then
    	wget "https://dumps.wikimedia.org/frwiki/20170220/frwiki-20170220-pages-articles1.xml-p000000003p000412300.bz2" > dump.xml.bz2
    fi
	mv dump.xml.bz2 /data/input/wikipedia/${LANGUAGE}

	###############     Download DBPedia    ###############
	echo "Downloading DBPedia dump in ${LANGUAGE}..."
	wget "hhttp://downloads.dbpedia.org/2016-04/core-i18n/${LANGUAGE}/mappingbased_objects_${LANGUAGE}.ttl.bz2" > mappingbased_objects.ttl.bz2
	mv mappingbased_objects.ttl.bz2 /data/input/dbpedia/${LANGUAGE}


	###############      Download types     ###############
	echo "Downloading types mappings in ${LANGUAGE}..."
	if [ ${LANGUAGE} = "en" ]; then
		wget "http://downloads.dbpedia.org/2016-04/core-i18n/${LANGUAGE}/instance_types_${LANGUAGE}.ttl.bz2" > instance_types.ttl.bz2
		wget "http://www.airpedia.org/resource/airpedia-classes-${LANGUAGE}.nt.gz" > airpedia.nt.gz
		wget "http://downloads.dbpedia.org/2016-04/core-i18n/${LANGUAGE}/instance_types_dbtax_dbo_${LANGUAGE}.ttl.bz2" > instance_types_dbtax.ttl.bz2
		wget "http://downloads.dbpedia.org/2016-04/core-i18n/${LANGUAGE}//instance_types_lhd_dbo_${LANGUAGE}/.ttl.bz2" > instance_types_lhd.ttl.bz2
		wget "http://downloads.dbpedia.org/2016-04/core-i18n/${LANGUAGE}//instance_types_sdtyped_dbo_${LANGUAGE}/.ttl.bz2" > instance_types_sdtyped.ttl.bz2
		mv instance_types.ttl.bz2 /data/input/sources/${LANGUAGE}/types
		mv airpedia.nt.gz /data/input/sources/${LANGUAGE}/types
		mv instance_types_dbtax.ttl.bz2 /data/input/sources/${LANGUAGE}/types
		mv instance_types_lhd.ttl.bz2/data/input/sources/${LANGUAGE}/types
		mv instance_types_sdtyped.ttl.bz2 /data/input/sources/${LANGUAGE}/types
	else
		wget "http://downloads.dbpedia.org/2016-04/core-i18n/${LANGUAGE}/instance_types_${LANGUAGE}.ttl.bz2" > instance_types.ttl.bz2
		wget "http://www.airpedia.org/resource/airpedia-classes-${LANGUAGE}.nt.gz" > airpedia.nt.gz
		mv instance_types.ttl.bz2 /data/input/sources/${LANGUAGE}/types
		mv airpedia.nt.gz /data/input/sources/${LANGUAGE}/types
	fi

	###############    Download redirect     ###############
	echo "Downloading redirects in ${LANGUAGE}..."
	if [ ${LANGUAGE} = "en" ]; then
		wget "https://www.dropbox.com/s/edanj4trdva1d4g/en_redirects.tsv?dl=0" > redirects.tsv
	fi
	if [ ${LANGUAGE} = "es" ]; then
		wget "https://www.dropbox.com/s/vsun6ck8kb0g1sx/es_redirects.tsv?dl=0" > redirects.tsv
	fi 
 	if [ ${LANGUAGE} = "it" ]; then
		wget "https://www.dropbox.com/s/4b0qj68hwrnezlg/it_redirects.tsv?dl=0" > redirects.tsv
	fi
	if [ ${LANGUAGE} = "de" ]; then
		wget "https://www.dropbox.com/s/j3zue7u3bkkceco/de_redirects.tsv?dl=0" > redirects.tsv
	fi
    if [ ${LANGUAGE} = "fr" ]; then
		wget "https://www.dropbox.com/s/5fad7r7z2g2auph/fr_redirects.tsv?dl=0" > redirects.tsv
    fi
	mv redirects.tsv /data/input/sources/${LANGUAGE}/redirect

	###############    Download OpenNLP models  (only english) ###############
	if [ ${LANGUAGE} = "en" ]; then
		echo "Downloading OpnNLP modules for ${LANGUAGE}..."
		wget "https://raw.githubusercontent.com/richardwilly98/elasticsearch-opennlp-auto-tagging/master/src/main/resources/models/en-lemmatizer.dict" > en-lemmatizer.dict
		wget "http://opennlp.sourceforge.net/models-1.5/en-pos-maxent.bin" > en-pos-maxent.bin
		wget "http://opennlp.sourceforge.net/models-1.5/en-token.bin" > en-token.bin
		mv en-lemmatizer.dict /data/input/models/${LANGUAGE}
		mv en-pos-maxent.bin /data/input/models/${LANGUAGE}
		mv en-token.bin /data/input/models/${LANGUAGE}
	fi

	###############          Download lists      ###############
	echo "Downloading source lists for ${LANGUAGE}..."

	if [ ${LANGUAGE} = "en" ]; then
		wget "https://www.dropbox.com/sh/z1gn38uxvkwvgbi/AADt4hHewICyxwh4zbTN2hy3a?dl=0" > en
		mv en /data/input/lists
	fi
	if [ ${LANGUAGE} = "es" ]; then
		wget "https://www.dropbox.com/sh/ui9vai8ymo56ro9/AADroMIoW_0tTblhF3rXzYo3a?dl=0" > es
		mv es /data/input/lists
	fi
	if [ ${LANGUAGE} = "it" ]; then
		wget "https://www.dropbox.com/sh/1oqbq0citfh6sbt/AAAh42ujAAIWIcUbTL-TF8Hra?dl=0" > it
		mv it /data/input/lists
	fi
	if [ ${LANGUAGE} = "de" ]; then
		wget "https://www.dropbox.com/sh/vm95ud31dkwz1s5/AABvOranaw-QtlQVu7agJjqAa?dl=0" > de
		mv de /data/input/lists
	fi
	if [ ${LANGUAGE} = "fr" ]; then
		wget "https://www.dropbox.com/sh/du6dyn24opxvwdc/AAAkcuvHnFfSDhmJAOSBFumda?dl=0" > fr
		mv fr /data/input/lists
	fi

	###############    Download lang properties  ###############
	echo "Downloading lang properties..."
	wget "https://www.dropbox.com/sh/lq8zrmftsclvayb/AADIAsmAzMik9kpNtcAJGqTna?dl=0" > languages
	mv languages /data/input
	

	###############    and Run it!  ###############
	echo "Executing for ${LANGUAGE} language..."
	echo "--------------------------------------"

	java -jar target/lectorplus-1.0-SNAPSHOT-jar-with-dependencies.jar config.properties ${LANGUAGE}

done

