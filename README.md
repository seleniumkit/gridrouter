# Selenium Grid Router

**Selenium Grid Router** is a lightweight server that routes and proxies [Selenium Wedriver](http://www.seleniumhq.org/projects/webdriver/) requests to multiple Selenium hubs.

## Golang Implementation
There is a smaller and faster Golang implementation of this server. See https://github.com/aandryashin/ggr for more details.

## What is this for
If you're frequently using Selenium for running your tests in browsers you may notice that a standard [Selenium Grid](https://github.com/SeleniumHQ/selenium/wiki/Grid2) installation has some faults that can prevent you from using it on large scale:
* **Does not support high availability.** Selenium Grid consists of a single entry point **hub** server and multiple **node** processes. Users interact only with hub. That means that if for some reason hub goes down all nodes also become unavailable to user.
* **Does not scale well.** Our experience shows that even when running on high-end hardware Selenium hub is able to handle correctly no more than 20-30 nodes. When more nodes are connected hub very often stops responding.
* **Does not support authentication and authorization.** Standard Selenium grid hub makes all nodes available for everyone.

## How it works
The basic idea is very simple:

1. Define user names and their passwords in a plain text file
2. Distribute a set of running Selenium hubs (aka "hosts") with nodes connected to each one over multiple datacenters
3. For each defined user save hosts to a simple XML configuration file
4. Start multiple instances of Grid Router in different datacenters and load-balance them
5. Work with Grid Router like you do with a regular Selenium hub

## Installation

Currently we maintain only Debian packages. To install on Ubuntu ensure that you have Java 8 installed:
```
# add-apt-repository ppa:webupd8team/java
# apt-get update
# apt-get install oracle-java8-installer
```
Then install Gridrouter itself:
```
# add-apt-repository ppa:yandex-qatools/gridrouter
# apt-get update
# apt-get install yandex-grid-router
# service yandex-grid-router start
```
Configuration files are located in `/etc/grid-router/` directory, XML quota files - by default in `/etc/grid-router/quota/`, log files reside in `/var/log/grid-router/`, binaries are installed to `/usr/share/grid-router`.

## Configuration
Two types of configuration files exist:
* A plain text file with users and passwords (users.properties)
* An XML file with user quota definition (&lt;username&gt;.xml)

### Users list (users.properties)
A typical file looks like this:
```
alice:alicePassword, user
bob:bobPassword, user
```
As you can see passwords are **NOT** encrypted. This is because we consider quotas as a way to easily limit Selenium browsers consumption and not a restrictive tool.

### User quota definition (&lt;username&gt;.xml)
This file has the following format:
```xml
<qa:browsers xmlns:qa="urn:config.gridrouter.qatools.ru">
    <browser name="firefox" defaultVersion="33.0">
        <version number="33.0">
            <region name="us-west">
                <host name="my-firefox33-hub-1.example.com" port="4444" count="5"/>
            </region>
            <region name="us-east">
                <host name="my-firefox33-hub-2.example.com" port="4444" count="5"/>
            </region>
        </version>
        <version number="37.0">
            <region name="us-west">
                <host name="my-firefox37-hub-1.example.com" port="4444" count="3"/>
                <host name="my-firefox37-hub-2.example.com" port="4444" count="4"/>
            </region>
            <region name="us-east">
                <host name="my-firefox37-hub-3.example.com" port="4444" count="2"/>
            </region>
        </version>
    </browser>
    <browser name="chrome" defaultVersion="42.0">
        <version number="42.0">
            <region name="us-west">
                <host name="my-chrome42-hub-1.example.com" port="4444" count="10"/>
            </region>
            <region name="us-east">
                <host name="my-chrome42-hub-2.example.com" port="4444" count="10"/>
            </region>
        </version>
    </browser>
</qa:browsers>
```
What we basically do in this file - we enumerate hub hosts, ports and counts of browsers available on each hub. We also distribute hosts across regions, i.e. we place hosts from different datacenters in different **&lt;region&gt;** tags. The most important thing is to make sure that browser name and browser version have **exactly** the same value as respective Selenium hub does.

### Authentication
Grid router is using [BASIC HTTP authentication](https://en.wikipedia.org/wiki/Basic_access_authentication). That means that for the majority of test frameworks connection URL would be:
```
http://username:password@grid-router-host.example.com:4444/wd/hub
```
However some Javascript test frameworks have their own ways to specify connection URL, user name and password.

### Hub selection logic
When you request a browser by specifying its name and version **Grid Router** does the following:

1. Searches for the browser in user quota XML and returns error if not found
2. Randomly selects a host from all hosts and tries to obtain browser on that host. Our algorithm also considers browser counts specified in XML for each host so that hosts with more browsers get more connections.
3. If browser was obtained - returns it to the user and proxies all requests in this session to the same host
4. If not - selects a new host **from another region** and tries again. This guarantees that when one datacenter goes down in most of cases we'll obtain browser at worst after the second attempt.
5. After trying all hosts returns error if no browser was obtained

### Hub configuration recommendations
Our experience shows that Grid Router works better with a big set of "small" hubs (having no more than 5 connected nodes) than with some "big" hubs. A good idea is to launch small virtual machines (with 1 or 2 virtual CPUs) containing one Selenium hub process 4-5 Selenium node processes that connect to **localhost**. This gives us the following profit:
* Because we have more hubs the probability to successfully obtain browser is greater
* If each virtual machine has only one browser version installed - it's simpler to increase overall count of available browsers
* Hubs with small count of connected nodes perform better

## Development
We're using [Docker](https://www.docker.com/) and [Ansible](http://www.ansible.com/) for integration tests so you need to install them on your Mac or Linux.

### Install Boot2docker (dog-nail for Mac users)

* Install Ansible: `brew install ansible`
* Create an empty inventory file: `touch /usr/local/etc/ansible/hosts`
* Adjust Python settings: `echo 'localhost ansible_python_interpreter=/usr/local/bin/python' >>  /usr/local/etc/ansible/hosts`
* Instally Python from [official website](https://www.python.org/ftp/python/2.7.10/python-2.7.10-macosx10.6.pkg)
* Install requests with pip: `pip install requests[security]`
* Install docker-py: `pip install -Iv https://pypi.python.org/packages/source/d/docker-py/docker-py-1.1.0.tar.gz`
* Run boot2docker: `boot2docker up`
* Get Docker VM IP: `boot2docker ip`
* Modify `/etc/hosts`: `<boot2docker_ip> boot2docker`
* Add certificates information to console: `$(boot2docker shellinit)`
* Export correct host name: `export DOCKER_HOST=tcp://boot2docker:2376`

### Running service locally

#### Start

1. Build project: `mvn clean package`
2. Start app: `ansible-playbook testing/start.yml`
3. Check that container is running: `docker ps -a`

#### Run integration tests

```bash]
$ ansible-playbook testing/test.yml
```

#### Stop

```bash
$ ansible-playbook testing/stop.yml
```
