import cv2, sys
from matplotlib import pyplot as plt
import numpy as np
from PIL import Image
import math
import firebase_admin
from pytesseract import *
import re
import pandas as pd
from firebase_admin import credentials
from firebase_admin import firestore
import datetime as dt
import calendar
from statsmodels.tsa.arima.model import ARIMA

cred = credentials.Certificate("/home/pi/Mypage/ServiceKey.json")
firebase_admin.initialize_app(cred)
db = firestore.client()

font = cv2.FONT_HERSHEY_COMPLEX

image = cv2.imread('/home/pi/Mypage/picture/water.png')
image1 = cv2.resize(image, (2000, 2000))

headImage = image[190: 292, 271: 467]

text = pytesseract.image_to_string(headImage, config = '--psm 6')
text1 = text[0:4]

numbers = re.sub(r'[^0-9]', '', text1)

croppedImage1 = image1[1110: 1450, 360: 710]
croppedImage2 = image1[1380: 1720, 750: 1090]
croppedImage3 = image1[1260: 1600, 1140: 1490]
croppedImage4 = image1[900: 1240, 1330: 1670]

def print_image(image):
    cimage = cv2.resize(image, (200, 200))

    src_hsv = cv2.cvtColor(cimage, cv2.COLOR_BGR2HSV)

    dst1 = cv2.inRange(cimage, (0, 0, 160), (80, 80, 255))
    img_gray = cv2.bitwise_and(src_hsv, src_hsv, mask = dst1)

    blur = cv2.GaussianBlur(img_gray, ksize=(5,5), sigmaX=0)
    ret, thresh1 = cv2.threshold(blur, 127, 255, cv2.THRESH_BINARY)

    edged = cv2.Canny(blur, 10, 250)

    kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (7,7))
    closed = cv2.morphologyEx(edged, cv2.MORPH_CLOSE, kernel)

    contours, _ = cv2.findContours(closed.copy(),cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    total = 0

    for cnt in contours :
        approx = cv2.approxPolyDP(cnt, 0.009 * cv2.arcLength(cnt, True), True)

        n = approx.ravel()
        i = 0
        
        res_x = 100
        res_y = 100
        res_xy = 0

        for j in n :
            if(i % 2 == 0):
                x = n[i]
                y = n[i + 1]
                
                t_xy = math.sqrt(math.pow(100 - x,2) + math.pow(100 - y,2))

                if(res_xy < t_xy):
                    res_x = x
                    res_y = y
                    res_xy = t_xy
            i = i + 1
            
        if(res_y < 100):
            if(res_x < 100):
                if(((100-res_x) / 80) > 0.95):
                    return 7
                elif(((100-res_x) / 80) > 0.58):
                    return 8
                else:
                    return 9
            else:
                if(((res_x-100) / 80) > 0.95):
                    return 2
                elif(((res_x-100) / 80) > 0.58):
                    return 1
                else:
                    return 0
        else:
            if(res_x < 100):
                if(((100-res_x) / 80) > 0.95):
                    return 7
                elif(((100-res_x) / 80) > 0.58):
                    return 6
                else:
                    return 5
            else:
                if(((res_x-100) / 80) > 0.95):
                    return 2
                elif(((res_x-100) / 80) > 0.58):
                    return 3
                else:
                    return 4

number1 = print_image(croppedImage1)
number2 = print_image(croppedImage2)
number3 = print_image(croppedImage3)
number4 = print_image(croppedImage4)

sum = int(numbers)*10000 + number1*1000 + number2*100 + number3*10 + number4

date = dt.datetime.now()

time = str(date.hour) + ":" + date.minute

with open('/home/pi/Mypage/사용량.csv', 'r') as fc:
    last_line = fc.readlines()[-1]
line = last_line.split()
line2 = line[0].split(",")
fc.close()

if hour == 0:
    f = open("/home/pi/Mypage/사용량.csv", "a")
    f.write('\n')
    f.write(str(date.year) + "-" + str(date.month) + "-" + str(date.day - 1) +',' + str(sum) + '\n')
    f.close()

sum -= int(line2[1])

doc_ref = db.collection(u'Water_User').document(u'jiwan4615@naver.com').collection(f'{date.year}').document(f'{date.month}').collection(f'{date.day}').document(u'METER')

doc = doc_ref.get()
d = doc.to_dict()
db_sum = d["meter"]

doc_ref.set({u'meter' : sum})
    
sum -= db_sum

doc_ref = db.collection(u'Water_User').document(u'jiwan4615@naver.com').collection(f'{date.year}').document(f'{date.month}').collection(f'{date.day}').document(f'{time}')
doc_ref.set({u'meter' : sum})

def predict():
    csv_file = '/home/pi/Mypage/사용량.csv'
    df = pd.read_csv(csv_file)
    df['날짜'] = pd.to_datetime(df['날짜'])
    df.set_index('날짜', inplace=True)

    doc_ref = db.collection(u'Water_User').document(u'jiwan4615@naver.com')

    doc = doc_ref.get()
    d = doc.to_dict()
    start = d["start"]
    start_data = start.split("/")
    
    start_year = start_data[0]
    start_month = start_data[1]

    current_month = pd.Timestamp.now().month
    current_year = pd.Timestamp.now().year
    current_date = pd.Timestamp.now().day

    date = datetime(year=current_year, month=current_month, day=current_date).date()
    last_day = calendar.monthrange(date.year, date.month)[1]

    start_date = pd.to_datetime(f'{current_year}-{start_month}-01')
    end_date = pd.to_datetime(f'{current_year}-{current_month}-{current_date}')
    current_month_data = df.loc[start_date:end_date, '사용량']

    model = ARIMA(current_month_data, order=(1, 2, 0))
    model_fit = model.fit()

    start_date = pd.to_datetime(f'{current_year}-{current_month}-{current_date + 1}')
    end_date = pd.to_datetime(f'{current_year}-{current_month}-{last_day}')

    forecast = model_fit.predict(start=start_date, end=end_date)

    plt.figure(figsize=(10, 6))
    plt.plot(df.index, df['사용량'], label='Actual')
    plt.plot(forecast.index, forecast, label='Forecast')
    plt.xlabel('날짜')
    plt.ylabel('사용량')
    plt.title('ARIMA Forecast for 2023')
    plt.legend()
    plt.show()

    soda = {'날짜': forecast.index, '사용량': forecast.values}
    dff = pd.DataFrame(soda)

    start_date = pd.to_datetime(f'{current_year}-{start_month}-01')
    end_date = pd.to_datetime(f'{current_year}-{current_month}-{current_date}')

    print(int(forecast.values[0]) - int(df.values[(end_date - start_date).days]))

    for i in range(1, forecast.index.size):
        print(int(forecast.values[i]) - int(forecast.values[i-1]))
        

    cred = credentials.Certificate("/home/pi/Mypage/ServiceKey.json")
    firebase_admin.initialize_app(cred)
    db = firestore.client()

    for i in range((current_date + 1), (last_day + 1)):
        print(i)
        doc_ref = db.collection(u'Water_User').document(u'jiwan4615@naver.com').collection(f'{current_year}').document(f'{current_month}').collection(f'{i}').document(u'predict_meter')
        doc_ref.set({
            u'meter' : (int(forecast.values[i - current_date]) - int(forecast.values[i - 1 - current_date]))
        })

if date.hour == 0:
    predict()
