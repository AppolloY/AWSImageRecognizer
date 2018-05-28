#### Basic configuration

**Public IP:** `xxxxxxxx` 

**S3 bucket name**: `recognition-result-bucket`

**The credential we use**：

Access key ID: `xxxxxxxxx`

Secret access key: `xxxxxxxxxxxx`

--- 
Just a little change.
Just another little change.
#### How to set up the project environment 

Firstly, we should install the aws command line tools on each instance and do the aws configuration.

```sh
$ pip install awscli --upgrade --user
$ aws configure
AWS Access Key ID [****************HCWQ]:
AWS Secret Access Key [****************dxsp]:
Default region name [us-west-1]:
Default output format [json]:
```

Then, we clone all the project code to the instance from GitHub using Git commands. (GitHub repository link: [project1](https://github.com/AlphaGarden/project1.git)).

```sh
$ mkdir ~/hhhhh
$ cd ~/hhhhh
$ git clone https://github.com/AlphaGarden/project1.git
```

We use Maven to do project management and use maven commands to compile the whole project.

```sh
$ sudo apt-get install maven
$ cd ~/hhhhh/project1
$ mvn compile
```

To start the *server*, you should run the following command.

```sh
$ mvn exec:java -Dexec.mainClass="edu.asu.cse546.controller.HttpController" -Dexec.cleanupDaemonThreads=false
```

Similarly, you can run the following command to start the *load balancer* or *image recognizer* on corresponding instance.

```sh
# Run the image recognizer
$ mvn exec:java -Dexec.mainClass="edu.asu.cse546.util.ImageRecognizer" -Dexec.cleanupDaemonThreads=false

# Run the load balancer
$ mvn exec:java -Dexec.mainClass="edu.asu.cse546.controller.LoadController" -Dexec.cleanupDaemonThreads=false
```

##### Automatically run the server/image recognizer program when the instance starts up

To simplify the operation, we write shell scripts in the *server* or *image recognizer* instance, and make our desired program run automatically when our instance starts up. A shell script example for the *server* instance is as follows and it should be written into `/etc/init.d/` directory. (Name it as `run.sh`)

```Sh
#!/bin/bash
export AWS_ACCESS_KEY_ID=xxxxxxxxxxxxxxx
export AWS_SECRET_ACCESS_KEY=xxxxxxxxxxxxxxxxxxxxxxxx

cd /home/ubuntu/hhhhh/project1/

git fetch --all
git reset --hard origin/master > /home/ubuntu/git.log

mvn compile > /home/ubuntu/compile.log
mvn exec:java -Dexec.mainClass="edu.asu.cse546.controller.HttpController" -Dexec.cleanupDaemonThreads=false

exit 0
```

Use the following command to make it start while the instance starts up.

```sh
$ cd /etc/init.d
$ vi run.sh  # Write the script
$ sudo chmod 755 run.sh  # Give permission to this script
$ sudo update-rc.d run.sh defaults 100
$ sudo reboot  # Reboot the instance to make it 
```

---

#### How to run the project and do image recognition

Since our app's service address is different (because we do not use PHP), and the listening port of our application is `5460`, the format of our URL is as follows:

```
http://[server-ip-address]:5460?imageUrl=[imageUrl]
```

An example of URL is as follows:

```
http://54.219.168.207:5460?imageUrl=http://visa.lab.asu.edu/cifar-10/0_cat.png
```

So, the modified test shell `test.sh` is:

```sh
#!/bin/bash
    array[10]="airplane"
    array[1]="automobile"
    array[2]="bird"
    array[3]="cat"
    array[4]="deer"
    array[5]="dog"
    array[6]="frog"
    array[7]="horse"
    array[8]="ship"
    array[9]="truck"

function generate(){
    n=1
    RANDOM=$1
    while [ $n -le $3 ];do
        sortnumber=$(($RANDOM%10+1))
        number=$(($RANDOM%9999+0))
        http="http://visa.lab.asu.edu/cifar-10/"$number"_"${array[$sortnumber]}".png"
        wget --spider -q -o /dev/null  --tries=1 -T 5 $http
            if [ $? -eq 0 ]
                then
                    let n++
                    project1="http://"$2":5460?imageUrl="$http  # different part
                    curl $project1
            fi
    done
}

function fork(){
        count=1;
        echo $1
        while [ "$count" -le "$1" ]
        do
                generate $(($base+$count)) $2 $3&
           
                count=$(( count+1 ))
        done
   }
   base=1213
if [ !${1} ]
then
echo ./test.sh [IP_address] [concurrent_requests_number] [total_requests_number]
fi
   s=$((${3}/${2}))
 
   fork ${2} ${1} $s

    exit 0



```
