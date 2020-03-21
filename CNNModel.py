from scipy.io import wavfile  # Library to read in and load .wav files
import pandas as pd  # Library to create and manipulate data frames
import numpy as np  # Library for array computing

# Create path variable
path = 'C:/Users/ciant/OneDrive/Documents/Year4/FinalYearProject/InstrumentID'


# Read in titles for labelled data
df = pd.read_csv(path+'/Instrument_Titles.csv')
df.set_index('fname', inplace=True)

# Index now holds file name column
for file in df.index:
    samplingRate, audioSignal = wavfile.read('Clean_Audio/'+file)  # Read in wav files
    df.at[file, 'length'] = audioSignal.shape[0]/samplingRate  # Access individual elements, get length of each signal in seconds

# Create set of instrument classes and get distribution
instrumentClasses = list(np.unique(df.label))
classDistrib = df.groupby(['label'])['length'].mean()