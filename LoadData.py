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

# Create path and filename variables
path = 'C:/Users/ciant/OneDrive/Documents/Year4/FinalYearProject/InstrumentID'


# Read in titles for labelled data
df = pd.read_csv(path+'/instrument_titles.csv')
df.set_index('fname', inplace=True)


# Index now holds file name column
for f in df.index:
    rate, signal = wavfile.read(path+'/instrument_wav_files/'+f)  # Read in wav files
    df.at[f, 'length'] = signal.shape[0]/rate  # Access individual elements, get length of each signal in seconds
