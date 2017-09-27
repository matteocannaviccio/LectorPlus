__author__ = 'matteo'
# -*- coding: utf-8 -*-


def getMapFromFile(file):
    '''
    Reads the map from the file
    '''
    map = {}
    for line in file:
        line = line.rstrip("\n")
        fields = line.split("\t")

        relation = fields[2]
        typed_phrase = fields[7]

        if relation not in map:
            map[relation] = set()
        map[relation].add(typed_phrase)
    file.close()
    return map


def main(provenance_1M_file, provenance_8M_file):
    provenance_1M_file_reader = open(provenance_1M_file, 'r')
    provenance_8M_file_reader = open(provenance_8M_file, 'r')

    rel2typedphrases_1M = getMapFromFile(provenance_1M_file_reader)
    rel2typedphrases_8M = getMapFromFile(provenance_8M_file_reader)


    for r in rel2typedphrases_1M.keys():
        tp_size_8M = rel2typedphrases_8M[r]
        tp_size_1M = rel2typedphrases_1M[r]
        inter = set.intersection(tp_size_8M, tp_size_1M)
        print r + "\t" + str(len(tp_size_1M)) + "\t" + str(len(tp_size_8M)) + "\t" + str(len(inter))


if __name__ == "__main__":
    provenance_1M_file = "/Users/matteo/Desktop/prov/en_provenance-1m"
    provenance_8M_file = "/Users/matteo/Desktop/prov/en_provenance-8m"
    main(provenance_1M_file, provenance_8M_file)
