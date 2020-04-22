import argparse
import logging
import errno
import json
import os
import shutil
import sys
import urllib.request
import zipfile
import platform
import subprocess
from distutils.dir_util import copy_tree

logging.basicConfig(filename="debug.log", level=logging.DEBUG)
logging.getLogger().addHandler(logging.StreamHandler())
rootPath = os.path.abspath(os.path.join(os.path.realpath(__file__), "../.."))
tmpPath = os.path.join(rootPath, ".tmp")
parser = argparse.ArgumentParser(description='Script to setup testrig')
logging.info("root path: " + rootPath)
# General Arguments
subparsers = parser.add_subparsers(dest='mode', help='sub-command help')
subparsers.required = True
subparsers_setup = subparsers.add_parser('setup', help='Setup testrig')
subparsers_run = subparsers.add_parser('run', help='Run testrig')
subparsers_test = subparsers.add_parser('test', help='Run test')
args = parser.parse_args()
print(args)
print("root_path: "+rootPath)
print("tmp_path: "+tmpPath)
# global variables #
partnerDemoServiceUrl = "https://mosip.s3-us-west-2.amazonaws.com/authentication-partnerdemo-service.jar"
partnerDemoServiceJarPath = os.path.join(rootPath, "ivv-orchestrator/dependency_jars/authentication-partnerdemo-service.jar")

registrationClientUrl = "http://13.71.87.138:8040/artifactory/libs-release-local/io/mosip/registration/registration-client/1.0.7/mosip-sw-1.0.7.zip"
registrationClientZipPath = os.path.join(tmpPath, "rc.zip")

identySDKUrl = "https://mosip.s3-us-west-2.amazonaws.com/identy-sdk.jar"
identySDKJarPath = os.path.join(rootPath, "ivv-registration/services_jar/identy-sdk.jar")


environment = ""
emailServerHost = ""
emailServerUsername = ""
emailServerPassword = ""

def preSetup():
    if not os.path.exists(tmpPath):
        os.makedirs(tmpPath)
    preRun()

def preRun():
    properties_path = os.path.join(os.path.realpath(__file__), './../properties.json')
    if os.path.isfile(properties_path):
        with open(properties_path, 'r') as file:
            properties = json.loads(file.read())
            registrationClientUrl = properties['registrationClientUrl']
            environment = properties['environment']
            emailServerHost = properties['emailServerHost']
            emailServerUsername = properties['emailServerUsername']
            emailServerPassword = properties['emailServerPassword']
            logging.info("Properties :")
            logging.info("registrationClientUrl -> "+registrationClientUrl)
            logging.info("environment -> "+environment)
            logging.info("emailServerHost -> "+emailServerHost)
            logging.info("emailServerUsername -> "+emailServerUsername)
            logging.info("emailServerPassword -> "+emailServerPassword)
            user_input = input("Hello contributor, Check the properties carefully. Then press any key to continue...")
    else:
        raise FileNotFoundError(errno.ENOENT, os.strerror(errno.ENOENT), properties_path)

def checkPlatform():
    if platform.system() != 'Linux' and  platform.system() != 'Windows':
        logging.info("Current script not supported for "+platform.system())
        return
    logging.info("Your OS: "+platform.system())


