import numpy as np
import cv2

faceCascade = cv2.CascadeClassifier("haarcascades\\haarcascade_frontalface_default.xml")
img_path = "..\\data\\woojae\\KakaoTalk_Moim_4ynN6RdCQvhoX2XnRvk2oUIujqBXfr.jpg"
image = cv2.imread(img_path)
gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

blur_kernel = np.ones( (6,6), np.float32 )/25

faces = faceCascade.detectMultiScale (
    gray,
    scaleFactor=1.1,
    minNeighbors=3,
    minSize=(30, 30)
)
print ("Found {0} faces!".format(len(faces)))

for (x, y, w, h) in faces:
    # test_img = image[y:y+h,x:x+w] # 얼굴만 잘라내기
    # test_img = cv2.cvtColor(test_img, cv2.COLOR_BGR2GRAY)
    # test_img = cv2.resize(test_img,(28,28))

    cv2.rectangle(image, (x, y), (x+w, y+h), (0, 255, 0), 2)

    # cv2.circle(image, (int(x+w/2),int(y+h/2)), int(w/2), (0,255,0), 2)
    # image[y:y+h,x:x+w] = cv2.filter2D(image[y:y+h,x:x+w], -1, blur_kernel)

cv2.imwrite("new_test.jpg",image)
# cv2.waitKey(0)
cv2.destroyAllWindows()
# cv2.waitKey(1)
