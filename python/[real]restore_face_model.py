from tensorflow.python.tools import inspect_checkpoint as chkp
import os
import tensorflow as tf
import matplotlib.image as mpimg
import numpy as np
import csv
from memoming_tools import csv_batch_container

# ckpt_path = "saved_model" + os.sep + "face_cnn.ckpt"
# ckpt_path_test = "saved_model" + os.sep + "checkpoint"
# chkp.print_tensors_in_checkpoint_file(ckpt_path, tensor_name='', all_tensors=True, all_tensor_names=False)

with tf.Session() as sess:
    saver = tf.train.import_meta_graph("saved_model" + os.sep + "face_cnn.ckpt.meta")
    saver.restore(sess , tf.train.latest_checkpoint("saved_model" + os.sep))

    graph       = tf.get_default_graph()
    logits      = graph.get_tensor_by_name("logits_0/BiasAdd:0")
    X_in        = graph.get_tensor_by_name("input_0:0")
    is_training = graph.get_tensor_by_name("is_training_0:0")

    ##################3
    # Testing
    csv_container   = csv_batch_container("test_data.csv")
    # batch_size      = 849
    batch_size      = csv_container.num_examples()
    correct         = 0
    # total_batch     = round((csv_container.num_examples() / batch_size) + 0.5 ) # 반올림 처리
    # total_batch     = csv_container.num_examples()

    batch_xl, batch_yl = csv_container.next_batch(batch_size)
    batch_xs = list()
    batch_ys = list()

    for j in range(len(batch_xl)) :
        t_img = mpimg.imread( "extract" + os.sep + "dataset" + os.sep + batch_xl[j] )
        t_imp = np.resize(t_img, (48,48,1) )
        batch_xs.append(t_imp)
        t_ls  = [0,0]
        t_ls[ int(batch_yl[j]) ] = 1
        batch_ys.append( t_ls )

    prediction_list = sess.run(logits, feed_dict={X_in:batch_xs, is_training:False})

    for i in range(len(prediction_list)) :
        print("Predict :",np.argmax(prediction_list[i]),"Label :",np.argmax(batch_ys[i]))
        if (np.argmax(prediction_list[i]) == np.argmax(batch_ys[i])) :
            correct += 1

    print("Accuracy :",correct/batch_size*100,"%")
