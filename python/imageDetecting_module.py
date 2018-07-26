import cv2
import os

class imageDetector :
    def __init__ (self) :
        self.faceCascade    = cv2.CascadeClassifier("haarcascades" + os.sep + "haarcascade_frontalface_alt.xml")
        self.imgCnt         = 1

    def faceDetect (self, img_path, save_path, size, symbol="face_img", show=False, save=False) :
        img     = cv2.imread(img_path)
        img     = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        faces   = self.faceCascade.detectMultiScale (
                        img,
                        scaleFactor=1.3,
                        minNeighbors=3,
                        minSize=(30, 30)
                  )
        if (show) :
            print ("Found {0} faces!".format(len(faces)))
            for (x, y, w, h) in faces:
                cv2.rectangle(img, (x, y), (x+w, y+h), (0, 255, 0), 2)
                # cv2.circle(image, (int(x+w/2),int(y+h/2)), int(w/2), (0,255,0), 2)
            cv2.imshow("Faces found", img)
            cv2.waitKey(0)
            cv2.destroyAllWindows()
            cv2.waitKey(1)

        if (save) :
            for (x, y, w, h) in faces:
                one_face_img    = img[y:y+h,x:x+w] # 얼굴만 잘라내기
                one_face_img    = cv2.resize( one_face_img,(size,size) )
                saveFileName    = symbol+"_"+str(self.imgCnt)+".jpg"
                cv2.imwrite( save_path + os.sep + saveFileName, one_face_img )
                print("Saved",saveFileName)
                self.imgCnt += 1
            cv2.destroyAllWindows()

if (__name__ == "__main__") :
    fileList    = os.listdir(".."+os.sep+"data"+os.sep+"woojae")
    imgDetector = imageDetector()
    for eachFile in fileList :
        img_path    = ".."+os.sep+"data"+os.sep+"woojae"+os.sep+ eachFile
        imgDetector.faceDetect(img_path=img_path, save_path=".."+os.sep+"data"+os.sep+"woojae_face_48", size=48, save=True)
