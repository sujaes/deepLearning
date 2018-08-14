import os
import shutil

def unzip_file_inPath ( targetPath, savePath ) :

    dirList = os.listdir(targetPath)

    for item in dirList :
        checkPath = os.path.join(targetPath,item)
        if ( os.path.isdir(checkPath) ) :
            unzip_file_inPath( checkPath, savePath )
        else :
            shutil.move( checkPath, savePath )
            print(item," 이 옮겨졌습니다.")



if ( __name__ == "__main__" ) :
    targetPath = "C:/Users/memoming/works/DegreeProject_OnlyYou/data/dummy/lfw"
    savePath = "C:/Users/memoming/works/DegreeProject_OnlyYou/data/dummy/unzip"

    unzip_file_inPath(targetPath=targetPath, savePath=savePath)
