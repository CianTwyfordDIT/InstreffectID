from scipy.io import wavfile  # Library to read in and load .wav files
import pandas as pd  # Library to create and manipulate data frames
import numpy as np  # Library for array computing
from Configuration import Configuration  # Contains Config class
from tqdm import tqdm  # Library to graph any iterable in python eg. making progress bar
from python_speech_features import mfcc   # Audio library for MFCC features
# Keras Imports
from keras.utils import to_categorical  # Use for categorical cross entropy
from keras.layers import Conv2D, MaxPool2D, Flatten, LSTM
from keras.layers import Dropout, Dense, TimeDistributed
from keras.models import Sequential  # Model for linear stacking of layers
from keras.callbacks import ModelCheckpoint  # Saving model from Keras to load up later and make predictions

from sklearn.utils.class_weight import compute_class_weight  # Estimate class weights for unbalanced data
import pickle  # Store binary files

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


# Convolutional neural network model
def ConvNeuralNetworkModel():
    convModel = Sequential()  # Linear stack of layers
    # Add convolutional layers to create a covolutional kernel
    # and learn new features about data
    # Increase number of filters for each layer to get more specific as
    # data gets convolved down through each layer
    convModel.add(Conv2D(16, (3, 3), activation='relu', strides=(1, 1), padding='same', input_shape=inputShape))  # Specify input shape for first layer
    convModel.add(Conv2D(32, (3, 3), activation='relu', strides=(1, 1), padding='same'))
    convModel.add(Conv2D(64, (3, 3), activation='relu', strides=(1, 1), padding='same'))
    convModel.add(Conv2D(128, (3, 3), activation='relu', strides=(1, 1), padding='same'))

    # Add pooling layer
    convModel.add(MaxPool2D((2, 2)))
    convModel.add(Dropout(0, 5))
    convModel.add(Flatten())  # Flatten output of pooling to one dimension

    # Add dense layers - gradually pull down layers
    convModel.add(Dense(128, activation='relu'))
    convModel.add(Dense(64, activation='relu'))
    convModel.add(Dense(10, activation='softmax'))

    convModel.summary()  # Print summary of the model to console

    convModel.compile(loss='categorical_crossentropy', optimizer='adam', metrics=['acc'])  # Compile model with all layers added

    return convModel  # Return the model


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
mapFeatY = np.argmax(featY, axis=1)  # Map to original class columns
inputShape = (featX.shape[1], featX.shape[2], 1)  # Define input shape for convolutional neural network
convModel = ConvNeuralNetworkModel()

# Estimate class weights for unbalanced data to reduce bias in neural network
classWeight = compute_class_weight('balanced', np.unique(mapFeatY), mapFeatY)

# Saving model from Keras to load up later and make predictions
# Create checkpoint for the model
modelCheckpoint = ModelCheckpoint(Configuration.modelPath, monitor='val_acc', verbose=1, mode='max',
                                  save_best_only=True, save_weights_only=False, period=1)
# Take featX and featY matrices to randomly create batches of the data over 10 epochs (iterations)
convModel.fit(featX, featY, epochs=10, batch_size=32, shuffle=True,
              class_weight=classWeight, validation_split=0.1,
              callbacks=[modelCheckpoint])
# Save model to specified path
convModel.save(Configuration.modelPath)
