__author__ = 'matteo'
# -*- coding: utf-8 -*-

import os


def main(relation_stats_file):
    relation_stats_reader = open(relation_stats_file, 'r')

    rel2phrases = {}
    rel2typedphrases = {}

    for line in relation_stats_reader:
        line = line.rstrip("\n")
        fields = line.split("\t")

        relation = fields[2].replace("(-1)", "")
        typed_phrase = fields[7]
        phrase = str(fields[7].split(" ")[1:-1])

        if relation not in rel2phrases:
            rel2phrases[relation] = set()
        rel2phrases[relation].add(phrase)

        if relation not in rel2typedphrases:
            rel2typedphrases[relation] = set()
        rel2typedphrases[relation].add(typed_phrase)

    relation_stats_reader.close()

    for r in rel2phrases.keys():
        tp_size = rel2typedphrases[r]
        p_size = rel2phrases[r]
        print r + "\t" + str(len(tp_size)) + "\t" + str(len(p_size))


if __name__ == "__main__":
    '''
    input_folder = "/Users/matteo/Desktop/ModelTextExt/en"
    for sbdirs, dirs, files in os.walk(input_folder):
        if len(files) > 0:
            for file in files:
                if file == ".DS_Store":
                    continue
                if file.endswith("provenance.bz2"):
                    language = file.split("_")[0]
                    main(input_folder + "/" + file)
    '''
    file = "/Users/matteo/Desktop/prov/sample_prov_1M.tsv"
    main(file)
