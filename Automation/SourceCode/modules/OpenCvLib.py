# pylint: disable=E1101
"""
#-------------------------------------------------------------------------------
# Name       :  OpenCvLib
# Purpose    :  OpenCV wrapper
#
# Author     :  Rebaca
#
# Created    :  26-03-2015
# Copyright  :  (c) Rebaca 2015
#-------------------------------------------------------------------------------
"""
import datetime
import cv2
import numpy as np

class OpenCvLibrary(object):
    """
    This class is to capture image/video and search a picture in
    another picture by using opencv and matplotlib
    """
    def __init__(self):
        pass

    @staticmethod
    def capture_image(video_file_path, tak_scrnsht_aftr_sec,
                      scrn_sht_fil_pth="test1.jpg"):
        """
        Capture image from <video_file_path> file at <tak_scrnsht_aftr_sec>
        seconds and save it as <scrn_sht_fil_pth>
        """
        cap = cv2.VideoCapture(video_file_path)
        tak_scrnsht_aftr_sec = int(tak_scrnsht_aftr_sec)

        while cap.isOpened():
            frame = cap.read()[1]
            # If there is no frame then exit
            if frame is None :
                break
            # Operation on the frame
            new_frame = cv2.cvtColor(frame, 0)
            # Showing frame
            cv2.imshow('Video Play', new_frame)

            current_time = cap.get(cv2.CAP_PROP_POS_MSEC)
            if current_time >= tak_scrnsht_aftr_sec * 1000 :
                # Taking the screen shot, ans save the
                cv2.imwrite(scrn_sht_fil_pth, new_frame)
                # Showing the screen shot
                img = cv2.imread(scrn_sht_fil_pth, 1)
                cv2.imshow('Screen Shot', img)
                #cv2.waitKey(1)
                break

            if cv2.waitKey(1) & 0xFF == ord('q'):
                break
        else:
            raise Exception("No able to capture video from file :" + \
                            repr(video_file_path))

        cap.release()
        cv2.destroyAllWindows()
        return True

    @staticmethod
    def crop_file(file_name, top_y, bottom_y, left_x, right_x):
        """
        Use to crop an image file
        @args:
        top_y    : top y coordinate of the crop box
        bottom_y : bottom y coordinate of the crop box
        left_x   : left x coordinate of the crop box
        right_x  : right x coordinate of the crop box

                       y value of this below line : top_y
                                   _______
                                  |       |
        x of this line : left_x-> |_______| <- x of this line : right_x
                       y value of this above line : bottom_y

        """
        img = cv2.imread(file_name)
        img = img[top_y:bottom_y, left_x:right_x]
        cv2.imwrite(file_name, img)
        #cv2.imshow("", img)
        #cv2.waitKey(1)


    @staticmethod
    def search_picture_in_picture(source_file_path, search_pic_path):
        """
        Search a picture into another big picture
        @args:
        source_file_path : The big pic file path where you want to search
        search_pic_path  : The pic file path you want to search
        """
        img_rgb = cv2.imread(source_file_path)
        img_gray = cv2.cvtColor(img_rgb, cv2.COLOR_BGR2GRAY)

        template = cv2.imread(search_pic_path, 0)
        width, height = template.shape[::-1]

        res = cv2.matchTemplate(img_gray, template, cv2.TM_CCOEFF_NORMED)
        threshold = 0.8
        loc = np.where( res >= threshold)

        mched_coordinate = zip(*loc[::-1])
        mtch_found = True if mched_coordinate else False

        for ech_pt in mched_coordinate:
            cv2.rectangle( img_rgb, ech_pt,
                          (ech_pt[0] + width, ech_pt[1] + height),
                          (0, 0, 255),
                          2)
        cv2.destroyAllWindows()

        return mtch_found

    @staticmethod
    def capture_video(record_duration, save_recording_file_name):
        """
        Capture a video for <record_duration> seconds and saved it in
        <save_recording_file_name> file name (with path)
        """
        start_time = datetime.datetime.now()
        cap = cv2.VideoCapture(0)

        # Define the codec and create VideoWriter object
        fourcc = cv2.VideoWriter_fourcc('i', 'Y', 'U', 'V') # *"XVID",
        out = cv2.VideoWriter(save_recording_file_name,
                              fourcc, 12.0,
                              (640, 480))
        print "Input :", record_duration,
        print " -- File name :", repr(save_recording_file_name)
        record_duration = int(record_duration)

        while (cap.isOpened()):
            # Capturing frame by frame
            ret, frame = cap.read()

            if ret == True:
                # Operation on the frame
                new_frame = cv2.cvtColor(frame, 0)

                # Displaying the resulting frame
                #cv2.imshow('Capture From WebCam',new_frame)

                # Write the frame
                out.write(new_frame)
                # Use appropriate time for cv2.waitKey(). If it is too less,
                # video will be very fast and if it is too high, video will be
                # slow (that is how you can display videos in slow motion).
                # 25 milliseconds will be OK in normal cases.

                # cv2.waitKey(1)

                # Capture till the duration provided
                if (datetime.datetime.now() - start_time ).total_seconds() > \
                  record_duration:
                    break

            else:
                cap.release()
                out.release()
                cv2.destroyAllWindows()
                raise Exception("Doesn't get any frame")
        else:
            cap.release()
            out.release()
            cv2.destroyAllWindows()
            raise Exception("No able to capture video from device.")

        # When everything is over then release the memory
        cap.release()
        out.release()
        cv2.destroyAllWindows()

if __name__ == "__main__":
    IMG1 = "E:\\LimelightSDKAutomation\\screenShots\\1.png"
    print ">>>>>>>>>", OpenCvLibrary.crop_file(IMG1, 320, 960, 170, 530)