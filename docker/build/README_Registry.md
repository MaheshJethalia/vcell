# Setup private Docker Registry with self-signed certificate (http://ralph.soika.com/how-to-setup-a-private-docker-registry)
**Registry service**  

```bash
ssh vcell-docker.cam.uchc.edu
mkdir -p /usr/local/deploy/registry_certs
cd /usr/local/deploy
openssl req -newkey rsa:4096 -nodes -sha256 -keyout registry_certs/domain.key -x509 -days 356 -out registry_certs/domain.cert
sudo docker run -d -p 5000:5000 -v $(pwd)/registry_certs:/certs -e REGISTRY_HTTP_TLS_CERTIFICATE=/certs/domain.cert -e REGISTRY_HTTP_TLS_KEY=/certs/domain.key --restart=always --name registry registry:2
```


**Registry simple web-based UI** to the private repository see http://vcell-docker.cam.uchc.edu:5001/home [registry gui, web-based registry viewer](http://vcell-docker.cam.uchc.edu:5001/home), this UI was recently ported to registry:2 and has limited functionality (as of Jan 25, 2018).

```bash
# Set the name of the registry host (theRegistryHost=vcell-docker.cam.uchc.edu for CCAM VCell installation)
export theRegistryHost=vcell-docker.cam.uchc.edu
sudo docker run -d --restart=always --name registry-ui -e ENV_DOCKER_REGISTRY_HOST=${theRegistryHost} -e ENV_DOCKER_REGISTRY_PORT=5000 -e ENV_DOCKER_REGISTRY_USE_SSL=1 -p 5001:80 konradkleine/docker-registry-frontend:v2

open http://localhost:5001
```
**Registry API, NOT USED** [catalog](https://vcell-docker.cam.uchc.edu:5000/v2/_catalog) https://vcell-docker.cam.uchc.edu:5000/v2/_catalog  


for removing old docker images from registry (error upon delete):

```bash
go get github.com/fraunhoferfokus/deckschrubber
export GOPATH=$HOME/go
$GOPATH/bin/deckschrubber -day 30 -registry https://vcell-docker.cam.uchc.edu:5000
```


## install self-signed cert as trusted CA to enable use of Docker registry by Singularity
trusting self signed certificate on Macos (https://github.com/docker/distribution/issues/2295), and Linux/Windows (https://docs.docker.com/registry/insecure/#failing).  For example, to trust the self-signed certificate on UCHC server nodes using Centos 7.2 (e.g. vcellapi, vcell-node1, vcell-node2, vcellapi-beta, vcell-node3, vcell-node4):

```bash
export theRegistryHost=vcell-docker.cam.uchc.edu
sudo scp vcell@{theRegistryHost}:/usr/local/deploy/registry_certs/domain.cert /etc/pki/ca-trust/source/anchors/{theRegistryHost}.crt
sudo update-ca-trust
sudo service docker stop
sudo service docker start
```