def checkMaven():
    logging.info("Checking maven")
    try:
        ds = subprocess.run(["mvn", "-v"], shell = True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
        if ds.returncode != 0:
            logging.error("Failed to check maven version")
            return

        if ds.stdout is None:
            logging.error("Maven not installed")
            return
    except subprocess.CalledProcessError as e:
        logging.error(e.output)


def checkJavaHome():
    logging.info("Checking JAVA_HOME environment variable")
    try:
        ds = subprocess.run(["echo", "$JAVA_HOME"], shell = True, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
        if ds.returncode != 0:
            logging.error("Failed to check JAVA_HOME env")
            return

        if ds.stdout is None:
            logging.error("JAVA_HOME not set. Please set JAVA_HOME environment variable and try again.")
            return
    except subprocess.CalledProcessError as e:
        logging.error(e.output)


def bytesToMB(v):
    return "%.2f" % round(v/1024/1024, 2)


def checkConfigFile():
    user_input = ''
    while user_input not in ['y', 'n']:
        logging.info("ivv-orchestrator/config.properties will be used to set the host and other user level credentials for testrig")
        user_input = input("Hello contributor, have you updated the ivv-orchestrator/config.properties (y/ n): ")
        if user_input == 'y':
            return
        elif user_input == 'n':
            logging.info("Please update ivv-orchestrator/config.properties, and run again")
            exit(0)
        else:
            logging.info("please enter y/ n")


def runPartnerServiceInfo():
    logging.info("If you are running scenarios with id authentication. You have to start the partner service. Go to the ivv-orchestrator dir and run java -jar dependency_jars/authentication-partnerdemo-service.jar")
    user_input = input("Hello contributor, If you are running scenarios with id authentication. You have to start the partner service . Go to the ivv-orchestrator dir and run java -jar dependency_jars/authentication-partnerdemo-service.jar. Then press any key to continue...")



def copydir(source, dest):
    """Copy a directory structure overwriting existing files"""
    for root, dirs, files in os.walk(source):
        if not os.path.isdir(root):
            os.makedirs(root)
        for each_file in files:
            rel_path = root.replace(source, '').lstrip(os.sep)
            dest_path = os.path.join(dest, rel_path, each_file)
            shutil.copy(os.path.join(root, each_file), dest_path)


def copytree(src, dst, symlinks=False, ignore=None):
    if not os.path.exists(dst):
        os.makedirs(dst)
    for item in os.listdir(src):
        s = os.path.join(src, item)
        d = os.path.join(dst, item)
        if os.path.isdir(s):
            copytree(s, d, symlinks, ignore)
        else:
            if not os.path.exists(d) or os.stat(s).st_mtime - os.stat(d).st_mtime > 1:
                shutil.copy2(s, d)


def reporthook(blocknum, blocksize, totalsize):
    readsofar = blocknum * blocksize
    if totalsize > 0:
        percent = readsofar * 1e2 / totalsize
        s = "\r%5.1f%% %*d / %d" % (
            percent, len(str(totalsize)), readsofar, totalsize)
        sys.stderr.write(s)
        if readsofar >= totalsize:  # near the end
            sys.stderr.write("\n")
    else:  # total size is unknown
        sys.stderr.write("read %d\n" % (readsofar,))


def copyDependencies():
    src = os.path.join(rootPath, "dependencies")
    dest = os.path.join(rootPath, "ivv-orchestrator", "local")
    if not os.path.exists(dest):
        os.makedirs(dest)
    logging.info("Coping files from "+src+" to "+dest)
    copy_tree(src, dest)
    logging.info("Coping files from "+src+" to "+dest+" done")


def copyConfigFile():
    src = os.path.join(rootPath, "ivv-orchestrator", "config.properties.example")
    dest = os.path.join(rootPath, "ivv-orchestrator", "config.properties")
    logging.info("Coping property file from "+src+" to "+dest)
    shutil.copyfile(src, dest)
    logging.info("Coping property file from "+src+" to "+dest+" done")


def downloadPartnerService():
    logging.info("Downloading Partner service from "+partnerDemoServiceUrl+" to "+partnerDemoServiceJarPath)
    urllib.request.urlretrieve(partnerDemoServiceUrl, partnerDemoServiceJarPath, reporthook)
    logging.info("Downloading Partner service from "+partnerDemoServiceUrl+" to "+partnerDemoServiceJarPath+" done")


# def downloadRegistrationService():
#     logging.info("Downloading Registration service from "+registrationServicesUrl+" to "+registrationServicesJarPath)
#     urllib.request.urlretrieve(registrationServicesUrl, registrationServicesJarPath, reporthook)
#     logging.info("Downloading Registration service from "+registrationServicesUrl+" to "+registrationServicesJarPath+" done")

def downloadRegClient():
    logging.info("Downloading Regclient from "+registrationClientUrl+" to "+registrationClientZipPath)
    urllib.request.urlretrieve(registrationClientUrl, registrationClientZipPath, reporthook)
    logging.info("Downloading Local database from "+registrationClientUrl+" to "+registrationClientZipPath+" done")

def extractLocalDatabase():
    logging.info("Unzipping "+registrationClientZipPath)
    with zipfile.ZipFile(registrationClientZipPath, 'r') as zip_ref:
        zip_ref.extractall(os.path.join(rootPath, '.tmp'))
    copy_tree(os.path.join(tmpPath, 'db'), os.path.join(rootPath, 'ivv-orchestrator', 'db'))
    logging.info("Copied database to "+os.path.join(rootPath, 'ivv-orchestrator', 'db'))

def downloadIdentySDK():
    logging.info("[*] Downloading IdentySDK from "+identySDKUrl+" to "+identySDKJarPath)
    urllib.request.urlretrieve(identySDKUrl, identySDKJarPath, reporthook)
    # response = urllib.request.urlopen(identySDKUrl)
    # print(response.read())
    # with open(identySDKJarPath, "wb") as file:
    #     file.write(response.content);
    logging.info("[*] Downloading IdentySDK from "+identySDKUrl+" to "+identySDKJarPath+" done")

def removeTempFolder():
    shutil.rmtree(tmpPath)

def removeLogsAndReport():
    logging.info("Removing registration-services logs")
    resistrationLogs = os.path.join(rootPath, 'ivv-orchestrator', 'logs')
    if os.path.exists(resistrationLogs):
        for file in os.listdir(resistrationLogs):
            if file.endswith(".log"):
                os.remove(os.path.join(resistrationLogs, file))

    logging.info("Removing testrig logs")
    testRigLogs = os.path.join(rootPath, 'ivv-orchestrator', 'testRun', 'reports')
    if os.path.exists(testRigLogs):
        for file in os.listdir(testRigLogs):
            if file.endswith(".log") or file.endswith(".html"):
                os.remove(os.path.join(testRigLogs, file))

def buildProject():
    try:
        cmdargs = []
        cmdargs.append('-Dmaven.test.skip=true')
        cmdargs.append('-Denvironment='+environment)
        cmdargs.append('-DemailServerHost='+emailServerHost)
        cmdargs.append('-DemailServerUsername='+emailServerUsername)
        cmdargs.append('-DemailServerPassword='+emailServerPassword)
        ds = subprocess.Popen(['mvn', 'clean', 'install', '-f', rootPath+'/pom.xml']+cmdargs, shell = True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
        while True:
            output = ds.stdout.readline()
            if output == b'' and ds.poll() is not None:
                logging.info("break")
                break
            if output:
                logging.info(output.strip())
    except subprocess.CalledProcessError as e:
        logging.error(e.output)
        exit(1)


def runTests():
    try:
        cmdargs = []
        cmdargs.append('-Denvironment='+environment)
        cmdargs.append('-DemailServerHost='+emailServerHost)
        cmdargs.append('-DemailServerUsername='+emailServerUsername)
        cmdargs.append('-DemailServerPassword='+emailServerPassword)
        ds = subprocess.Popen(['mvn', 'test', '-f', rootPath+'/pom.xml']+cmdargs, shell = True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
        logging.error(ds.stderr)
        while True:
            output = ds.stdout.readline()
            if output == b'' and ds.poll() is not None:
                break
                return
            if output:
                logging.info(output.strip())
    except subprocess.CalledProcessError as e:
        logging.error(e.output)
        exit(1)


def setup():
    preSetup()
    checkPlatform()
    checkMaven()
    checkJavaHome()
    copyDependencies()
    copyConfigFile()
    downloadPartnerService()
    downloadIdentySDK()
    downloadRegClient()
    extractLocalDatabase()
    removeTempFolder()


def run():
    preRun()
    removeLogsAndReport()
    checkPlatform()
    checkMaven()
    checkJavaHome()
    checkConfigFile()
    buildProject()
    runPartnerServiceInfo()
    runTests()

def test():
    # preSetup()
    # checkPlatform()
    # checkMaven()
    # checkJavaHome()
    copyDependencies()
    # downloadIdentySDK()
    # downloadRegClient()
    # extractLocalDatabase()
    # removeTempFolder()

if args.mode == 'setup':
    setup()
elif args.mode == 'run':
    run()
elif args.mode == 'test':
    test()
else:
    logging.warning("subcommand not found")
