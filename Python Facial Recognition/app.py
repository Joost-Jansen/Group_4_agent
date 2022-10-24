import cv2
import sys
import numpy as np
from flask import Flask
import os
document_path = os.getcwd()+'/haarcascade_frontalface_default.xml'

app = Flask(__name__)

# cascPath = sys.argv[1]
faceCascade = cv2.CascadeClassifier(document_path)

video_capture = cv2.VideoCapture(0)

# Set model path
model = 'emotion-ferplus-8.onnx'

# Now read the model
net = cv2.dnn.readNetFromONNX(model)
emotions = ['Neutral', 'Happy', 'Surprise', 'Sad', 'Anger', 'Disgust', 'Fear', 'Contempt']

@app.route('/')
def hello():
    return getEmotion()

def getEmotion():
    current_emotion = ''
    # Capture frame-by-frame
    ret, frame = video_capture.read()

    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)

    faces = faceCascade.detectMultiScale(
        gray,
        scaleFactor=1.1,
        minNeighbors=5,
        minSize=(30, 30),
        flags=cv2.CASCADE_SCALE_IMAGE
    )
    if(len(faces) > 0):
        prime_face = faces[0]
        print(prime_face)
        x = prime_face[0]
        y = prime_face[1]
        w = prime_face[2]
        h = prime_face[3]
        padding = 3
        cropped = frame[y:y+h, x:x+w]  
        gray = cv2.cvtColor(cropped,cv2.COLOR_BGR2GRAY) 
        resized_face = cv2.resize(gray, (64, 64))
        processed_face = resized_face.reshape(1,1,64,64)
        net.setInput(processed_face)
        Output = net.forward()
        expanded = np.exp(Output - np.max(Output))
        probablities =  expanded / expanded.sum()
        prob = np.squeeze(probablities)
        predicted_emotion = emotions[prob.argmax()]

        current_emotion = predicted_emotion
        return {"emotion": current_emotion, "x": str(x), "y": str(y)}
    else:
        return {"emotion": "Neutral", "x": "0", "y": "0"}
    #     cv2.imshow('Video', cropped)
    # # Draw a rectangle around the faces
    # for (x, y, w, h) in faces:
    #     cv2.rectangle(frame, (x, y), (x+w, y+h), (0, 255, 0), 2)

    # Display the resulting frame
    # cv2.imshow('Video', frame)

    # if cv2.waitKey(1) & 0xFF == ord('q'):
    #     break

    # When everything is done, release the capture

