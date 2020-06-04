import sys, os
import pandas as pd
from pm4py.objects.conversion.log import converter as log_converter
from pm4py.algo.discovery.inductive import algorithm as inductive_miner
from pm4py.objects.conversion.process_tree import converter as pt_converter
from pm4py.objects.petri.exporter import exporter as pnml_exporter
from pm4py.algo.discovery.alpha import algorithm as alpha_miner
dirname = sys.argv[1]
# dirname = "C:/Users/User/Desktop/DIPLOM/SampleHelloWorldProject/_behavioral_model_data/logs"
for filename in os.listdir(dirname):
    if (filename.endswith(".csv")):
        log_csv = pd.read_csv(dirname + "/" + filename, sep=',')
        log_csv.rename(columns={'activity name': 'concept:name'}, inplace=True)
        log_csv.rename(columns={'case ID': 'case:concept:name'}, inplace=True)
        event_log = log_converter.apply(log_csv, variant=log_converter.Variants.TO_EVENT_LOG)
        net, initial_marking, final_marking = alpha_miner.apply(event_log)
        for transition in net.transitions:
            transition.name = transition.name.replace(" ", "_") + "_" + filename.split('.')[0]
            transition.label = transition.label.replace(" ", "_")
        for place in net.places:
            place.name = place.name.replace(" ", "_") + "_" + filename.split('.')[0]

        # tree = inductive_miner.apply_tree(event_log)
        # net, initial_marking, final_marking = pt_converter.apply(tree, variant=pt_converter.Variants.TO_PETRI_NET)
        pnml_exporter.apply(net, initial_marking, dirname + "/" + filename.split('.')[0] + ".pnml")