__author__ = 'matteo'
# -*- coding: utf-8 -*-

import sys
import os


def main(relation_stats_file):
	relation_stats_reader = open(relation_stats_file, 'r')
	language = relation_stats_file.split("/")[-1].split("_")[0]

	rel2count = {}

	for line in relation_stats_reader:
		line = line.rstrip("\n")
		fields = line.split("\t")

		count = int(fields[0])
		relation = fields[1].replace("(-1)", "")
		if not rel2count.has_key(relation):
			rel2count[relation] = 0
		rel2count[relation] = rel2count[relation] + count
		
	relation_stats_reader.close()

	for r in rel2count.keys():
		print str(rel2count[r]) + "\t" +r

if __name__ == "__main__":
	input_folder = "/Users/matteo/Desktop/ModelTextExt/es"
	for sbdirs, dirs, files in os.walk(input_folder):
		if len(files) > 0:
			for file in files:
				if file == ".DS_Store":
					continue
				if file.endswith("relations_stats.tsv"):
					language = file.split("_")[0]
					main(input_folder  + "/" + file)				


