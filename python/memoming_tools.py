import os
import csv
import shutil
import random

class csv_batch_container :
    def __init__ (self, csvPath):
        self.csvPath = csvPath
        self.cursor = 0
        self.readCSV()

    def readCSV (self) :
        with open(self.csvPath,"r") as csvFile :
            csv_data_reader = csv.reader(csvFile)
            self.csv_data = list()
            for row in csv_data_reader :
                self.csv_data.append(row)
            self.csv_length = len(self.csv_data)

    def next_batch(self, batch_size):
        if( self.csv_length <= self.cursor ) :
            return "null"
        total_data  = list()
        x_data      = list()
        y_data      = list()
        if( ((self.csv_length-self.cursor) < batch_size) or (self.csv_length < self.cursor + batch_size) ):
            total_data = self.csv_data[self.cursor:]
        else :
            total_data = self.csv_data[self.cursor : self.cursor+batch_size]
        self.cursor += batch_size
        for each in total_data:
            x_data.append(each[0])
            y_data.append(each[1])
        return (x_data,y_data)

    def num_examples(self):
        return self.csv_length

def find_and_move_anotherFile_inPath ( targetPath, filterPath,  savePath ) :
    targetFileList = os.listdir( targetPath )
    filterFileList = os.listdir( filterPath )
    fileNo         = 0

    for item in targetFileList :
        flag = True
        for check in filterFileList :
            if ( item == check ) :
                flag = False
                break
        if ( flag ) :
            fileNo += 1
            shutil.move( os.path.join(targetPath,item), savePath )
            print("["+str(fileNo)+"] moved file : ", item)

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
