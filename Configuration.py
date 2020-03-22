# This file specifies the configuration variables
# needed for various functions in the application.


import os


# Configuration class to initialise constants for neural network model to
# avoid hard coding
class Configuration:
    # Number of triangular filters, number of MFCC features, length of signal to calculate
    # the Fast Fourier Transform and the sampling rate
    def __init__(self, mode='conv', numFilt=26, numFeat=13, numFft=512, samplingRate=16000):
        self.mode = mode
        self.numFilt = numFilt
        self.numFeat = numFeat
        self.numFft = numFft
        self.samplingRate = samplingRate
        self.sampleStep = int(samplingRate/10)  # Return 1/10 of a second
        self.modelPath = os.path.join('Saved_Model', mode+'.model')
        self.picklePath = os.path.join('Saved_Pickle', mode+'.p')
