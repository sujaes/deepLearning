import tensorflow           as tf
import matplotlib.image     as mpimg
import matplotlib.pyplot    as plt
import numpy                as np


X_img       = tf.placeholder( tf.float32, [None, 28, 28, 1] )
Y           = tf.placeholder( tf.float32, [None, 2] )

W1          = tf.Variable( tf.random_normal( [3, 3, 1, 32] , stddev=0.01 ) )
conv1       = tf.nn.conv2d( X_img, W1, strides=[1,1,1,1], padding="SAME" )
activate1   = tf.nn.relu(conv1)
pool1       = tf.nn.max_pool(activate1, ksize=[1,2,2,1], strides=[1,2,2,1], padding="SAME" ) # 14 by 14


W2          = tf.Variable( tf.random_normal( [3, 3, 32, 64] , stddev=0.01 ) )
conv2       = tf.nn.conv2d( pool1, W2, strides=[1,1,1,1], padding="SAME" )
activate2   = tf.nn.relu(conv2)
pool2       = tf.nn.max_pool(activate2, ksize=[1,2,2,1], strides=[1,2,2,1], padding="SAME" ) # 14 by 14

 # 위의 pool2에서 strides가 2니까 pool1이 14 by 14 이므로 padding을 주었어도 strides에 의해 7 by 7이 된다.
 # 이 과정은 Fully connected Layer에 넣기전에 펼쳐주는 작업이다.
pool2_reshape = tf.reshape( pool2, [-1, 7*7*64] )


W3 = tf.get_variable("W2", shape=[7*7*64, 2], initializer=tf.contrib.layers.xavier_initializer() )
b = tf.Variable( tf.random_normal([2]) )
hypothesis = tf.matmul(pool2_reshape,W3) + b

learning_rate = 0.005

cost = tf.reduce_mean( tf.nn.softmax_cross_entropy_with_logits(logits=hypothesis, labels=Y) )
optimizer = tf.train.AdamOptimizer( learning_rate = learning_rate ).minimize( cost )

training_epochs = 100
batch_size = 1

with tf.Session() as sess :
    sess.run( tf.global_variables_initializer() )

    print( "Learning started." )

    for epoch in range ( training_epochs ) :
        avg_cost = 0
        total_batch = 30

        for i in range(total_batch) :
            batch_x = mpimg.imread( "./Image/test_img_" + str(i) + ".jpg" ).reshape(-1,28,28,1)
            batch_y = [[1,0]]
            feed_dict = { X_img : batch_x , Y : batch_y }
            sess.run(optimizer, feed_dict = feed_dict )
            each_cost = sess.run( cost, feed_dict = feed_dict )
            avg_cost += each_cost/total_batch
        print( "Epoch : " , '%04d' % (epoch + 1), "Cost : ", '{:9f}'.format(avg_cost) )
    print( "Learning Finished!" )



    # Check
    correct_prediction = tf.equal(tf.argmax(hypothesis, 1), tf.argmax(Y,1))
    accuracy = tf.reduce_mean(tf.cast(correct_prediction, tf.float32))
    test_x = mpimg.imread( "./Image2/test_img_0.jpg" ).reshape(-1,28,28,1)
    test_y = [[0,1]]
    print("accuracy : ", sess.run(accuracy , feed_dict={X_img:test_x, Y:test_y}))
