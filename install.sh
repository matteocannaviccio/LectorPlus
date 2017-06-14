#!/bin/bash
export LANGUAGES=(en de es it fr)

# read the wikipedia dump path each language
source ./dump.properties

echo "-------------------------------"
echo "Creating LectorPlus environment"
echo "-------------------------------"

# create folders skeleton
mkdir data
mkdir data/input
mkdir data/input/wikipedia
mkdir data/input/dbpedia
mkdir data/lists
mkdir data/languages
mkdir data/models
mkdir data/sources
mkdir data/sources/ontology

###############     Download Ontolgy    ###############
echo "1) Download DBPedia Ontology (language independent)"
wget -q  "http://downloads.dbpedia.org/2016-04/dbpedia_2016-04.nt"
mv dbpedia_2016-04.nt data/sources/ontology

#### configure the environment for each language
for ((i=0;i<${#LANGUAGES[@]};++i)); do
LANGUAGE=${LANGUAGES[i]}

	# create language-specific folders
	mkdir data/input/wikipedia/${LANGUAGE}
	mkdir data/input/dbpedia/${LANGUAGE}
	mkdir data/sources/${LANGUAGE}
	mkdir data/sources/${LANGUAGE}/types
	mkdir data/sources/${LANGUAGE}/redirect
	mkdir data/lists/${LANGUAGE}
	mkdir data/models/${LANGUAGE}

	echo "--------------------------------  "
	echo "Configuration for language:  ${LANGUAGE} "
	echo "--------------------------------  "

	###############    Download lang properties  ###############
	echo "2) Download language properties"
	if [ ${LANGUAGE} = "en" ]; then
		wget -q -O data/languages/${LANGUAGE}.properties "https://www.dropbox.com/s/y3yqakdcwhizkx2/en.properties?dl=0"
	fi
	if [ ${LANGUAGE} = "es" ]; then
		wget -q -O data/languages/${LANGUAGE}.properties "https://www.dropbox.com/s/ijviti49l6udmcu/es.properties?dl=0"
	fi
	if [ ${LANGUAGE} = "it" ]; then
		wget -q -O data/languages/${LANGUAGE}.properties "https://www.dropbox.com/s/q02dxaf5hzq7tmd/it.properties?dl=0"
	fi
	if [ ${LANGUAGE} = "de" ]; then
		wget -q -O data/languages/${LANGUAGE}.properties "https://www.dropbox.com/s/r3mebdhz62r57wu/de.properties?dl=0"
	fi
	if [ ${LANGUAGE} = "fr" ]; then
		wget -q -O data/languages/${LANGUAGE}.properties "https://www.dropbox.com/s/lc83zvfozwzu2bp/fr.properties?dl=0"
	fi

	###############    Download Wikipedia   #####################
	echo "3a) Download Wikipedia dump"
	if [ ${LANGUAGE} = "en" ]; then
		wget -q -O data/input/wikipedia/en/dump.xml.bz2 ${wikipedia_en}
	fi
	if [ ${LANGUAGE} = "es" ]; then
		wget -q -O data/input/wikipedia/es/dump.xml.bz2 ${wikipedia_es}
    fi 
 	if [ ${LANGUAGE} = "it" ]; then
		wget -q -O data/input/wikipedia/it/dump.xml.bz2 ${wikipedia_it}
	fi
	if [ ${LANGUAGE} = "de" ]; then
		wget -q -O data/input/wikipedia/de/dump.xml.bz2 ${wikipedia_de}
	fi
    if [ ${LANGUAGE} = "fr" ]; then
		wget -q -O data/input/wikipedia/fr/dump.xml.bz2 ${wikipedia_fr}
    fi

	###############     Download DBPedia    ###############
	echo "3b) Download DBPedia dump"
	dbpedia="http://downloads.dbpedia.org/2016-04/core-i18n/${LANGUAGE}/mappingbased_objects_${LANGUAGE}.ttl.bz2"
	wget -q -O data/input/dbpedia/${LANGUAGE}/mappingbased_objects.ttl.bz2 ${dbpedia}


	###############      Download types     ###############
	echo "3c) Download types mappings"

	if [ ${LANGUAGE} = "en" ]; then
		echo "     -> type_instance.ttl"
		wget -q  "http://downloads.dbpedia.org/2016-04/core-i18n/${LANGUAGE}/instance_types_${LANGUAGE}.ttl.bz2"
		mv instance_types_${LANGUAGE}.ttl.bz2 data/sources/${LANGUAGE}/types/instance_types.ttl.bz2
		
		echo "     -> airpedia.nt"
		wget -q "http://www.airpedia.org/resource/airpedia-classes-${LANGUAGE}.nt.gz"
		mv airpedia-classes-${LANGUAGE}.nt.gz data/sources/${LANGUAGE}/types/airpedia.nt.gz
		
		echo "     -> type_instance_dbtax.ttl"
		wget -q  "http://downloads.dbpedia.org/2016-04/core-i18n/${LANGUAGE}/instance_types_dbtax_dbo_${LANGUAGE}.ttl.bz2"
		mv instance_types_dbtax_dbo_${LANGUAGE}.ttl.bz2 data/sources/${LANGUAGE}/types/instance_types_dbtax.ttl.bz2
		
		echo "     -> type_instance_lhd.ttl"
		wget -q  "http://downloads.dbpedia.org/2016-04/core-i18n/${LANGUAGE}/instance_types_lhd_dbo_${LANGUAGE}.ttl.bz2"
		mv instance_types_lhd_dbo_${LANGUAGE}.ttl.bz2 data/sources/${LANGUAGE}/types/instance_types_lhd.ttl.bz2
		
		echo "     -> type_instance_sdtypes.ttl"
		wget -q  "http://downloads.dbpedia.org/2016-04/core-i18n/${LANGUAGE}/instance_types_sdtyped_dbo_${LANGUAGE}.ttl.bz2"
		mv instance_types_sdtyped_dbo_${LANGUAGE}.ttl.bz2 data/sources/${LANGUAGE}/types/instance_types_sdtyped.ttl.bz2
	fi
	if [ ${LANGUAGE} = "de" ]; then
		echo "     -> type_instance.ttl"
		wget -q  "http://downloads.dbpedia.org/2016-04/core-i18n/${LANGUAGE}/instance_types_${LANGUAGE}.ttl.bz2"
		mv instance_types_${LANGUAGE}.ttl.bz2 data/sources/${LANGUAGE}/types/instance_types.ttl.bz2
		
		echo "     -> airpedia.nt"
		wget -q "http://www.airpedia.org/resource/airpedia-classes-${LANGUAGE}.nt.gz"
		mv airpedia-classes-${LANGUAGE}.nt.gz data/sources/${LANGUAGE}/types/airpedia.nt.gz

		echo "     -> type_instance_sdtypes.ttl"
		wget -q  "http://downloads.dbpedia.org/2016-04/core-i18n/${LANGUAGE}/instance_types_sdtyped_dbo_${LANGUAGE}.ttl.bz2"
		mv instance_types_sdtyped_dbo_${LANGUAGE}.ttl.bz2 data/sources/${LANGUAGE}/types/instance_types_sdtyped.ttl.bz2
	
		echo "     -> type_instance_lhd.ttl"
		wget -q  "http://downloads.dbpedia.org/2016-04/core-i18n/${LANGUAGE}/instance_types_lhd_dbo_${LANGUAGE}.ttl.bz2"
		mv instance_types_lhd_dbo_${LANGUAGE}.ttl.bz2 data/sources/${LANGUAGE}/types/instance_types_lhd.ttl.bz2
	fi
	if [ ${LANGUAGE} != "de" ] && [ ${LANGUAGE} != "en" ]; then
		echo "     -> type_instance.ttl"
		wget -q  "http://downloads.dbpedia.org/2016-04/core-i18n/${LANGUAGE}/instance_types_${LANGUAGE}.ttl.bz2"
		mv instance_types_${LANGUAGE}.ttl.bz2 data/sources/${LANGUAGE}/types/instance_types.ttl.bz2
		
		echo "     -> airpedia.nt"
		wget -q "http://www.airpedia.org/resource/airpedia-classes-${LANGUAGE}.nt.gz"
		mv airpedia-classes-${LANGUAGE}.nt.gz data/sources/${LANGUAGE}/types/airpedia.nt.gz
	fi

	###############    Download redirect     ###############
	echo "3d) Download redirects file"
	if [ ${LANGUAGE} = "en" ]; then
		wget -q -O data/sources/${LANGUAGE}/redirect/redirects.tsv "https://www.dropbox.com/s/lwgc19srzlrtaqp/redirects.tsv.bz2?dl=0"
	fi
	if [ ${LANGUAGE} = "es" ]; then
		wget -q -O data/sources/${LANGUAGE}/redirect/redirects.tsv "https://www.dropbox.com/s/u8agpsnwtg3yhlp/redirects.tsv.bz2?dl=0"
	fi 
 	if [ ${LANGUAGE} = "it" ]; then
		wget -q -O data/sources/${LANGUAGE}/redirect/redirects.tsv "https://www.dropbox.com/s/4ea7z5g2h9s31rd/redirects.tsv.bz2?dl=0"
	fi
	if [ ${LANGUAGE} = "de" ]; then
		wget -q -O data/sources/${LANGUAGE}/redirect/redirects.tsv "https://www.dropbox.com/s/o2jso5r917jspld/redirects.tsv.bz2?dl=0"
	fi
    if [ ${LANGUAGE} = "fr" ]; then
		wget -q -O data/sources/${LANGUAGE}/redirect/redirects.tsv "https://www.dropbox.com/s/hj9psr9i5maeu43/redirects.tsv.bz2?dl=0"
    fi

	###############          Download lists      ###############
	echo "3e) Download source lists:"
	
	if [ ${LANGUAGE} = "en" ]; then
		echo "     -> currencies.tsv"
		wget -q -O data/lists/${LANGUAGE}/currencies.tsv "https://www.dropbox.com/s/agdx57liefhxoig/currencies.tsv?dl=0"

		echo "     -> nationalities.tsv"
		wget -q -O data/lists/${LANGUAGE}/nationalities.tsv "https://www.dropbox.com/s/xoz08shuuet2h3s/nationalities.tsv?dl=0"

		echo "     -> professions.tsv"
		wget -q -O data/lists/${LANGUAGE}/professions.tsv "https://www.dropbox.com/s/4ul4z9rra71tqj4/professions.tsv?dl=0"

		echo "     -> stopwords.tsv"
		wget -q -O data/lists/${LANGUAGE}/stopwords.tsv "https://www.dropbox.com/s/03ferhbc1tp15c7/stopwords.tsv?dl=0"
	fi

	if [ ${LANGUAGE} = "es" ]; then
		echo "     -> currencies.tsv"
		wget -q -O data/lists/${LANGUAGE}/currencies.tsv "https://www.dropbox.com/s/ph9xngzhmobsvtc/currencies.tsv?dl=0"

		echo "     -> nationalities.tsv"
		wget -q -O data/lists/${LANGUAGE}/nationalities.tsv "https://www.dropbox.com/s/pu7b67fk2llirpd/nationalities.tsv?dl=0"

		echo "     -> professions.tsv"
		wget -q -O data/lists/${LANGUAGE}/professions.tsv "https://www.dropbox.com/s/ddffrwopa2q4afr/professions.tsv?dl=0"
	fi

	if [ ${LANGUAGE} = "it" ]; then
		echo "     -> currencies.tsv"
		wget -q -O data/lists/${LANGUAGE}/currencies.tsv "https://www.dropbox.com/s/awzydkuubc0pctc/currencies.tsv?dl=0"

		echo "     -> nationalities.tsv"
		wget -q -O data/lists/${LANGUAGE}/nationalities.tsv "https://www.dropbox.com/s/5bi5kkevpfga0g7/nationalities.tsv?dl=0"

		echo "     -> professions.tsv"
		wget -q -O data/lists/${LANGUAGE}/professions.tsv "https://www.dropbox.com/s/xsan6dr0nxippd0/professions.tsv?dl=0"
	fi

	if [ ${LANGUAGE} = "fr" ]; then
		echo "     -> currencies.tsv"
		wget -q -O data/lists/${LANGUAGE}/currencies.tsv "https://www.dropbox.com/s/cfsqyotj34e4bhq/currencies.tsv?dl=0"

		echo "     -> nationalities.tsv"
		wget -q -O data/lists/${LANGUAGE}/nationalities.tsv "https://www.dropbox.com/s/brdk81sox778nt6/nationalities.tsv?dl=0"

		echo "     -> professions.tsv"
		wget -q -O data/lists/${LANGUAGE}/professions.tsv "https://www.dropbox.com/s/ibocfyhrlo607e1/professions.tsv?dl=0"
	fi

	if [ ${LANGUAGE} = "de" ]; then
		echo "     -> currencies.tsv"
		wget -q -O data/lists/${LANGUAGE}/currencies.tsv "https://www.dropbox.com/s/4k550pqii8ox69j/currencies.tsv?dl=0"

		echo "     -> nationalities.tsv"
		wget -q -O data/lists/${LANGUAGE}/nationalities.tsv "https://www.dropbox.com/s/1qf74es81b3c245/nationalities.tsv?dl=0"

		echo "     -> professions.tsv"
		wget -q -O data/lists/${LANGUAGE}/professions.tsv "https://www.dropbox.com/s/sob8376r0mab3aa/professions.tsv?dl=0"
	fi

	###############    Download OpenNLP models  (only english) ###############
	if [ ${LANGUAGE} = "en" ]; then
		echo "3f) Downloading OpnNLP modules (only for ${LANGUAGE})"
		echo " -> downlaod em-lemmatizer.dict ..."
		wget -q -O data/models/${LANGUAGE}/en-lemmatizer.dict "http://raw.githubusercontent.com/richardwilly98/elasticsearch-opennlp-auto-tagging/master/src/main/resources/models/en-lemmatizer.dict"

		echo " -> downlaod en-pos-maxent ..."
		wget -q -O data/models/${LANGUAGE}/en-pos-maxent.bin "http://opennlp.sourceforge.net/models-1.5/en-pos-maxent.bin"

		echo " -> downlaod en-token ..."
		wget -q -O data/models/${LANGUAGE}/en-token.bin "http://opennlp.sourceforge.net/models-1.5/en-token.bin"
	fi
done

echo ""
echo "------------------"
echo "LectorPlus successfully installed."
