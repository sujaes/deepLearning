# This script will detect faces via your webcam.
# Tested with OpenCV3
import cv2
import signal
import sys
import time

def sigint_handler(signum,frame):
    sys.exit(0)

signal.signal(signal.SIGINT,sigint_handler)

prevTime    = 0
cap         = cv2.VideoCapture(0)
cap.set(3,320)
cap.set(4,240)

# Create the haar cascade
faceCascade = cv2.CascadeClassifier("C:/Users/memoming/DegreeProject/OpenCV/Download/opencv/sources/data/haarcascades/haarcascade_frontalface_default.xml")
i = 0
while(True):
    curTime = time.time()
	# Capture frame-by-frame
    ret,frame = cap.read()

	# Our operations on the frame come here
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)

	# Detect faces in the image
	# faces = faceCascade.detectMultiScale(
	# 	gray,
	# 	scaleFactor=1.1,
	# 	minNeighbors=5,
	# 	minSize=(30, 30)
	# 	#flags = cv2.CV_HAAR_SCALE_IMAGE
	# )

    faces = faceCascade.detectMultiScale(
        gray,
        scaleFactor=1.06,
        minNeighbors=5,
        minSize=(30, 30)
        # flags = cv2.cv.CV_HAAR_SCALE_IMAGE
    )


	# Draw a rectangle around the faces
    for (x, y, w, h) in faces:
        # cv2.rectangle(frame, (x, y), (x+w, y+h), (0, 255, 0), 2)
        # test_img = frame[y:y+h,x:x+w] # 얼굴만 잘라내기
        # cv2.imwrite("test_img_"+str(i)+".jpg",test_img)
        # i += 1

	# Display the resulting frame
        # cv2.imwrite("python_stream.jpg",frame)
       # print("saved")
        # sec=curTime-prevTime
        prevTime=curTime
        # fps=1/(sec)
        print("Found {0} faces!\t".format(len(faces))+"saved\t")
        test_img = frame[y:y+h,x:x+w] # 얼굴만 잘라내기
        test_img = cv2.cvtColor(test_img, cv2.COLOR_BGR2GRAY)
        test_img = cv2.resize(test_img,(48,48))
        cv2.imwrite("./Image5/test_img_"+str(i)+".jpg",test_img)
        i += 1
        # if (fps >= 2) :
        #     test_img = frame[y:y+h,x:x+w] # 얼굴만 잘라내기
        #     test_img = cv2.cvtColor(test_img, cv2.COLOR_BGR2GRAY)
        #     test_img = cv2.resize(test_img,(48,48))
        #     cv2.imwrite("./Image5/test_img_"+str(i)+".jpg",test_img)
        #     i += 1
          #  print "fps {0}".format(fps)

    #cv2.imshow('frame', frame)
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break
    if i==3000 :
        break



# When everything done, release the capture
cap.release()
#cv2.destroyAllWindows()
