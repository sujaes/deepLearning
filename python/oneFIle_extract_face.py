import cv2
import os

def extract_face( imgPath, pixel, fileName ) :
    img         = cv2.imread(imgPath)
    gray_img    = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY) # gray scale
    faceCascade = cv2.CascadeClassifier("./haarcascades/haarcascade_frontalface_default.xml")
    faces       = faceCascade.detectMultiScale(
                        gray_img,
                        scaleFactor=1.1,
                        minNeighbors=1,
                        minSize=(30, 30)
                )
    print("Face dected !")
    print ("Found {0} faces!".format(len(faces)))

    i = 0
    for (x, y, w, h) in faces:
        face_img = gray_img[y:y+h,x:x+w] # 얼굴만 잘라내기
        face_img = cv2.resize( face_img,(pixel,pixel) )
        cv2.imwrite("./extract/" + fileName + "_face_"+str(i)+".jpg", face_img )
        i += 1

        cv2.rectangle(img, (x, y), (x+w, y+h), (0, 255, 0), 2)
    cv2.imshow("Faces found", img)
    cv2.waitKey(0)

    cv2.destroyAllWindows()


# ─────────────────────────────────────────────────────────────────────────────────────────

# extract_face( 'test_img.jpg', 28, "test_img" )

def find_targetFiles_inPath ( folderPath, targetFormat ) :
    contentsList = os.listdir( folderPath )
    for item in contentsList :
        itemPath = os.path.join( folderPath, item )
        if ( os.path.isdir(itemPath) ):
            find_targetFiles_inPath( itemPath, targetFormat )
        else :
            if ( item.endswith(targetFormat) ) :
                extract_face( itemPath, 48, item[:-4] )
            else :
                continue

# dirPath = os.getcwd() + "\\dummy"
# find_targetFiles_inPath(dirPath , "jpg")

test_path = os.getcwd() + "\\test_img.jpg"
extract_face(test_path , 300, "test_img")
# find_targetFiles_inPath(test_path , "jpg")
