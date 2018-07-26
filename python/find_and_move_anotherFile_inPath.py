import os
import shutil

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

# os.getcwd()
# C:\Users\memoming\DegreeProject\Github\Python\Data

targetPath = os.getcwd() + "\\extract\\원본\\주현"
# C:\Users\memoming\DegreeProject\Github\Python\Data\extract\원본\우재

filterPath = os.getcwd() + "\\extract\\주현\\face"
# C:\Users\memoming\DegreeProject\Github\Python\Data\extract\우재\face_sample

savePath   = os.getcwd() + "\\extract\\주현\\nonface"
# C:\Users\memoming\DegreeProject\Github\Python\Data\extract\우재\nonface


find_and_move_anotherFile_inPath( targetPath, filterPath, savePath )
