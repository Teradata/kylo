#!/usr/bin/env bash

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

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
TARGET_DIR=work
CA_DIR=${TARGET_DIR}/ca
KEYSTORE_FILE=${TARGET_DIR}/keystore.jks
CLIENT_CERT_KEYSTORE=${TARGET_DIR}/client-cert.jks

if [[ -d ${CA_DIR} ]] ; then
    rm -Rf ${CA_DIR}
fi

if [[ -f ${KEYSTORE_FILE} ]] ; then
    rm -Rf ${KEYSTORE_FILE}
fi

if [[ -f ${CLIENT_CERT_KEYSTORE} ]] ; then
    rm -Rf ${CLIENT_CERT_KEYSTORE}
fi

if [ ! -x "$(which openssl)" ] ; then
   echo "[ERROR] No openssl in PATH"
   exit 1
fi

if [  ! -x "$(which keytool)" ] ; then
   echo "[ERROR] No keytool in PATH"
   exit 1
fi

mkdir -p ${CA_DIR}/private ${CA_DIR}/certs ${CA_DIR}/crl ${CA_DIR}/csr ${CA_DIR}/newcerts ${CA_DIR}/intermediate

echo "[INFO] Generating CA private key"
# Less bits = less secure = faster to generate
openssl genrsa -passout pass:changeit -aes256 -out ${CA_DIR}/private/ca.key.pem 2048

chmod 400 ${CA_DIR}/private/ca.key.pem

echo "[INFO] Generating CA certificate"
openssl req -config ${DIR}/openssl.cnf \
      -key ${CA_DIR}/private/ca.key.pem \
      -new -x509 -days 7300 -sha256 -extensions v3_ca \
      -out ${CA_DIR}/certs/ca.cert.pem \
      -passin pass:changeit \
      -subj "/C=US/ST=California/L=San Francisco/O=ThinkBig Analytics, a Teradata Company/CN=CA Certificate"

echo "[INFO] Prepare CA database"
echo 1000 > ${CA_DIR}/serial
touch ${CA_DIR}/index.txt

echo "[INFO] Generating server private key"
openssl genrsa -aes256 \
      -passout pass:changeit \
      -out ${CA_DIR}/private/localhost.key.pem 2048

openssl rsa -in ${CA_DIR}/private/localhost.key.pem \
      -out ${CA_DIR}/private/localhost.decrypted.key.pem \
      -passin pass:changeit

chmod 400 ${CA_DIR}/private/localhost.key.pem
chmod 400 ${CA_DIR}/private/localhost.decrypted.key.pem

echo "[INFO] Generating server certificate request"
openssl req -config ${DIR}/openssl.cnf \
      -key ${CA_DIR}/private/localhost.key.pem \
      -passin pass:changeit \
      -new -sha256 -out ${CA_DIR}/csr/localhost.csr.pem \
      -subj "/C=US/ST=California/L=San Francisco/O=ThinkBig Analytics, a Teradata Company/CN=localhost"

echo "[INFO] Signing certificate request"
openssl ca -config ${DIR}/openssl.cnf \
      -extensions server_cert -days 375 -notext -md sha256 \
      -passin pass:changeit \
      -batch \
      -in ${CA_DIR}/csr/localhost.csr.pem \
      -out ${CA_DIR}/certs/localhost.cert.pem


keytool -importcert -alias ca-cert -keystore ${KEYSTORE_FILE} -file ${CA_DIR}/certs/ca.cert.pem -noprompt -storepass changeit
keytool -importcert -alias vault-cert -keystore ${KEYSTORE_FILE} -file ${CA_DIR}/certs/localhost.cert.pem -noprompt -storepass changeit
