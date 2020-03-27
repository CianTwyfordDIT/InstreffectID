from flask import Flask, request
from werkzeug.utils import secure_filename
import os
from PredictClass import predict

# Create path variable
path = 'C:/Users/ciant/OneDrive/Documents/Year4/FinalYearProject/InstrumentID/Predict_Audio'

app = Flask(__name__)


@app.route('/', methods=['GET', 'POST'])
def handleRequest():
    response = "Connected To Flask Server Successfully"

    return response


@app.route('/uploadFile', methods=['GET', 'POST'])
def uploadFile():
    if request.method =='POST':
        file = request.files['uploadFile']
        if file:
            filePath = path+"/"+secure_filename(file.filename)
            file.save(filePath)

            response = predict(filePath)
            os.remove(filePath)
        else:
            response = "File Upload Failed"
        return response


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
