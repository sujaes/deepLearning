import os
import csv
import random

def make_label ( true_dataFile_path, false_dataFile_path, proportion ) :
    data_list   = list()

    for each in os.listdir( true_dataFile_path ) :
        data_list.append([each,1])
    for each in os.listdir( false_dataFile_path ) :
        data_list.append([each,0])

    random.shuffle(data_list)
    test_data   = data_list[ :int((len(data_list)*proportion)) ]
    train_data  = data_list[ int((len(data_list)*proportion)): ]

    with open( "train_data.csv", "w", newline="" ) as f:
        wr = csv.writer(f)
        for each in train_data :
            wr.writerow(each)

    with open( "test_data.csv", "w", newline="" ) as f:
        wr = csv.writer(f)
        for each in test_data :
            wr.writerow(each)
