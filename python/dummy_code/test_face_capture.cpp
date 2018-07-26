#include "opencv/cv.h"
#include "opencv/highgui.h"
#include <iostream>
#include <time.h>
#include <csignal>

clock_t start_point,end_point;
bool stop=false;
void sigIntHandler(int signal){
	stop=true;
}
using namespace cv;
using namespace std;

int main(int argc, char *argv[])
{

	char buffer[40];
	double process_time;
	int cnt=0;

	cv::VideoCapture capture(0);
        cv::Mat frame_original;
        cv::Mat frame;
        cv::Mat grayframe;
    	cv::CascadeClassifier face_classifier;
    	face_classifier.load("haarcascade_frontalface_default.xml");
    	signal(SIGINT,sigIntHandler);

    	while (!stop) {

	    	start_point=clock();
       		cnt++;

            	capture >> frame_original; // get a new frame from webcam
            	cv::resize(frame_original,frame,cv::Size(frame_original.cols/2,
                frame_original.rows/2),0,0,CV_INTER_NN); // downsample 1/2x
                cv::cvtColor(frame, grayframe, CV_BGR2GRAY);
                cv::equalizeHist(grayframe,grayframe);

                std::vector<cv::Rect> faces;

                face_classifier.detectMultiScale(grayframe, faces,
                    1.1, // increase search scale by 10% each pass
                    3,   // merge groups of three detections
                    CV_HAAR_FIND_BIGGEST_OBJECT|CV_HAAR_SCALE_IMAGE,
                    cv::Size(30,30));

                for(int i=0; i<faces.size(); i++) {
                    cv::Point lb(faces[i].x + faces[i].width,
                                 faces[i].y + faces[i].height);
                    cv::Point tr(faces[i].x, faces[i].y);

                    cv::rectangle(frame, lb, tr, cv::Scalar(0,255,0), 3, 4, 0);
                }

		sprintf(buffer,"/home/root/face.jpg");
		imwrite(buffer,frame);
		end_point=clock();
		process_time=((double)(end_point-start_point)/(CLOCKS_PER_SEC/1000));
		printf("Exe time:%04f msec. %03.1f frames. saved image:%d\n",process_time,1000./process_time,cnt);
    }

    // VideoCapture automatically deallocate camera object
    return 0;
}
cc = g++
OBJECT = facetest.o
TARGET = facetest
CFLAGS =
LDFLAGS = -lopencv_calib3d -lopencv_contrib -lopencv_core -lopencv_features2d -lopencv_flann -lopencv_gpu -lopencv_highgui -lopencv_imgproc -lopencv_legacy -lopencv_ml -lopencv_nonfree -lopencv_objdetect -lopencv_ocl -lopencv_photo -lopencv_stitching -lopencv_superres -lopencv_video -lopencv_videostab -lrt -lpthread -lm -ldl
$(TARGET): $(OBJECT)
        $(cc) $(LDFLAGS) -o $@ $(OBJECT)
%.o:%.c
        $(cc) $(CCFLAGS) -c -o $@ $<
clean:
        rm -f *.o $(TARGET)
