#!/bin/bash
#export LANGUAGES=(fr de en es it)
export LANGUAGES=(en it)

# read the wikipedia dump path each language
source ./dump.properties

echo "-------------------------------"
echo "Creating LectorPlus environment"
echo "-------------------------------"

# create folders skeleton
mkdir -p data/sources/ontology
mkdir -p data/spotlight

###############     Download Ontolgy    ###############
echo "1a) Download DBPedia Ontology (language independent)"
final="data/sources/ontology/dbpedia_2016-04.nt"
tmp=$final"_temp"
if [ ! -e $final ]; then
	wget -q -O $tmp "http://downloads.dbpedia.org/2016-04/dbpedia_2016-04.nt"
	mv $tmp $final
else
	echo "       -> already present."
fi

##########     Download DBpedia Spotlight    ##########
echo "1b) Download DBpedia Spotlight Jar (language independent)"
final="data/spotlight/dbpedia-spotlight-latest.jar"
tmp=$final"_temp"
if [ ! -e $final ]; then
	wget -q -O $tmp "http://downloads.dbpedia-spotlight.org/spotlight/dbpedia-spotlight-1.0.0.jar"
	mv $tmp $final
else
	echo "       -> already present."
fi

#### configure the environment for each language
for ((i=0;i<${#LANGUAGES[@]};++i)); do
LANGUAGE=${LANGUAGES[i]}

	echo "--------------------------------  "
	echo "Configuration for language:  ${LANGUAGE} "
	echo "--------------------------------  "

	# create language-specific folders
	mkdir -p data/input/wikipedia/${LANGUAGE}
	mkdir -p data/input/dbpedia/${LANGUAGE}
	mkdir -p data/sources/${LANGUAGE}
	mkdir -p data/sources/${LANGUAGE}/types
	mkdir -p data/sources/${LANGUAGE}/redirect
	mkdir -p data/models/${LANGUAGE}

	###############    Download Wikipedia   #####################
	if [ ${LANGUAGE} = "en" ]; then
		echo "2) Download Wikipedia dump (170MB fragment, 14G whole dump)"
		final="data/input/wikipedia/${LANGUAGE}/dump.xml.bz2"
		tmp=$final"_temp"
		if [ ! -e $final ]; then
			wget -q -O $tmp ${wikipedia_en}
			mv $tmp $final
		else
			echo "       -> already present."
		fi
	fi
	if [ ${LANGUAGE} = "es" ]; then
		echo "2) Download Wikipedia dump (~2.5G)"
		final="data/input/wikipedia/${LANGUAGE}/dump.xml.bz2"
		tmp=$final"_temp"
		if [ ! -e $final ]; then
			wget -q -O $tmp ${wikipedia_es}
			mv $tmp $final
		else
			echo "       -> already present."
		fi
	fi
	if [ ${LANGUAGE} = "de" ]; then
		echo "2) Download Wikipedia dump (~4.2G)"
		final="data/input/wikipedia/${LANGUAGE}/dump.xml.bz2"
		tmp=$final"_temp"
		if [ ! -e $final ]; then
			wget -q -O $tmp ${wikipedia_de}
			mv $tmp $final
		else
			echo "       -> already present."
		fi
	fi
	if [ ${LANGUAGE} = "it" ]; then
		echo "2) Download Wikipedia dump (170MB fragment, 2.2G whole dump)"
		final="data/input/wikipedia/${LANGUAGE}/dump.xml.bz2"
		tmp=$final"_temp"
		if [ ! -e $final ]; then
			wget -q -O $tmp ${wikipedia_it}
			mv $tmp $final
		else
			echo "       -> already present."
		fi
	fi
	if [ ${LANGUAGE} = "fr" ]; then
		echo "2) Download Wikipedia dump (~3.5G)"
		final="data/input/wikipedia/${LANGUAGE}/dump.xml.bz2"
		tmp=$final"_temp"
		if [ ! -e $final ]; then
			wget -q -O $tmp ${wikipedia_fr}
			mv $tmp $final
		else
			echo "       -> already present."
		fi
	fi

	###############     Download DBPedia    ###############
	echo "3) Download DBPedia dump (160MB or less)"
	final="data/input/dbpedia/${LANGUAGE}/mappingbased_objects.ttl.bz2"
	tmp=$final"_temp"

	if [ ! -e $final ]; then
		dbpedia="http://downloads.dbpedia.org/2016-04/core-i18n/${LANGUAGE}/mappingbased_objects_${LANGUAGE}.ttl.bz2"
		wget -q -O $tmp ${dbpedia}
		mv $tmp $final
	else
		echo "       -> already present."
	fi

	###############      Download types     ###############
	echo "3b) Download types mappings"

	if [ ${LANGUAGE} = "en" ] || [ ${LANGUAGE} = "de" ] || [ ${LANGUAGE} = "es" ] || [ ${LANGUAGE} = "it" ] || [ ${LANGUAGE} = "fr" ] ; then

		echo "     -> type_instance.ttl"
		final="data/sources/${LANGUAGE}/types/instance_types.ttl.bz2"
		tmp=$final"_temp"
		if [ ! -e $final ]; then
			wget -q -O $tmp "http://downloads.dbpedia.org/2016-04/core-i18n/${LANGUAGE}/instance_types_${LANGUAGE}.ttl.bz2"
			mv $tmp $final
		else
			echo "       -> already present."
		fi

		echo "     -> airpedia.nt"
		final="data/sources/${LANGUAGE}/types/airpedia.nt.gz"
		tmp=$final"_temp"
		if [ ! -e $final ]; then
			wget -q -O $tmp "http://www.airpedia.org/resource/airpedia-classes-${LANGUAGE}.nt.gz"
			mv $tmp $final
		else
			echo "       -> already present."
		fi

	fi

	if [ ${LANGUAGE} = "en" ] || [ ${LANGUAGE} = "de" ]; then

		echo "     -> type_instance_lhd.ttl"
		final="data/sources/${LANGUAGE}/types/instance_types_lhd.ttl.bz2"
		tmp=$final"_temp"
		if [ ! -e $final ]; then
			wget -q -O $tmp "http://downloads.dbpedia.org/2016-04/core-i18n/${LANGUAGE}/instance_types_lhd_dbo_${LANGUAGE}.ttl.bz2"
			mv $tmp $final
		else
			echo "       -> already present."
		fi

		echo "     -> type_instance_sdtyped.ttl"
		final="data/sources/${LANGUAGE}/types/instance_types_sdtyped.ttl.bz2"
		tmp=$final"_temp"
		if [ ! -e $final ]; then
			wget -q -O $tmp "http://downloads.dbpedia.org/2016-04/core-i18n/${LANGUAGE}/instance_types_sdtyped_dbo_${LANGUAGE}.ttl.bz2"
			mv $tmp $final
		else
			echo "       -> already present."
		fi

	fi

	if [ ${LANGUAGE} = "en" ]; then
		echo "     -> type_instance_dbtax.ttl"
		final="data/sources/${LANGUAGE}/types/instance_types_dbtax.ttl.bz2"
		tmp=$final"_temp"
		if [ ! -e $final ]; then
			wget -q -O $tmp "http://downloads.dbpedia.org/2016-04/core-i18n/${LANGUAGE}/instance_types_dbtax_dbo_${LANGUAGE}.ttl.bz2"
			mv $tmp $final
		else
			echo "       -> already present."
		fi
	fi

	###############    Download redirect     ###############
	echo "3d) Download redirects file"
	final="data/sources/${LANGUAGE}/redirect/redirects.tsv.bz2"
	tmp=$final"_temp"

	if [ ${LANGUAGE} = "en" ]; then
		if [ ! -e $final ]; then
			wget -q -O $tmp "https://drive.google.com/uc?export=download&id=1VO6PqWGWMr__SD09Ji8yyNWmMPMd0El4"
			mv $tmp $final
		else
			echo "       -> already present."
		fi
	fi
	if [ ${LANGUAGE} = "es" ]; then
		if [ ! -e $final ]; then
			wget -q -O $tmp "https://drive.google.com/uc?export=download&id=1cbWfHh3xG8UZOf99wj_TUuKJ6xSL-zId"
			mv $tmp $final
		else
			echo "       -> already present."
		fi
	fi
	if [ ${LANGUAGE} = "it" ]; then
		if [ ! -e $final ]; then
			wget -q -O $tmp "https://drive.google.com/uc?export=download&id=1CPccl_V4CLe5hFNjSYfWa8yOTRuiV-M7"
			mv $tmp $final
		else
			echo "       -> already present."
		fi
	fi
	if [ ${LANGUAGE} = "de" ]; then
		if [ ! -e $final ]; then
			wget -q -O $tmp "https://drive.google.com/uc?export=download&id=1zaiSK-gwUxQXmC9tIaMRa_TeAH9cjW_7"
			mv $tmp $final
		else
			echo "       -> already present."
		fi
	fi
	if [ ${LANGUAGE} = "fr" ]; then
		if [ ! -e $final ]; then
			wget -q -O $tmp "https://drive.google.com/uc?export=download&id=1xyaa-iFlWqQXR-BpDbvV9EpEwzvb_73W"
			mv $tmp $final
		else
			echo "       -> already present."
		fi
	fi

	###############    Download OpenNLP models  (only english) ###############
	if [ ${LANGUAGE} = "en" ]; then
		echo "3f) Downloading OpnNLP modules (only for ${LANGUAGE})"

		echo " -> downlaod em-lemmatizer.dict ..."
		final="data/models/${LANGUAGE}/en-lemmatizer.dict"
		tmp=$final"_temp"
		if [ ! -e $final ]; then
			wget -q -O $tmp "http://raw.githubusercontent.com/richardwilly98/elasticsearch-opennlp-auto-tagging/master/src/main/resources/models/en-lemmatizer.dict"
			mv $tmp $final
		else
			echo "       -> already present."
		fi

		echo " -> downlaod en-pos-maxent ..."
		final="data/models/${LANGUAGE}/en-pos-maxent.bin"
		tmp=$final"_temp"
		if [ ! -e $final ]; then
			wget -q -O $tmp "http://opennlp.sourceforge.net/models-1.5/en-pos-maxent.bin"
			mv $tmp $final
		else
			echo "       -> already present."
		fi

		echo " -> downlaod en-token ..."
		final="data/models/${LANGUAGE}/en-token.bin"
		tmp=$final"_temp"
		if [ ! -e $final ]; then
			wget -q -O $tmp "http://opennlp.sourceforge.net/models-1.5/en-token.bin"
			mv $tmp $final
		else
			echo "       -> already present."
		fi
	fi

	###############     Download DBPedia Spotlight model   ###############
	echo "3b) Download DBPedia Spotlight model"
	final="data/spotlight/${LANGUAGE}"
	if [ ! -e $final ]; then
		wget -q "http://downloads.dbpedia-spotlight.org/2016-04/en/model/raw/${LANGUAGE}.tar.gz"
		tar -xzf ${LANGUAGE}.tar.gz
		rm ${LANGUAGE}.tar.gz
		mv ${LANGUAGE} data/spotlight
	else
		echo "       -> already present."
	fi
done

echo ""
echo "-----------------------"
echo "LectorPlus successfully installed."
echo "-----------------------"
