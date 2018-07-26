import sys
import dlib
import cv2

# Take the image file name from the command line
# file_name = sys.argv[1]
file_name = "test11.jpg"

# Create a HOG face detector using the built-in dlib class
face_detector = dlib.get_frontal_face_detector()

# win = dlib.image_window()

# Load the image into an array
image = cv2.imread(file_name)

# Run the HOG face detector on the image data.
# The result will be the bounding boxes of the faces in our image.
detected_faces = face_detector(image, 1)

print("I found {} faces in the file {}".format(len(detected_faces), file_name))

# Open a window on the desktop showing the image
# win.set_image(image) # CODE

# Loop through each face we found in the image
for i, face_rect in enumerate(detected_faces):
	# Detected faces are returned as an object with the coordinates
	# of the top, left, right and bottom edges
	# print("- Face #{} found at Left: {} Top: {} Right: {} Bottom: {}".format(i, face_rect.left(), face_rect.top(), face_rect.right(), face_rect.bottom()))
    cv2.rectangle(image, (face_rect.left(), face_rect.top()), (face_rect.right(), face_rect.bottom()), (0, 255, 0), 8)
	# Draw a box around each face we found
	# win.add_overlay(face_rect) #CODE

cv2.imwrite("new_test11.jpg",image)

# Wait until the user hits <enter> to close the window
dlib.hit_enter_to_continue()
cv2.destroyAllWindows()
