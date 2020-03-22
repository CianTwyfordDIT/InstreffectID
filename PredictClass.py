import os  # Library to interact with operating system
import pandas as pd  # Library to create and manipulate data frames
import numpy as np  # Library for array computing
import pickle  # Store binary files
from keras.models import load_model  # Keras function to load saved model file

# Create paths and filename variables
path = 'C:/Users/ciant/OneDrive/Documents/Year4/FinalYearProject/InstrumentID'
file = ''.join(os.listdir(path+'/Predict_Audio'))
picklePath = os.path.join('SavedPickle', 'conv.p')  # Create path to conv.p

df = pd.read_csv(path+'/Instrument_Titles.csv')  # Load wave file names and labels into dataframe
instrumentClasses = list(np.unique(df.label))  # Get unique instrument class names

#  Store pickle into config
with open(picklePath, 'rb') as pickleHandle:
    Configuration = pickle.load(pickleHandle)

convModel = load_model(Configuration.modelPath)  # Load saved model into model
