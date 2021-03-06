import tensorflow as tf
import matplotlib.image as mpimg
import numpy as np
import os
import csv
import sys
sys.path.insert(0, '..\\..\\..\\memomingTools')
from memomingTools import csv_batch_container

batch_size      = 32
training_epochs = 100
img_size        = 48
num_classes     = 2

#                                            shape = [ batch_size, 가로픽셀, 세로픽셀, 채널 ]
X               = tf.placeholder( tf.float32, shape=[None,img_size,img_size,1], name="input_0")
#                                            shape = [ batch_size, 128개 얼굴 특징점 ]
Y               = tf.placeholder( tf.float32, shape=[None,128])
is_training     = tf.placeholder( tf.bool, name="is_training_0" )

# Convolutional Layer #1
conv1_layers    = tf.layers.conv2d ( inputs=X, filters=32, kernel_size=[3,3], padding="SAME", activation=tf.nn.relu, use_bias=True )
pool1_layers    = tf.layers.max_pooling2d( inputs=conv1_layers, pool_size=[2,2], padding="SAME", strides=2 )
dropout1_layers = tf.layers.dropout( inputs=pool1_layers, rate=0.5, training=is_training )

# # Convolutional Layer #2
conv2_layers    = tf.layers.conv2d ( inputs=dropout1_layers, filters=64, kernel_size=[3,3], padding="SAME", activation=tf.nn.relu, use_bias=True )
pool2_layers    = tf.layers.max_pooling2d( inputs=conv2_layers, pool_size=[2,2], padding="SAME", strides=2 )
dropout2_layers = tf.layers.dropout( inputs=pool2_layers, rate=0.5, training=is_training )

# # Convolutional Layer #3
conv3_layers    = tf.layers.conv2d ( inputs=dropout2_layers, filters=128, kernel_size=[3,3], padding="SAME", activation=tf.nn.relu, use_bias=True )
pool3_layers    = tf.layers.max_pooling2d( inputs=conv3_layers, pool_size=[2,2], padding="SAME", strides=2 )
dropout3_layers = tf.layers.dropout( inputs=pool3_layers, rate=0.5, training=is_training )

flat            = tf.contrib.layers.flatten( dropout3_layers )
dense4          = tf.layers.dense(inputs=flat, units=625, activation=tf.nn.relu ) #625 이부분을 뭘로해야할지 모르겟다.
dropout4        = tf.layers.dropout( inputs=dense4, rate=0.5, training=is_training )

logits          = tf.layers.dense(inputs=dropout4, units=2, activation=None, use_bias=True, name="logits_0" )
cost            = tf.reduce_mean( tf.nn.softmax_cross_entropy_with_logits_v2(labels=Y, logits=logits) )
optimizer       = tf.train.AdamOptimizer(0.0005).minimize(cost)
saver           = tf.train.Saver()

def training_model ( sess, x_data, y_data ):
    return sess.run( [ cost,  optimizer], feed_dict={ X: x_data,  Y:y_data,  is_training:True} )

with tf.Session() as sess :
    sess.run(tf.global_variables_initializer())

    for epoch in range(training_epochs):
        avg_cost = 0
        path = "..\\..\\data\\individual_model\\"
        train = csv_batch_container( path + "train_data.csv" )
        total_batch = round((train.num_examples() / batch_size) + 0.5 ) # 반올림 처리

        for i in range(total_batch):
            batch_xl, batch_yl = train.next_batch(batch_size)
            batch_xs = list()
            batch_ys = list()

            for j in range(len(batch_xl)) :
                t_img = mpimg.imread( path + "dummy_data_big\\" + batch_xl[j] )
                t_imp = np.resize(t_img, (img_size,img_size,1) )
                batch_xs.append(t_imp)
                t_ls  = [0,0]
                t_ls[ int(batch_yl[j]) ] = 1
                batch_ys.append( t_ls )

            # Train
            c, _ = training_model ( sess, batch_xs, batch_ys )
            avg_cost += c / total_batch

        print('Epoch: ', '%04d' %(epoch + 1), 'Cost = ', avg_cost)
    print('Training Finished....! \n\n')


    save_path   = saver.save( sess, "saved_model" + os.sep + 'woojae_model.ckpt' )
    print("Model saved to %s" % save_path)


    # Testing
    batch_size      = 64
    correct         = 0
    csv_container   = csv_batch_container(path + "test_data.csv")
    total_batch     = round((csv_container.num_examples() / batch_size) + 0.5 ) # 반올림 처리

    batch_xl, batch_yl = csv_container.next_batch(batch_size)
    batch_xs = list()
    batch_ys = list()

    for j in range(len(batch_xl)) :
        t_img = mpimg.imread( path + "dummy_data_big\\"+ batch_xl[j] )
        t_imp = np.resize(t_img, (img_size,img_size,1) )
        batch_xs.append(t_imp)
        t_ls  = [0,0]
        t_ls[ int(batch_yl[j]) ] = 1
        batch_ys.append( t_ls )

    prediction_list = sess.run(logits, feed_dict={X:batch_xs, is_training:False})

    for i in range(len(prediction_list)) :
        print("Predict :",np.argmax(prediction_list[i]),"Label :",np.argmax(batch_ys[i]))
        if (np.argmax(prediction_list[i]) == np.argmax(batch_ys[i])) :
            correct += 1

    print("Accuracy :",correct/batch_size*100,"%")
