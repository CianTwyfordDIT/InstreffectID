import os


# Configuration class to initialise constants for neural network model to
# avoid hard coding
class Configuration:
    # Number of triangular filters, number of MFCC features, length of signal to calculate
    # the Fast Fourier Transform and the sampling rate
    def __init__(self, mode='conv', nfilt=26, nfeat=13, nfft=512, samplingRate=16000):
        self.mode = mode
        self.nfilt = nfilt
        self.nfeat = nfeat
        self.nfft = nfft
        self.samplingRate = samplingRate
        self.sampleStep = int(samplingRate/10)  # Return 1/10 of a second
        self.modelPath = os.path.join('SavedModel', mode+'.model')
        self.picklePath = os.path.join('SavedPickle', mode+'.p')
