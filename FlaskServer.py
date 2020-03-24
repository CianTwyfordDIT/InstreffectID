from flask import Flask, request
from werkzeug.utils import secure_filename

# Create path variable
path = 'C:/Users/ciant/OneDrive/Documents/Year4/FinalYearProject/InstrumentID/Predict_Audio/'

app = Flask(__name__)


@app.route('/uploadFile', methods=['GET', 'POST'])
def uploadFile():
    if request.method == 'PUT':
        file = request.files['file']
        filePath = path+secure_filename(file.filename)
        file.save(filePath)
        response = "Success"

        return response


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)