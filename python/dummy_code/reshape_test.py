import numpy as np
import cv2


def resizeImg ( img, x, y ) :
    return cv2.resize( img,(x,y) )

# faceCascade = cv2.CascadeClassifier('haarcascades/haarcascade_frontalface_default.xml')
faceCascade = cv2.CascadeClassifier("C:/Users/memoming/DegreeProject/OpenCV/Download/opencv/sources\/data/haarcascades/haarcascade_frontalface_default.xml")
image = cv2.imread('test_img.jpg')
newImage = resizeImg(image,28,28)
cv2.imwrite("resize_test_img.jpg",newImage)
gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
faces = faceCascade.detectMultiScale(
    gray,
    scaleFactor=1.1,
    minNeighbors=1,
    minSize=(30, 30)
    # flags = cv2.cv.CV_HAAR_SCALE_IMAGE
)
print ("Found {0} faces!".format(len(faces)))
for (x, y, w, h) in faces:
    cv2.rectangle(image, (x, y), (x+w, y+h), (0, 255, 0), 2)

cv2.imshow("Faces found", image)
cv2.waitKey(0)
cv2.destroyAllWindows()
cv2.waitKey(1)
