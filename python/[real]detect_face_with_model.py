import tensorflow as tf
import numpy as np
import cv2
import os
import csv
from tensorflow.python.tools import inspect_checkpoint as chkp
import matplotlib.image as mpimg
from memoming_tools import csv_batch_container



def is_face_with_model ( img ) :
    with tf.Session() as sess:
        saver = tf.train.import_meta_graph("saved_model" + os.sep + "face_cnn.ckpt.meta")
        saver.restore(sess , tf.train.latest_checkpoint("saved_model" + os.sep))

        graph       = tf.get_default_graph()
        logits      = graph.get_tensor_by_name("logits_0/BiasAdd:0")
        X_in        = graph.get_tensor_by_name("input_0:0")
        is_training = graph.get_tensor_by_name("is_training_0:0")

        #####################
        # Use Trained Model #
        #####################
        batch_xs    = np.resize(img, (1,48,48,1) )
        prediction  = sess.run(logits, feed_dict={X_in:batch_xs, is_training:False})
        return np.argmax(prediction)




if ( __name__ == "__main__" ) :
    faceCascade = cv2.CascadeClassifier('haarcascades/haarcascade_frontalface_default.xml')
    image       = cv2.imread('test_img2.jpg')
    gray        = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

    blur_kernel = np.ones( (6,6), np.float32 )/25

    faces = faceCascade.detectMultiScale(
        gray,
        scaleFactor=1.01, # 1.1 하며 26 , 1.01 하면 56
        minNeighbors=1,
        minSize=(30, 30)
    )
    print ("Found {0} faces!".format(len(faces)))
    for (x, y, w, h) in faces:
        each_img = image[y:y+h,x:x+w] # 얼굴만 잘라내기
        each_img = cv2.cvtColor(each_img, cv2.COLOR_BGR2GRAY)
        each_img = cv2.resize(each_img,(48,48))
        if ( is_face_with_model(each_img) ) :
            cv2.rectangle(image, (x, y), (x+w, y+h), (0, 255, 0), 2)

    # cv2.imwrite("./test_img_with_model.jpg",image)

        # a = cv2.circle(image, (int(x+w/2),int(y+h/2)), int(w/2), (0,255,0), 2)
        # image[y:y+h,x:x+w] = cv2.filter2D(image[y:y+h,x:x+w], -1, blur_kernel)

    cv2.imshow("Faces found", image)
    cv2.waitKey(0)
    cv2.destroyAllWindows()
    cv2.waitKey(1)
