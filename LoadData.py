# This file loads in audio data, completes a Fourier Transform on the
# data, retrieves the MFCCs from the data and uses masks to remove any
# unnecessary signal components.

import os  # Library to interact with operating system
import librosa  # Audio library to read in data
from tqdm import tqdm  # Library to graph any iterable in python eg. making progress bar
import pandas as pd  # Library to create and manipulate data frames
import numpy as np  # Library for array computing
from scipy.io import wavfile  # Library to read in and load .wav files
from python_speech_features import mfcc, logfbank  # Audio library for MFCC and filter bank features

# Create path variable
path = 'C:/Users/ciant/OneDrive/Documents/Year4/FinalYearProject/InstrumentID'

# Create dictionaries
audioSignals={}
fourierTrans={}
filterBank={}
mfccs={}


# Read in titles for labelled data
df = pd.read_csv(path+'/instrument_titles.csv')
df.set_index('fname', inplace=True)


# Calculate the Fast Fourier Transform
def calculate_fourierTrans(signal, rate):
    signalLength = len(signal)  # Get length of signal
    frequency = np.fft.rfftfreq(signalLength, d=1/rate)  # Length and time that passes between each sample
    absSignal = abs(np.fft.rfft(signal)/signalLength)  # Get absolute value for magnitude
    return absSignal, frequency  # Return magnitude and frequency


# Index now holds file name column
for f in df.index:
    rate, signal = wavfile.read(path+'/instrument_wav_files/'+f)  # Read in wav files
    df.at[f, 'length'] = signal.shape[0]/rate  # Access individual elements, get length of each signal in seconds


# Create set of instrument classes
instrument_classes = list(np.unique(df.label))
df.reset_index(inplace=True)


# Get one example file from each instrument class
for _class in instrument_classes:
    wavFile = df[df.label == _class].iloc[0, 0]  # Get first file of each class with iloc
    audioSignal, samplingRate = librosa.load(path+'/instrument_wav_files/'+wavFile, sr=44100)  # Load in wave files and set their sampling rate
    audioSignals[_class] = audioSignal  # Store signal read in into dictionary
    fourierTrans[_class] = calculate_fourierTrans(audioSignal, samplingRate)  # Store returned signal from fft into dictionary

    # Create Filter Banks
    fbank = logfbank(audioSignal[:samplingRate], samplingRate, nfilt=26,
                     nfft=1103).T  # Get one second of signal and transpose the returning matrix
    filterBank[_class] = fbank  # Store into dictionary
