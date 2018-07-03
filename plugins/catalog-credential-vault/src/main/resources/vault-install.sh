#!/bin/bash

###
# #%L
# kylo-catalog-credential-vault
# %%
# Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
# %%
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#     http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# #L%
###

###########################################################################
# Download and Install Vault                                              #
# This script is prepared for caching of the download directory           #
###########################################################################


VAULT_VER="${VAULT_VER:-0.10.1}"
UNAME=$(uname -s |  tr '[:upper:]' '[:lower:]')
VAULT_ZIP="vault_${VAULT_VER}_${UNAME}_amd64.zip"
IGNORE_CERTS="${IGNORE_CERTS:-no}"

# cleanup
mkdir -p vault
mkdir -p download

if [[ ! -f "download/${VAULT_ZIP}" ]] ; then
    cd download
    # install Vault
    if [[ "${IGNORE_CERTS}" == "no" ]] ; then
      echo "Downloading Vault with certs verification"
      curl -O "https://releases.hashicorp.com/vault/${VAULT_VER}/${VAULT_ZIP}"
    else
      echo "WARNING... Downloading Vault WITHOUT certs verification"
      curl -O "https://releases.hashicorp.com/vault/${VAULT_VER}/${VAULT_ZIP}" --no-check-certificate
    fi

    if [[ $? != 0 ]] ; then
      echo "Cannot download Vault"
      exit 1
    fi
    cd ..
fi

cd vault

if [[ -f vault ]] ; then
  rm vault
fi

unzip ../download/${VAULT_ZIP}
chmod a+x vault

# check
./vault --version
