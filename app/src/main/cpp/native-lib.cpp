#include <jni.h>
#include <android/log.h>
#include <opencv2/opencv.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>

#include "include/rest_rpc.hpp"
#include <chrono>
#include <fstream>
#define TAG "NativeLib"

using namespace std;

using namespace rest_rpc;
using namespace rest_rpc::rpc_service;

double avgTime = 0;
void calcHist(cv::Mat &mat);
void hough(cv::Mat &mat);
void orb(cv::Mat &mat);
void fast(cv::Mat &mat);
void sift(cv::Mat &mat);


extern "C" {

int JNICALL
Java_com_example_nativeopencvandroidtemplate_MainActivity_getTime(JNIEnv *env,jobject instance) {
    return (int)avgTime;
}

jboolean JNICALL
Java_com_example_nativeopencvandroidtemplate_MainActivity_rpcClientInit(JNIEnv *env,jobject instance) {

    rpc_client client("192.168.0.106", 9000);
    bool r = client.connect(1);
    if (!r) {
        std::cout << "connect timeout" << std::endl;
        return false;
    }
    return true;
}

void JNICALL
Java_com_example_nativeopencvandroidtemplate_MainActivity_imageTrans(JNIEnv *env,jobject instance, jlong matAddr) {
    // get Mat from raw address
//    cv::Mat &mat = *(cv::Mat *) matAddr;
//    vector<uchar> temp;
//    std::string file;
//    cv::imencode(".jpg", mat, temp);
//    temp.assign(file.begin(), file.end());


//    bool r = client.connect(1);
//    if (!r) {
//        std::cout << "connect timeout" << std::endl;
//        return;
//    }

//    auto f = client.async_call<FUTURE>("upload", "test", file);
//    if (f.wait_for(std::chrono::milliseconds(500)) == std::future_status::timeout) {
//        std::cout << "timeout" << std::endl;
//    }
//    else {
//        f.get().as();
//        std::cout << "ok" << std::endl;
//    }

}

void JNICALL
Java_com_example_nativeopencvandroidtemplate_MainActivity_imageProc(JNIEnv *env,jobject instance, jlong matAddr) {

    // get Mat from raw address
    cv::Mat &mat = *(cv::Mat *) matAddr;

    clock_t begin = clock();

    //calcHist(mat);
    //hough(mat);
    //orb(mat);
    //fast(mat);
    //sift(mat);

    // log computation time to Android Logcat
    double totalTime = double(clock() - begin) / CLOCKS_PER_SEC;
    __android_log_print(ANDROID_LOG_INFO, TAG, "computation time = %f seconds\n",
                        totalTime);
    avgTime = totalTime*1000;

}

}

void calcHist(cv::Mat &mat){
    cv::Mat dst;

    vector<cv::Mat> bgr_planes;
    split(mat, bgr_planes);

    int histSize = 256;
    float range[] = { 0,256 };
    const float *histRanges = { range };

    cv::Mat b_hist, g_hist, r_hist;
    calcHist(&bgr_planes[0], 1, 0, cv::Mat(), b_hist, 1, &histSize, &histRanges, true, false);
    calcHist(&bgr_planes[1], 1, 0, cv::Mat(), g_hist, 1, &histSize, &histRanges, true, false);
    calcHist(&bgr_planes[2], 1, 0, cv::Mat(), r_hist, 1, &histSize, &histRanges, true, false);

    int hist_h = 1080;
    int hist_w = 1920;
    int bin_w = hist_w / histSize;
    //cv::Mat histImage(hist_w, hist_h, CV_8UC3, cv::Scalar(0, 0, 0));
    normalize(b_hist, b_hist, 0, hist_h, cv::NORM_MINMAX, -1, cv::Mat());
    normalize(g_hist, g_hist, 0, hist_h, cv::NORM_MINMAX, -1, cv::Mat());
    normalize(r_hist, r_hist, 0, hist_h, cv::NORM_MINMAX, -1, cv::Mat());

    for (int i = 0; i < histSize; i++)
    {
        line(mat, cv::Point((i - 1)*bin_w, hist_h - cvRound(b_hist.at<float>(i - 1))),
             cv::Point((i)*bin_w, hist_h - cvRound(b_hist.at<float>(i))), cv::Scalar(255, 0, 0), 2, cv::LINE_AA);
        line(mat, cv::Point((i - 1)*bin_w, hist_h - cvRound(g_hist.at<float>(i - 1))),
             cv::Point((i)*bin_w, hist_h - cvRound(g_hist.at<float>(i))), cv::Scalar(0, 255, 0), 2, cv::LINE_AA);
        line(mat, cv::Point((i - 1)*bin_w, hist_h - cvRound(r_hist.at<float>(i - 1))),
             cv::Point((i)*bin_w, hist_h - cvRound(r_hist.at<float>(i))), cv::Scalar(0, 0, 255), 2, cv::LINE_AA);
    }
}

void hough(cv::Mat &mat){
    cv::Mat gray, dst;
    cvtColor(mat, gray, cv::COLOR_BGR2GRAY);
    Canny(gray, dst, 0, 60, 3);
    vector<cv::Vec4f> lines;
    HoughLinesP(dst, lines, 1.0, CV_PI / 180, 150, 0, 3);
    for (auto planes : lines) {
        line(mat, cv::Point(planes[0], planes[1]), cv::Point(planes[2], planes[3]), cv::Scalar(0, 0, 255), 2);
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