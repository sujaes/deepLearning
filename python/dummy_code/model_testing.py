import tensorflow as tf
import matplotlib.image as mpimg
import numpy as np


sess = tf.InteractiveSession()
new_saver = tf.train.import_meta_graph("./Model/learningModel.ckpt.meta")
new_saver.restore(sess, tf.train.latest_checkpoint('./Model/'))
tf.get_default_graph().as_graph_def()

x = sess.graph.get_tensor_by_name("input:0")
y = sess.graph.get_tensor_by_name("output:0")

img = mpimg.imread("./Image1/test_img_11.jpg")
img = [np.reshape(img,(28,28,1))]
result = sess.run(y, feed_dict={x:img})

img2 = mpimg.imread("./Image2/test_img_11.jpg")
img2 = [np.reshape(img,(28,28,1))]
result2 = sess.run(y, feed_dict={x:img})

print(result)
print(result2)
