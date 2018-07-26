import tensorflow as tf
import matplotlib.image as mpimg
import numpy as np
import os
import csv
from memoming_tools import csv_batch_container

batch_size  = 128
img_size    = 48
num_classes = 2

X     = tf.placeholder( tf.float32, shape=[None,48,48,1], name="input")
Y     = tf.placeholder( tf.float32, shape=[None,2])
is_training = tf.placeholder( tf.bool )

# kernel1     = tf.Variable( tf.truncated_normal(shape=[3,3,1,32], stddev=0.01) )
# bias1       = tf.Variable( tf.truncated_normal(shape=[4], stddev=0.01 ) )
# conv1       = tf.nn.conv2d(X, kernel1, strides=[1,1,1,1], padding="SAME" ) + bias1
# activation1 = tf.nn.relu(conv1)
# pool1       = tf.nn.max_pool(activation1, ksize=[1,2,2,1], strides=[1,2,2,1], padding="SAME" )

# Convolutional Layer #1
conv1_layers    = tf.layers.conv2d ( inputs=X, filters=32, kernel_size=[3,3], padding="SAME", activation=tf.nn.relu, use_bias=True )
pool1_layers    = tf.layers.max_pooling2d( inputs=conv1_layers, pool_size=[2,2], padding="SAME", strides=2 )
dropout1_layers = tf.layers.dropout( inputs=pool1_layers, rate=0.7, training=is_training )


# # kernel2     = tf.Variable( tf.truncated_normal(shape=[4,4,4,64], stddev=0.01) )
# # bias2       = tf.Variable( tf.truncated_normal(shape=[8], stddev=0.01 ) )
# # conv2       = tf.nn.conv2d(pool1, kernel2, strides=[1,1,1,1], padding="SAME" ) + bias2
# # activation2 = tf.nn.relu(conv2)
# # pool2       = tf.nn.max_pool(activation1, ksize=[1,2,2,1], strides=[1,2,2,1], padding="SAME" )
#
#
# # Convolutional Layer #2
conv2_layers    = tf.layers.conv2d ( inputs=dropout1_layers, filters=64, kernel_size=[3,3], padding="SAME", activation=tf.nn.relu, use_bias=True )
pool2_layers    = tf.layers.max_pooling2d( inputs=conv2_layers, pool_size=[2,2], padding="SAME", strides=2 )
dropout2_layers = tf.layers.dropout( inputs=pool2_layers, rate=0.7, training=is_training )
#
#
# # kernel3     = tf.Variable( tf.truncated_normal(shape=[4,4,4,64], stddev=0.01) )
# # bias3       = tf.Variable( tf.truncated_normal(shape=[8], stddev=0.01 ) )
# # conv3       = tf.nn.conv2d(pool1, kernel2, strides=[1,1,1,1], padding="SAME" ) + bias2
# # activation3 = tf.nn.relu(conv2)
# # pool3       = tf.nn.max_pool(activation1, ksize=[1,2,2,1], strides=[1,2,2,1], padding="SAME" )
#
#
# # Convolutional Layer #3
conv3_layers    = tf.layers.conv2d ( inputs=dropout2_layers, filters=128, kernel_size=[3,3], padding="SAME", activation=tf.nn.relu, use_bias=True )
pool3_layers    = tf.layers.max_pooling2d( inputs=conv3_layers, pool_size=[2,2], padding="SAME", strides=2 )
dropout3_layers = tf.layers.dropout( inputs=pool3_layers, rate=0.7, training=is_training )
#
#
# # W1          = tf.Variable(tf.truncated_normal(shape=[8*7*7,1]) )
# # B1          = tf.Variable(tf.truncated_normal(shape=[1]) )
# # pool2_flat  = tf.reshape(pool2,[-1,8*7*7])
# # output_layer = tf.matmul(pool2_flat,W1)+B1
#
#
flat        = tf.contrib.layers.flatten( dropout3_layers )
dense4      = tf.layers.dense(inputs=flat, units=625, activation=tf.nn.relu ) #625 이부분을 뭘로해야할지 모르겟다.
dropout4    = tf.layers.dropout( inputs=dense4, rate=0.5, training=is_training )

logits      = tf.layers.dense(inputs=dropout4, units=2, activation=tf.nn.relu )

cost        = tf.reduce_mean( tf.nn.softmax_cross_entropy_with_logits_v2(labels=Y, logits=logits) )
optimizer   = tf.train.AdamOptimizer(0.005).minimize(cost)



def training_model ( sess, x_data, y_data ):
    return sess.run( [ cost,  optimizer], feed_dict={ X: x_data,  Y:y_data,  is_training:True} )


training_epochs = 10000

