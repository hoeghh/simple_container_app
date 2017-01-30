from flask import Flask

app = Flask(__name__)

@app.route('/')
def index():
        return 'Hello world from Praqma in :<br>Aarhus and Oslo and Odense. What a great day'

if __name__ == '__main__':
        app.run(debug=True,host='0.0.0.0')
