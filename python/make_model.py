import tensorflow as tf
import matplotlib.image as mpimg
import numpy as np

# batch_size  = 128
img_size    = 28 #48
num_classes = 1

def init_weights(shape):
    return tf.Variable(tf.random_normal(shape, stddev=0.01))

X           = tf.placeholder( tf.float32, shape=[None,28,28,1],name="input")
Y_Label     = tf.placeholder( tf.float32, shape=[None,1])

# Fillter  a,b,c,d 일경우 a,b는 size , c는 색상 , d는 필터의 개수를 의미한다
kernel1     = tf.Variable( tf.truncated_normal(shape=[4,4,1,4], stddev=0.1) )
bias1       = tf.Variable( tf.truncated_normal(shape=[4], stddev=0.1 ) )
conv1       = tf.nn.conv2d(X, kernel1, strides=[1,1,1,1], padding="SAME" ) + bias1
activation1 = tf.nn.relu(conv1)
pool1       = tf.nn.max_pool(activation1, ksize=[1,2,2,1], strides=[1,2,2,1], padding="SAME" )

kernel2     = tf.Variable( tf.truncated_normal(shape=[4,4,4,8], stddev=0.1) )
bias2       = tf.Variable( tf.truncated_normal(shape=[8], stddev=0.1 ) )
conv2       = tf.nn.conv2d(pool1, kernel2, strides=[1,1,1,1], padding="SAME" ) + bias2
activation2 = tf.nn.relu(conv2)
pool2       = tf.nn.max_pool(activation1, ksize=[1,2,2,1], strides=[1,2,2,1], padding="SAME" )

W1          = tf.Variable(tf.truncated_normal(shape=[8*7*7,1]) )
B1          = tf.Variable(tf.truncated_normal(shape=[1]) )
pool2_flat  = tf.reshape(pool2,[-1,8*7*7])
output_layer = tf.matmul(pool2_flat,W1)+B1

loss        = tf.reduce_mean(tf.nn.softmax_cross_entropy_with_logits(labels=Y_Label, logits=output_layer))
train_step  = tf.train.AdamOptimizer(0.005).minimize(loss)

correct_prediction  = tf.equal(tf.argmax(output_layer, 1), tf.argmax(Y_Label, 1))
accuracy            = tf.reduce_mean(tf.cast(correct_prediction, tf.float32))


# ---------------------------------------------------------

X_in  = list()
label = [ 1, 1, 1, 1, 1, 1,\
        1, 1, 1, 1, 1, 1,\
        1, 1, 1, 1, 1, 1,\
        1, 1, 1, 1, 1, 1,\
        1, 1, 1, 1, 1, 1,\
        1, 1, 1, 1, 1, 1,\
        1, 1, 1, 1, 1, 1,\
        1, 1, 1, 1, 1, 1,\
        1, 1, 1, 1, 1, 1,\
        1, 1, 1, 1, 1, 1]
label = np.resize(label,(60,1))


for i in range(30) :
    t_img = mpimg.imread("./Image/test_img_"+str(i)+".jpg")
    t_imp = np.resize(t_img, (28,28,1) ) #48  48 1
    X_in.append(t_imp)


with tf.Session() as sess :
    print("Start !")

    saver = tf.train.Saver(tf.all_variables())
    # ckpt = tf.train.get_checkpoint_state(FLAGS.logs_dir)

    sess.run(tf.global_variables_initializer())
    for i in range( 10000 ) :
        trainingData = X_in
        Y = label
        sess.run(train_step, feed_dict={X:trainingData, Y_Label:Y})
        if (i%100 == 0) :
            print(str(i)+" ... training")
    print("Model Created !")

    saver.save(sess, "./Model/" + 'learningModel.ckpt')




    print(" Model testing....! ")
    img = mpimg.imread("./Image/test_img_11.jpg")
    img = [np.reshape(img,(28,28,1))]
    rlt = sess.run(output_layer,feed_dict={X:img})
    print(rlt)

    img3 = mpimg.imread("./Image/test_img_1.jpg")
    img3 = [np.reshape(img3,(28,28,1))]
    rlt3 = sess.run(output_layer,feed_dict={X:img3})
    print(rlt3)

    img2 = mpimg.imread("./Image2/test_img_11.jpg")
    img2 = [np.reshape(img2,(28,28,1))]
    result2 = sess.run(output_layer, feed_dict={X:img2})
    print(result2)
