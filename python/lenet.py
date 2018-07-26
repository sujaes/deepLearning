import tensorflow as tf


batch_size      = 128
training_epochs = 1
img_size        = 32
num_classes     = 2


X               = tf.placeholder( tf.float32, shape=[None,img_size,img_size,1], name="input_0")
Y               = tf.placeholder( tf.float32, shape=[None,2] dtype=)
is_training     = tf.placeholder( tf.bool, name="is_training_0" )

# Convolutional Layer #1
conv1_layers    = tf.layers.conv2d ( inputs=X, filters=6, kernel_size=[5,5], padding="SAME", activation=tf.nn.relu, use_bias=True )
pool1_layers    = tf.layers.max_pooling2d( inputs=conv1_layers, pool_size=[2,2], padding="SAME", strides=2 )
# dropout1_layers = tf.layers.dropout( inputs=pool1_layers, rate=0.5, training=is_training )


# # Convolutional Layer #2
conv2_layers    = tf.layers.conv2d ( inputs=dropout1_layers, filters=16, kernel_size=[5,5], padding="SAME", activation=tf.nn.relu, use_bias=True )
pool2_layers    = tf.layers.max_pooling2d( inputs=conv2_layers, pool_size=[2,2], padding="SAME", strides=2 )
# dropout2_layers = tf.layers.dropout( inputs=pool2_layers, rate=0.5, training=is_training )


flat            = tf.contrib.layers.flatten( dropout3_layers )
dense           = tf.layers.dense(inputs=flat, units=84, activation=tf.nn.relu )
logits          = tf.layers.dense(inputs=dropout4, units=2, activation=tf.nn.relu )

cost        = tf.reduce_mean( tf.nn.softmax_cross_entropy_with_logits_v2(labels=Y, logits=logits) )
optimizer   = tf.train.AdamOptimizer(0.005).minimize(cost)

def training_model ( sess, x_data, y_data ):
    return sess.run( [ cost,  optimizer], feed_dict={ X: x_data,  Y:y_data} )
