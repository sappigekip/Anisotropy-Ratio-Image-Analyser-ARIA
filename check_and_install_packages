#script to check and install dependencies to run the automated ROI determination with cellpose.

import subprocess
import sys

# List the required packages
required_packages = ['scikit-image', 'cellpose']

def install(package):
    subprocess.check_call([sys.executable, '-m', 'pip', 'install', package])

for package in required_packages:
    try:
        __import__(package)
    except ImportError:
        print(f"Installing {package}...")
        install(package)
