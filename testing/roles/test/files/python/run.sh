#! /bin/sh
pip install -r /code/requirements.txt
py.test --junitxml=/code/target/surefire-reports/test_selenium.xml -q /code/src/test_selenium.py