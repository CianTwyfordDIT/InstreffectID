from flask import Flask

app = Flask(__name__)


@app.route('/', methods=['GET', 'POST'])
def handle_request():
    return "Connected to Flask Server :D"


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)