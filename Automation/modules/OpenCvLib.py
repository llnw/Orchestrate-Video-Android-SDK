import sys, os, time, datetime
import cv2
import numpy as np
from matplotlib import pyplot as plt

import hashlib

class OpenCvLibrary(object):
    def __init__(self):
        pass

    def capture_image(self, vdoFilPth, takScrnShtAftrSec, scrnShtFilPth="test1.jpg"):
        '''Capture image from <vdoFilPth> file at <takScrnShtAftrSec> seconds and save it as <scrnShtFilPth>'''
        cap = cv2.VideoCapture(vdoFilPth)
        takScrnShtAftrSec = int(takScrnShtAftrSec)

        while cap.isOpened():
            ret, frame = cap.read()
            # If there is no frame then exit
            if frame is None :
                break
            # Operation on the frame
            newFrame = cv2.cvtColor(frame, 0)
            # Showing frame
            cv2.imshow('Video Play', newFrame)

            curTim = cap.get(cv2.CAP_PROP_POS_MSEC)
            if curTim >= takScrnShtAftrSec * 1000 :
                # Taking the screenshot, ans save the
                cv2.imwrite(scrnShtFilPth, newFrame)
                # Showing the screenshot
                img = cv2.imread(scrnShtFilPth,1)
                #cv2.imshow('Screen Shot',img)
                #cv2.waitKey(1) # 1
                #time.sleep(5)
                break

            if cv2.waitKey(1) & 0xFF == ord('q'):
                break


        else:
            raise Exception("No able to capture video from file :"+repr(vdoFilPth))
        #time.sleep(10)
        cap.release()
        cv2.destroyAllWindows()
        return True

    def is_exact_same(self, img1, img2):
        ''' '''
        h_val1 = hashlib.md5(open(img1,'rb').read()).hexdigest()
        h_val2 = hashlib.md5(open(img2,'rb').read()).hexdigest()
        return h_val1 == h_val2

    def match_two_image(self, img1, img2):
        ''' '''
        pass


    def crop_file(self, file_name, y1, y2, x1, x2):
        img = cv2.imread(file_name)
        img = img[y1:y2, x1:x2]
        cv2.imwrite(file_name, img)
        # 80,437 ::: 640, 360
        #template = template[437:797, 80:720] # Chropping


    def search_picture_in_picture(self, sourceFile, searchPic):
        ''' '''
        img_rgb = cv2.imread(sourceFile)
        img_gray = cv2.cvtColor(img_rgb, cv2.COLOR_BGR2GRAY)

        template = cv2.imread(searchPic, 0)
        w, h = template.shape[::-1]

        res = cv2.matchTemplate(img_gray,template,cv2.TM_CCOEFF_NORMED)
        threshold = 0.8
        loc = np.where( res >= threshold)

        mchedCordinate = zip(*loc[::-1])
        mtchFound = True if mchedCordinate else False


        for pt in mchedCordinate:
            cv2.rectangle(img_rgb, pt, (pt[0] + w, pt[1] + h), (0,0,255), 2)
        '''
        if mchedCordinate:
            #cv2.imwrite('res.png',img_rgb)
            cv2.imshow("Detected Point", img_rgb)
            cv2.waitKey(1) # 0
            time.sleep(10)
            pass
        else:
            print "Not matched"
        '''
        cv2.destroyAllWindows()
        '''
        if mtchFound:
            print "Mathced"
        else:
            print "Not Mathced"
        '''
        return mtchFound
        '''
        if mtchFound:
            return mtchFound
        else:
            raise Exception("Image %s doesn't present in image %s"%(repr(searchPic), repr(sourceFile)))
        '''

    def search_picture_not_in_picture(self, sourceFile, searchPic):
        ''' '''
        img_rgb = cv2.imread(sourceFile)
        img_gray = cv2.cvtColor(img_rgb, cv2.COLOR_BGR2GRAY)
        template = cv2.imread(searchPic,0)
        w, h = template.shape[::-1]

        res = cv2.matchTemplate(img_gray,template,cv2.TM_CCOEFF_NORMED)
        threshold = 0.8
        loc = np.where( res >= threshold)

        mchedCordinate = zip(*loc[::-1])
        mtchFound = True if mchedCordinate else False

        for pt in mchedCordinate:
            cv2.rectangle(img_rgb, pt, (pt[0] + w, pt[1] + h), (0,0,255), 2)
        '''
        if mchedCordinate:
            #cv2.imwrite('res.png',img_rgb)
            cv2.imshow("Detected Point", img_rgb)
            cv2.waitKey(1) # 0
            time.sleep(10)
            pass
        else:
            print "Not matched"
        '''
        cv2.destroyAllWindows()
        if mtchFound:
            raise Exception("Image %s present in image %s"%(repr(searchPic), repr(sourceFile)))
        else:
            return True

    def capture_video(self, recDuration, recordFileName):
        '''Capture for <recDuration> seconds'''
        startTim = datetime.datetime.now()
        cap = cv2.VideoCapture(0)

        # Define the codec and create VideoWriter object
        fourcc = cv2.VideoWriter_fourcc('i','Y', 'U', 'V') # *"XVID",
        out = cv2.VideoWriter(recordFileName, fourcc, 12.0, (640,480))
        print "Input :", recDuration, " -- File name :", repr(recordFileName)
        recDuration = int(recDuration)

        while (cap.isOpened()):
            # Capturing frame by frame
            ret, frame = cap.read()

            if ret == True:
                # Operation on the frame
                newFrame = cv2.cvtColor(frame, 0)

                # Displaying the resulting frame
                cv2.imshow('Capture From WebCam',newFrame)

                # Write the frame
                out.write(newFrame)

                '''
                use appropriate time for cv2.waitKey(). If it is too less, video will be
                very fast and if it is too high, video will be slow (Well, that is how
                you can display videos in slow motion). 25 milliseconds will be OK in
                normal cases.
                '''
                cv2.waitKey(1)

                # Capture till the duration provided
                if (datetime.datetime.now() - startTim ).total_seconds() > recDuration:
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

        # When everything is over then relese the memory
        cap.release()
        out.release()
        cv2.destroyAllWindows()

    def search_picture_in_video(self, sourceFile, searchPic):
        '''Is the image <searchPic> is a part of the video <sourceFile>'''
        cap = cv2.VideoCapture(sourceFile)
        template = cv2.imread(searchPic,0)
        w, h = template.shape[::-1]
        mtchFound = False
        while cap.isOpened() :
            ret, frame = cap.read()
            if frame is None:
                break

            cnvrtFrm = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)

            # Matching
            res = cv2.matchTemplate(cnvrtFrm,template,cv2.TM_CCOEFF_NORMED)
            threshold = 0.8
            loc = np.where( res >= threshold)
            mchedCordinate = zip(*loc[::-1])
            mtchFound = True if mchedCordinate else False
            if mtchFound:
                for pt in mchedCordinate:
                    cv2.rectangle(frame, pt, (pt[0] + w, pt[1] + h), (0,0,255), 2)
                '''
                if mchedCordinate:
                    #cv2.imwrite('res.png',img_rgb)
                    cv2.imshow("Detected Point", frame)
                    cv2.waitKey(1)#0
                    time.sleep(10)
                '''
                #cap.release()
                #cv2.destroyAllWindows()
                #return mtchFound
                break


        cap.release()
        cv2.destroyAllWindows()

        if mtchFound:
            return mtchFound
        else:
            raise Exception("Image %s doesn't present in video %s"%(repr(searchPic), repr(sourceFile)))



if __name__ == "__main__":
    obj = OpenCvLibrary()
    #obj.capture_video(30, 'capture.avi')
    img1 = "E:\\LimelightSDKAutomation\\screenShots\\SS-2015_04_14_18_47_08.png"
    img2 = "E:\\LimelightSDKAutomation\\screenShots\\SS-2015_04_14_18_47_08-1.png"
    print ">>>>>>>>>", obj.crop_file(img2, 437, 797, 80, 720)