## Scripts for setting up and running tetrig
Follow the document in order of dependencies, setup, run

#### Dependencies:
* Python version >= 3.6.9
* JAVA version >= 1.8.0_221
* Maven version >= 3.6.0

#### Setup
* Clone the repository and go to folder scripts. There you will find a python script (run.py)
* RUN "python3 run.py setup" command; this command will perform a series of tasks like downloading externally built jars, generating configuration files, preparing test data for execution, etc

#### Run
* Go to folder scripts where run.py is present
* RUN "python3 run.py run" command; this command will run test and after end of execution you can see the logs and reports in ivv-orchestrator/testRun/reports directory

