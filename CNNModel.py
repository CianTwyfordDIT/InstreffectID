from scipy.io import wavfile  # Library to read in and load .wav files
import pandas as pd  # Library to create and manipulate data frames
import numpy as np  # Library for array computing
from Configuration import Configuration  # Contains Config class
from tqdm import tqdm  # Library to graph any iterable in python eg. making progress bar
from python_speech_features import mfcc   # Audio library for MFCC features
# Keras Imports
from keras.utils import to_categorical  # Use for categorical cross entropy

# Create path variable
path = 'C:/Users/ciant/OneDrive/Documents/Year4/FinalYearProject/InstrumentID'


# Function to create featX matrix and featY matrix to hold all small
# samples of audio with their corresponding classes
def generateFeatures():
    # Create lists to be converted to numpy array later
    featX = []
    featY = []
    # Track min and max to normalise input between 0 and 1
    minimum, maximum = float('inf'), -float('inf')  # Highest possible float minimum and maximum

    # Iterate through range of number of samples
    # Use tqdm to graphically represent progress
    for _ in tqdm(range(numSamples)):
        randomClass = np.random.choice(classDistrib.index, p=probDistrib)  # Choose random instrument class based on probability distribution
        file = np.random.choice(df[df.label == randomClass].index)  # Choose random audio file based on randomly generated classs
        samplingRate, wavFile = wavfile.read('Clean_Audio/'+file)  # Read in randomly selected file from clean directory
        label = df.at[file, 'label']  # Get file label
        randomIndex = np.random.randint(0, wavFile.shape[0] - Configuration.sampleStep)  # Randomly get index to start sampling at based on length of audio file
        audioSample = wavFile[randomIndex:randomIndex+Configuration.sampleStep]  # Get random 1/10 second sample from file from random index

        #  Create one sample to go into featX matrix by calculating MFCCs
        sampleX = mfcc(audioSample, samplingRate,
                       numcep=Configuration.nfeat, nfilt=Configuration.nfilt,
                       nfft=Configuration.nfft)

        minimum = min(np.amin(sampleX), minimum)  # Update minimum
        maximum = max(np.amax(sampleX), maximum)  # Update maximum
        featX.append(sampleX)  # Add sample to list
        featY.append(instrumentClasses.index(label))  # Add index of class to list

    # Set Configuration min and max
    Configuration.min = minimum
    Configuration.max = maximum

    featX, featY = np.array(featX), np.array(featY)  # Convert lists to arrays
    featX = (featX - minimum) / (maximum - minimum)  # Normalise featX
    featX = featX.reshape(featX.shape[0], featX.shape[1], featX.shape[2], 1)  # Change shape of X for convolutional neural network without changing data
    featY = to_categorical(featY, num_classes=10)  # Hot encode y for categorical cross entropy

    return featX, featY  # Return feature lists


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

# Randomly sample along length of audio files and take a 1/10 second
# chunk out of audio
# Determine how many samples are possible within signal
# Move through audio file and chunk bit by bit every 1/10 second
numSamples = int(df['length'].sum()/0.1) * 2  # Get 1/10 total length of files and get double that in samples
probDistrib = classDistrib / classDistrib.sum()  # Probability of each class between 0 and 1
# Random sampling from audio data
# The higher the class ditribution, the more likely the class is selected for sampling
randSamples = np.random.choice(classDistrib.index, p=probDistrib)


Configuration = Configuration(mode='conv')  # Set configuration from class
featX, featY = generateFeatures()  # Build feature set featX and featY from random sampling