with tf.Session() as sess :
    sess.run(tf.global_variables_initializer())

    for epoch in range(training_epochs):
        avg_cost = 0
        train = csv_batch_container( "train_data.csv" )
        total_batch = round((train.num_examples() / batch_size) + 0.5 ) # 반올림 처리

        for i in range(total_batch):
            batch_xl, batch_yl = train.next_batch(batch_size)
            batch_xs = list()
            batch_ys = list()

            for j in range(batch_size) :
                t_img = mpimg.imread( "extract" + os.sep + "dataset" + os.sep + batch_xl[j] )
                t_imp = np.resize(t_img, (48,48,1) )
                batch_xs.append(t_imp)
                t_ls  = [0,0]
                t_ls[ int(batch_yl[j]) ] = 1
                batch_ys.append( t_ls )

            # Train
            c, _ = training_model ( sess, batch_xs, batch_ys )
            avg_cost += c / total_batch

        print('Epoch: ', '%04d' %(epoch + 1), 'Cost = ', avg_cost)
    print('Training Finished....! \n\n')

    saver       = tf.train.Saver()
    save_path   = saver.save(sess, "saved_model" + os.sep + 'face_cnn.ckpt')
    print("Model saved to %s" % save_path)



# def predict(sess, x_test, training = is_training):
#
#     return sess.run( logits, feed_dict={ X : x_test,  training:training})
#
# def get_accuracy(sess, x_test, y_test, training = is_training):
#     return sess.run( accuracy, feed_dict={ X: x_test,  Y : y_test,  training: training})
#
# # correct_prediction  = tf.equal(tf.argmax(output_layer, 1), tf.argmax(Y_Label, 1))
# # accuracy            = tf.reduce_mean(tf.cast(correct_prediction, tf.float32))
#
# correct_prediction  = tf.equal( tf.argmax(logits, 1), tf.argmax(Y, 1) )
# accuracy            = tf.reduce_mean( tf.cast(correct_prediction, tf.float32) )








#
#
#
#
# # ───────────────────────────────────────────────────────────────
#
#
# loss        = tf.reduce_mean(tf.nn.softmax_cross_entropy_with_logits(labels=Y_Label, logits=output_layer))
# train_step  = tf.train.AdamOptimizer(0.005).minimize(loss)
#
# correct_prediction  = tf.equal(tf.argmax(output_layer, 1), tf.argmax(Y_Label, 1))
# accuracy            = tf.reduce_mean(tf.cast(correct_prediction, tf.float32))
#
#
# # ---------------------------------------------------------
#
# X_in  = list()
# label = [ 1, 1, 1, 1, 1, 1,\
#         1, 1, 1, 1, 1, 1,\
#         1, 1, 1, 1, 1, 1,\
#         1, 1, 1, 1, 1, 1,\
#         1, 1, 1, 1, 1, 1,\
#         1, 1, 1, 1, 1, 1,\
#         1, 1, 1, 1, 1, 1,\
#         1, 1, 1, 1, 1, 1,\
#         1, 1, 1, 1, 1, 1,\
#         1, 1, 1, 1, 1, 1]
# label = np.resize(label,(60,1))
#
#
# for i in range(30) :
#     t_img = mpimg.imread("./Image/test_img_"+str(i)+".jpg")
#     t_imp = np.resize(t_img, (28,28,1) ) #48  48 1
#     X_in.append(t_imp)
#
#
# with tf.Session() as sess :
#     print("Start !")
#
#     saver = tf.train.Saver(tf.all_variables())
#     # ckpt = tf.train.get_checkpoint_state(FLAGS.logs_dir)
#
#     sess.run(tf.global_variables_initializer())
#     for i in range( 10000 ) :
#         trainingData = X_in
#         Y = label
#         sess.run(train_step, feed_dict={X:trainingData, Y_Label:Y})
#         if (i%100 == 0) :
#             print(str(i)+" ... training")
#     print("Model Created !")
#
#     saver.save(sess, "./Model/" + 'learningModel.ckpt')
#
#
#
#
#     print(" Model testing....! ")
#     img = mpimg.imread("./Image/test_img_11.jpg")
#     img = [np.reshape(img,(28,28,1))]
#     rlt = sess.run(output_layer,feed_dict={X:img})
#     print(rlt)
#
#     img3 = mpimg.imread("./Image/test_img_1.jpg")
#     img3 = [np.reshape(img3,(28,28,1))]
#     rlt3 = sess.run(output_layer,feed_dict={X:img3})
#     print(rlt3)
#
#     img2 = mpimg.imread("./Image2/test_img_11.jpg")
#     img2 = [np.reshape(img2,(28,28,1))]
#     result2 = sess.run(output_layer, feed_dict={X:img2})
#     print(result2)
