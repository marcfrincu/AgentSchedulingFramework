# AgentSchedulingFramework
Project holding the main results of my agent-based scheduling framework combining a series of technologies (part of my PhD work between 2008-2011).

## Description

The Agent Scheduling Framework (ASF) provides a decentralized method for scheduling tasks and self-heal. It is based on **Feedback Control Loops** that govern the previous two functionalities. 

It relies on my own **OSyRIS workflow engine** based on a nature inspired paradigm (chemical reactions was the kickstarter) to execute the scheduling algorithms and **RabbitMQ** for handling messages between components. Unfortunately, I cannot find the source code for OSyRIS but I have included the compiled jar file (lib/ folder) together with some example files used by ASF during the scheduling and negotiation (rules/ folder).

### Published papers

The platform has been presented in various conferences. See below for a selected list:

- M. Frincu, N. Villegas, D. Petcu, H. Muller, R. Ruvoy, [Self-Healing Distributed Scheduling Platform](https://hal.inria.fr/inria-00563670), Procs. IEEE/ACM CCGrid, 2011.
- M. Frincu, D. Petcu, [OSyRIS: a Nature Inspired Workflow Engine for Service Oriented Environments](https://www.scpe.org/index.php/scpe/article/view/642), Scalable Computing Practice and Experience, vol. 11(1), 2010.

## Disclaimer

The project was a proof of concept demonstrating self-healing in an agent-based distributed platform and should be treated accordingly. It combines various technologies and concepts I found interesting during my PhD studies and worth further exploring.

The file *asf-todo* contains some of my plans for further improving the prototype and which I never got the time to do.

### Class Structure

The class structure is as follows:

- **agent/** - contains Agent.java which models a platform agent
- **modules/** contains classes for defining the main agent modules
  - **communication/** contains AMQP communication classes
    - **json/** contains classes for (d)ecoding JSON messages sent via AMQP
- **network/** - contains MulticastClient for broadcasting multicast messages used to identify remote available resources
- **runner/** - contains RunModule which is used to start modules in case of failures
- **services/** - contains classes for accessing agent/task/resource information from a database. The example uses PostgresQL but can be any other database as long as it uses the provided service interfaces
- **settings/** - contains classes for setting up the platform and for logging
- **simulator/** - contains Failures.java which is used to simulate module failures.
- **test/** - contains various classes for testing the functionality of various platform components
- **utils/** - contains other utility classes such as classes for accessing HDFS and for copying/deploying the agent archive to a remote location

## License

GPL - please see the accompanying gpl-3.0.txt file

## Credits

The Scheduling Platform was developed as part of the European FP7-ICT project mOSAIC grant no. 256910, at the e-Austria Research Institute in Timisoara Romania. For any questions/remarks feel free to contact me.

## Requirements

- Java 1.6 or newer
- Additional libraries come bundled with the package
- Ant utility
- RRabbitMQ 3.0.2 or later
- Hadoop
- Postgresql 8.4 or later

## Build from sources

*ant dist*

**NOTE:** once ant is run you will see a dist/ folder containing all the necessary files to run the platform.

## Test installation

The files already contain a simple example.

**NOTE:** by default the agent token is 26853a29-0073-4ab8-b3ad-5aacf442d0f5 and located in the agent.token file.

*cd dist*
*java -jar asf-engine_{TIMESTAMP}.jar deploy.json*

You can view the trace in the Postgres table 'trace' or in the log file asf.log from the dist/26853a29-0073-4ab8-b3ad-5aacf442d0f5/ folder.

ALTERNATIVELY you can start the platform one module at a time by using either Eclipse or the command line: 

*runner.RunModule HEALING FALSE -1 localhost true FALSE asf.tar executor.sh*

The arguments are explained in the *runner.RunModule* class. You can have either HEALING, SCHEDULING, NEGOTIATION or EXECUTOR as first argument depending on the module type.
