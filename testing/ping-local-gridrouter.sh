#!/usr/bin/env bash
curl http://boot2docker:$(docker ps | grep jetty | sed 's/.*0.0.0.0://' | sed 's/->.*//')/ping && echo
