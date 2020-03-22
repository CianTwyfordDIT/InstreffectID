# This file takes in an unlabelled audio sample and
# makes an instrument classification prediction based
# on the trained neural network.


import os  # Library to interact with operating system
import pandas as pd  # Library to create and manipulate data frames
import numpy as np  # Library for array computing
import pickle  # Store binary files
from keras.models import load_model  # Keras function to load saved model file
from scipy.io import wavfile  # Library to read in and load .wav files
from python_speech_features import mfcc  # Audio library for MFCC features
from Configuration import Configuration  # Contains Configuration class

# Create paths and filename variables
path = 'C:/Users/ciant/OneDrive/Documents/Year4/FinalYearProject/InstrumentID'
file = ''.join(os.listdir(path+'/Predict_Audio'))
picklePath = os.path.join('Saved_Pickle', 'conv.p')  # Create path to conv.p


# Get the most frequent element in predict list
def mostFrequent(list):
    counter = 0
    element = list[0]

    for i in list:
        currentFreq = list.count(i)
        if currentFreq > counter:
            counter = currentFreq
            element = i
    return element


# Make predictions on files in specified directory
def predict(audioDirectory):
    prediction = []  # Create list for prediction

    samplingRate, wavFile = wavfile.read(os.path.join(audioDirectory, file))  # Read in file to predict

    # Walk through each audio file
    for i in range(0, wavFile.shape[0]-Configuration.sampleStep, Configuration.sampleStep):
        audioSample = wavFile[i:i+Configuration.sampleStep]  # Get sample of the audio
        featX = mfcc(audioSample, samplingRate, numcep=Configuration.numFeat,
                     nfilt=Configuration.numFilt, nfft=Configuration.numFft)  # Get MFCCs of sample

        featX = (featX-Configuration.min) / (Configuration.max-Configuration.min)  # Normalise data
        featX = featX.reshape(1, featX.shape[0], featX.shape[1], 1)  # Change shape of featX for convolutional neural network without changing data
        pred = convModel.predict(featX)  # Get prediction from model
        prediction.append(np.argmax(pred))  # Add prediction to prediction list

    prediction = instrumentClasses[mostFrequent(prediction)]  # Get instrument class of most common index in y_pred list

    return prediction  # Return prediction


df = pd.read_csv(path+'/Instrument_Titles.csv')  # Load wave file names and labels into dataframe
instrumentClasses = list(np.unique(df.label))  # Get unique instrument class names

#  Store pickle into config
with open(picklePath, 'rb') as pickleHandle:
    Configuration = pickle.load(pickleHandle)

convModel = load_model(Configuration.modelPath)  # Load saved model into model

prediction = predict(path+'/Predict_Audio')  # Call predict function with directory of file to be analysed

# Print Result To Console
print()
print('Predicted Classification of '+file+':')
print(prediction)
