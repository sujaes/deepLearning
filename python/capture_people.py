# This script will detect faces via your webcam.
# Tested with OpenCV3
import cv2
import signal
import sys
import time

def sigint_handler(signum,frame):
    sys.exit(0)

signal.signal(signal.SIGINT,sigint_handler)


cap         = cv2.VideoCapture(0)
cap.set(3,320)
cap.set(4,240)

# Create the haar cascade
faceCascade = cv2.CascadeClassifier("./haarcascades/haarcascade_frontalface_default.xml")
i = 0
while(True):

    ret,frame = cap.read()
	# Our operations on the frame come here
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
	# Detect faces in the image
    faces = faceCascade.detectMultiScale(
        gray,
        scaleFactor=1.06,
        minNeighbors=5,
        minSize=(30, 30)
    )


    for (x, y, w, h) in faces:
        print("Found {0} faces!\t".format(len(faces))+"saved\t")
        # test_img = frame[y:y+h,x:x+w] # 얼굴만 잘라내기
        # test_img = cv2.cvtColor(test_img, cv2.COLOR_BGR2GRAY)
        # test_img = cv2.resize(test_img,(128,128))
        # cv2.imwrite("./temp/test_img_"+str(i)+".jpg",test_img)
        cv2.imwrite("./temp/test_img_"+str(i)+".jpg",frame)
        i += 1

    if i==1000 :
        break


# When everything done, release the capture
cap.release()
#cv2.destroyAllWindows()
