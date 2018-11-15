#!/bin/bash
export LANGUAGES=(fr de en es it)

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
	mkdir -p data/lists/${LANGUAGE}
	mkdir -p data/models/${LANGUAGE}
	mkdir -p data/languages

	###############    Download lang properties  ###############
	echo "2) Download language properties"
	final="data/languages/${LANGUAGE}.properties"
	tmp=$final"_temp"

	if [ ${LANGUAGE} = "en" ]; then
		if [ ! -e $final ]; then
			wget -q -O $tmp "https://www.dropbox.com/s/y3yqakdcwhizkx2/en.properties?dl=0"
			mv $tmp $final
		else
			echo "       -> already present."
		fi
	fi
	if [ ${LANGUAGE} = "es" ]; then
		if [ ! -e $final ] ; then
			wget -q -O $tmp "https://www.dropbox.com/s/ijviti49l6udmcu/es.properties?dl=0"
			mv $tmp $final
		else
			echo "       -> already present."
		fi
	fi
	if [ ${LANGUAGE} = "it" ]; then
		if [ ! -e $final ]; then
			wget -q -O $tmp "https://www.dropbox.com/s/q02dxaf5hzq7tmd/it.properties?dl=0"
			mv $tmp $final
		else
			echo "       -> already present."
		fi
	fi
	if [ ${LANGUAGE} = "de" ]; then
		if [ ! -e $final ]; then
			wget -q -O $tmp "https://www.dropbox.com/s/r3mebdhz62r57wu/de.properties?dl=0"
			mv $tmp $final
		else
			echo "       -> already present."
		fi
	fi
	if [ ${LANGUAGE} = "fr" ]; then
		if [ ! -e $final ]; then
			wget -q -O $tmp "https://www.dropbox.com/s/lc83zvfozwzu2bp/fr.properties?dl=0"
			mv $tmp $final
		else
			echo "       -> already present."
		fi
	fi

	###############    Download Wikipedia   #####################
	if [ ${LANGUAGE} = "en" ]; then
		echo "3a) Download Wikipedia dump (~14G)"
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
		echo "3a) Download Wikipedia dump (~2.5G)"
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
		echo "3a) Download Wikipedia dump (~4.2G)"
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
		echo "3a) Download Wikipedia dump (~2.2G)"
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
		echo "3a) Download Wikipedia dump (~3.5G)"
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
	echo "3b) Download DBPedia dump"
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
	echo "3c) Download types mappings"

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
			wget -q -O $tmp "https://www.dropbox.com/s/upou8x1mutrypm4/redirects.tsv.bz2?dl=0"
			mv $tmp $final
		else
			echo "       -> already present."
		fi
	fi
	if [ ${LANGUAGE} = "es" ]; then
		if [ ! -e $final ]; then
			wget -q -O $tmp "https://www.dropbox.com/s/u8agpsnwtg3yhlp/redirects.tsv.bz2?dl=0"
			mv $tmp $final
		else
			echo "       -> already present."
		fi
	fi
	if [ ${LANGUAGE} = "it" ]; then
		if [ ! -e $final ]; then
			wget -q -O $tmp "https://www.dropbox.com/s/4ea7z5g2h9s31rd/redirects.tsv.bz2?dl=0"
			mv $tmp $final
		else
			echo "       -> already present."
		fi
	fi
	if [ ${LANGUAGE} = "de" ]; then
		if [ ! -e $final ]; then
			wget -q -O $tmp "https://www.dropbox.com/s/o2jso5r917jspld/redirects.tsv.bz2?dl=0"
			mv $tmp $final
		else
			echo "       -> already present."
		fi
	fi
	if [ ${LANGUAGE} = "fr" ]; then
		if [ ! -e $final ]; then
			wget -q -O $tmp "https://www.dropbox.com/s/hj9psr9i5maeu43/redirects.tsv.bz2?dl=0"
			mv $tmp $final
		else
			echo "       -> already present."
		fi
	fi

	###############          Download lists      ###############
	echo "3e) Download source lists:"

	if [ ${LANGUAGE} = "en" ]; then

		echo "     -> currencies.tsv"
		final="data/lists/${LANGUAGE}/currencies.tsv"
		tmp=$final"_temp"
		if [ ! -e $final ]; then
			wget -q -O $tmp "https://www.dropbox.com/s/agdx57liefhxoig/currencies.tsv?dl=0"
			mv $tmp $final
		else
			echo "       -> already present."
		fi

		echo "     -> nationalities.tsv"
		final="data/lists/${LANGUAGE}/nationalities.tsv"
		tmp=$final"_temp"
		if [ ! -e $final ]; then
			wget -q -O $tmp "https://www.dropbox.com/s/xoz08shuuet2h3s/nationalities.tsv?dl=0"
			mv $tmp $final
		else
			echo "       -> already present."
		fi

		echo "     -> professions.tsv"
		final="data/lists/${LANGUAGE}/professions.tsv"
		tmp=$final"_temp"
		if [ ! -e $final ]; then
			wget -q -O $tmp "https://www.dropbox.com/s/4ul4z9rra71tqj4/professions.tsv?dl=0"
			mv $tmp $final
		else
			echo "       -> already present."
		fi

		echo "     -> stopwords.tsv"
		final="data/lists/${LANGUAGE}/stopwords.tsv"
		tmp=$final"_temp"
		if [ ! -e $final ]; then
			wget -q -O $tmp "https://www.dropbox.com/s/03ferhbc1tp15c7/stopwords.tsv?dl=0"
			mv $tmp $final
		else
			echo "       -> already present."
		fi
	fi

	if [ ${LANGUAGE} = "es" ]; then
		echo "     -> currencies.tsv"
		final="data/lists/${LANGUAGE}/currencies.tsv"
		tmp=$final"_temp"
		if [ ! -e $final ]; then
			wget -q -O $tmp "https://www.dropbox.com/s/ph9xngzhmobsvtc/currencies.tsv?dl=0"
			mv $tmp $final
		else
			echo "       -> already present."
		fi

		echo "     -> nationalities.tsv"
		final="data/lists/${LANGUAGE}/nationalities.tsv"
		tmp=$final"_temp"
		if [ ! -e $final ]; then
			wget -q -O $tmp "https://www.dropbox.com/s/pu7b67fk2llirpd/nationalities.tsv?dl=0"
			mv $tmp $final
		else
			echo "       -> already present."
		fi

		echo "     -> professions.tsv"
		final="data/lists/${LANGUAGE}/professions.tsv"
		tmp=$final"_temp"
		if [ ! -e $final ]; then
			wget -q -O $tmp "https://www.dropbox.com/s/ddffrwopa2q4afr/professions.tsv?dl=0"
			mv $tmp $final
		else
			echo "       -> already present."
		fi
	fi

	if [ ${LANGUAGE} = "it" ]; then
		echo "     -> currencies.tsv"
		final="data/lists/${LANGUAGE}/currencies.tsv"
		tmp=$final"_temp"
		if [ ! -e $final ]; then
			wget -q -O $tmp "https://www.dropbox.com/s/awzydkuubc0pctc/currencies.tsv?dl=0"
			mv $tmp $final
		else
			echo "       -> already present."
		fi

		echo "     -> nationalities.tsv"
		final="data/lists/${LANGUAGE}/nationalities.tsv"
		tmp=$final"_temp"
		if [ ! -e $final ]; then
			wget -q -O $tmp "https://www.dropbox.com/s/5bi5kkevpfga0g7/nationalities.tsv?dl=0"
			mv $tmp $final
		else
			echo "       -> already present."
		fi

		echo "     -> professions.tsv"
		final="data/lists/${LANGUAGE}/professions.tsv"
		tmp=$final"_temp"
		if [ ! -e $final ]; then
			wget -q -O $tmp "https://www.dropbox.com/s/xsan6dr0nxippd0/professions.tsv?dl=0"
			mv $tmp $final
		else
			echo "       -> already present."
		fi
	fi

	if [ ${LANGUAGE} = "fr" ]; then
		echo "     -> currencies.tsv"
		final="data/lists/${LANGUAGE}/currencies.tsv"
		tmp=$final"_temp"
		if [ ! -e $final ]; then
			wget -q -O $tmp "https://www.dropbox.com/s/cfsqyotj34e4bhq/currencies.tsv?dl=0"
			mv $tmp $final
		else
			echo "       -> already present."
		fi

		echo "     -> nationalities.tsv"
		final="data/lists/${LANGUAGE}/nationalities.tsv"
		tmp=$final"_temp"
		if [ ! -e $final ]; then
			wget -q -O $tmp "https://www.dropbox.com/s/brdk81sox778nt6/nationalities.tsv?dl=0"
			mv $tmp $final
		else
			echo "       -> already present."
		fi

		echo "     -> professions.tsv"
		final="data/lists/${LANGUAGE}/professions.tsv"
		tmp=$final"_temp"
		if [ ! -e $final ]; then
			wget -q -O $tmp "https://www.dropbox.com/s/ibocfyhrlo607e1/professions.tsv?dl=0"
			mv $tmp $final
		else
			echo "       -> already present."
		fi

	fi

	if [ ${LANGUAGE} = "de" ]; then
		echo "     -> currencies.tsv"
		final="data/lists/${LANGUAGE}/currencies.tsv"
		tmp=$final"_temp"
		if [ ! -e $final ]; then
			wget -q -O $tmp "https://www.dropbox.com/s/4k550pqii8ox69j/currencies.tsv?dl=0"
			mv $tmp $final
		else
			echo "       -> already present."
		fi

		echo "     -> nationalities.tsv"
		final="data/lists/${LANGUAGE}/nationalities.tsv"
		tmp=$final"_temp"
		if [ ! -e $final ]; then
			wget -q -O $tmp "https://www.dropbox.com/s/1qf74es81b3c245/nationalities.tsv?dl=0"
			mv $tmp $final
		else
			echo "       -> already present."
		fi

		echo "     -> professions.tsv"
		final="data/lists/${LANGUAGE}/professions.tsv"
		tmp=$final"_temp"
		if [ ! -e $final ]; then
			wget -q -O $tmp "https://www.dropbox.com/s/sob8376r0mab3aa/professions.tsv?dl=0"
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
