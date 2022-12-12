#include <jni.h>
#include <android/log.h>
#include <opencv2/opencv.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>

#define TAG "NativeLib"

using namespace std;

void orb(cv::Mat &mat);
void fast(cv::Mat &mat);
void sift(cv::Mat &mat);

extern "C" {
void JNICALL
Java_com_example_nativeopencvandroidtemplate_MainActivity_adaptiveThresholdFromJNI(JNIEnv *env,jobject instance,jlong matAddr) {

    // get Mat from raw address
    cv::Mat &mat = *(cv::Mat *) matAddr;

    clock_t begin = clock();

    //orb(mat);
    //fast(mat);
    sift(mat);

    // log computation time to Android Logcat
    double totalTime = double(clock() - begin) / CLOCKS_PER_SEC;
    __android_log_print(ANDROID_LOG_INFO, TAG, "adaptiveThreshold computation time = %f seconds\n",
                        totalTime);
}
}

void orb(cv::Mat &mat)
{
    cv::Ptr<cv::ORB> ptr = cv::ORB::create(3000);
    vector<cv::KeyPoint> keyPoints;
    ptr->detect(mat, keyPoints, cv::Mat());
    drawKeypoints(mat, keyPoints, mat, cv::Scalar(0, 0, 255));
}

void fast(cv::Mat &mat)
{
    cv::Ptr<cv::FastFeatureDetector> ptr = cv::FastFeatureDetector::create(35);
    vector<cv::KeyPoint> keyPoints;
    ptr->detect(mat, keyPoints, cv::Mat());
    drawKeypoints(mat, keyPoints, mat, cv::Scalar(255, 0, 0));
}

void sift(cv::Mat &mat){
    cv::Mat grayImg;
    cv::Ptr<cv::SIFT> ptr = cv::SIFT::create();
    vector<cv::KeyPoint> keyPoints;
    cvtColor(mat, grayImg, cv::COLOR_BGR2GRAY);
    ptr->detect(grayImg, keyPoints, cv::Mat());
    drawKeypoints(mat, keyPoints, mat, cv::Scalar(0, 255, 0),cv::DrawMatchesFlags::NOT_DRAW_SINGLE_POINTS);
}