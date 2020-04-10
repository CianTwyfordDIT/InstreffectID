# This file acts as the server for the application using
# Flask. It accepts connection requests and stores files
# uploaded to the server in the Predict_Audio directory.
# The file is then predicted with the imported PredictClass
# and the resulting prediction is returned to the client.

from flask import Flask, request  # Library for micro webframe and retrieving data from POST
from werkzeug.utils import secure_filename  # Retrieve filename from file in POST
import os  # Library to interact with operating system
from PredictClass import predict  # Contains PredictClass

# Create path variable
path = 'C:/Users/ciant/OneDrive/Documents/Year4/FinalYearProject/InstrumentID/Predict_Audio'

app = Flask(__name__)  # This module


# Method for dealing with connection request from client
@app.route('/', methods=['GET', 'POST'])
def handleRequest():
    response = "Connected To Server"  # Return when client has connected to server

    return response


# Method for dealing with file uploads from client
@app.route('/uploadFile', methods=['GET', 'POST'])
def uploadFile():
    # Check to see if POST is used
    if request.method =='POST':
        file = request.files['uploadFile']  # Retrieve file body from POST
        # Check to see if file was uploaded
        if file:
            # Store file into Predict_Audio directory for access from PredictClass
            filePath = path+"/"+secure_filename(file.filename)
            file.save(filePath)

            response = predict(filePath)  # Call predict method from PredictClass
            os.remove(filePath)  # Remove file from directory once prediction is complete
        # If file was not uploaded, send response
        else:
            response = "File Upload Failed"
        return response


# Run app on local host and set port number
if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
