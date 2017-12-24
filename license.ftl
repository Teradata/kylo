<#ftl encoding="UTF-8" strip_whitespace=true/>
<#--
 #%L
 install
 %%
 Copyright (C) 2017 ThinkBig Analytics
 %%
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 #L%
-->

<#-- Constants -->
<#assign LICENSE_FILE_NAMES = ["META-INF/LICENSE"]/>
<#assign NOTICE_FILE_NAMES = ["META-INF/NOTICE", "META-INF/NOTICE.txt", "NOTICE", "NOTICE.txt"]/>

<#-- BEGIN LICENSES MACRO -->
<#macro licenses dependencyMap>
  <#-- Initialize utilities -->
  <#assign objectConstructor = "freemarker.template.utility.ObjectConstructor"?new()/>

  <#assign beansWrapper = objectConstructor("freemarker.ext.beans.BeansWrapper")/>
  <#assign staticModels = beansWrapper.getStaticModels()/>

  <#assign CharBuffer = staticModels["java.nio.CharBuffer"]/>
  <#assign buffer = CharBuffer.allocate(8192)/>

  <#assign Charset = staticModels["java.nio.charset.Charset"]/>
  <#assign UTF8 = Charset.forName("UTF-8")/>

  <#assign Pattern = staticModels["java.util.regex.Pattern"]/>
  <#assign FILE_EXT_PATTERN = Pattern.compile("\\.([^\\.]+)$")/>

  <#-- Covert dependencyMap into a sequence of hashes -->
  <#assign dependencies = objectConstructor("java.util.ArrayList", dependencyMap?size)/>

  <#-- -->
  <#list dependencyMap as dependency>
    <#assign result = dependencies.add({"licenses": dependency.getValue(), "name": dependency.getKey().name, "project": dependency.getKey()})/>
  </#list>
  <#-- -->

  <#-- Output license for each dependency -->
  <#list dependencies?sort_by("name") as dependency>
    <#assign licenses = dependency.licenses/>
    <#assign project = dependency.project/>

    <#-- Open JAR file -->
    <#assign artifact = project.getArtifact()/>
    <#assign hasJarFile = false/>
    <#assign repository = project.getProjectBuildingRequest().getLocalRepository()/>

    <#assign artifactPath = repository.pathOf(artifact)?replace(".bundle$", ".jar")/>
    <#if !artifactPath?ends_with(".jar")>
      <#assign matcher = FILE_EXT_PATTERN.matcher(artifactPath)/>
      <#assign artifactPath = matcher.replaceAll(".jar")/>
    </#if>
    <#-- -->

    <#assign artifactFile = objectConstructor("java.io.File", repository.getBasedir(), artifactPath)/>
    <#if artifactFile.exists()>
      <#assign hasJarFile = true/>
      <#assign jarFile = objectConstructor("java.util.jar.JarFile", artifactFile)/>
    </#if>
    <#-- -->

    <#-- Find notice file -->
    <#assign hasNoticeFile = false/>

    <#-- -->
    <#if hasJarFile>
      <#list NOTICE_FILE_NAMES as noticeFileName>
        <#if jarFile.getEntry(noticeFileName)??>
          <#assign hasNoticeFile = true/>
          <#assign noticeFile = jarFile.getEntry(noticeFileName)/>
          <#break/>
        </#if>
      </#list>
    </#if>
    <#-- -->

<#-- Output notice -->
--------------------------------------------------------------------------------

<#-- -->
<#if project.groupId == "antlr">
  <@publicDomain/>
<#elseif project.groupId == "ch.qos.cal10n">
  <@cal10nLicense/>
<#elseif project.groupId == "com.google.protobuf">
  <@protobufLicense/>
<#elseif project.groupId == "com.h2database">
  <@generalLicense "Eclipse Public License - v 1.0"/>
<#elseif project.groupId == "com.infradna.tool">
  <@infradnaLicense/>
<#elseif project.groupId == "com.jcraft">
  <@jcraftLicense/>
<#elseif project.groupId == "com.thoughtworks.paranamer">
  <@paranamerLicense/>
<#elseif project.groupId == "dk.brics.automaton">
  <@automatonLicense/>
<#elseif project.groupId == "dom4j">
  <@dom4jLicense/>
<#elseif project.groupId == "javax.servlet" || project.groupId == "javax.servlet.jsp">
  <@generalLicense "Common Development and Distribution License Version 1.1"/>
<#elseif project.groupId == "javax.transaction">
  <@javaxTransactionLicense/>
<#elseif project.groupId == "net.jcip">
  <@apache2License/>
<#elseif project.groupId?starts_with("org.apache.")>
  <@apache2License/>
<#elseif project.groupId == "com.databricks">
  <@apache2License/>
<#elseif project.groupId == "org.antlr">
  <@antlrLicense/>
<#elseif project.groupId == "org.bouncycastle">
  <@bouncyCastleLicense/>
<#elseif project.groupId == "org.codehaus.jettison">
  <@apache2License/>
<#elseif project.groupId == "org.codehaus.woodstox">
  <@stax2License/>
<#elseif project.groupId == "org.eclipse.jgit">
  <@jgitLicense/>
<#elseif project.groupId == "org.fusesource.leveldbjni">
  <@leveldbjniLicense/>
<#elseif project.groupId == "org.hashids">
  <@mitLicense/>
<#elseif project.groupId == "org.jvnet">
  <@generalLicense "Common Development and Distribution License Version 1.1"/>
<#elseif project.groupId == "org.ow2.asm" || project.groupId == "asm">
  <@asmLicense/>
<#elseif project.groupId == "org.slf4j">
  <@slf4jLicense/>
<#elseif project.groupId == "postgresql">
  <@postgresqlLicense/>
<#elseif project.groupId == "xmlenc">
  <@xmlencLicense/>
<#elseif licenses?seq_contains("Public Domain")>
  <@publicDomain/>
<#elseif licenses?seq_contains("Creative Commons CC0 1.0 Universal")>
  <@generalLicense "Creative Commons CC0 1.0 Universal license"/>
<#elseif licenses?seq_contains("WTFPL")>
  <@generalLicense "WTFPL license"/>
<#elseif licenses?seq_contains("Apache License, Version 2.0")>
  <@apache2License/>
<#elseif licenses?seq_contains("Day Specification License")>
  <@generalLicense "Day Specification License"/>
<#elseif licenses?seq_contains("Eclipse Public License - v 1.0")>
  <@generalLicense "Eclipse Public License - v 1.0"/>
<#elseif licenses?seq_contains("GNU Lesser General Public License, Version 2.1")>
  <@generalLicense "GNU Lesser General Public License, Version 2.1"/>
<#elseif licenses?seq_contains("Common Development and Distribution License Version 1.1")>
  <@generalLicense "Common Development and Distribution License Version 1.1"/>
<#else>
  <#stop "Template missing license for " + project.groupId + ":" + project.artifactId + ": " + licenses?join(", ")/>
</#if>
<#-- -->

</#list>
</#macro>
<#-- END LICENSES MACRO -->

<#-- BEGIN INDIVIDUAL LICESE MACROS -->

<#-- General licenses -->
<#macro generalLicense licenseName>
  <@projectInfo project/>

Licensed under the ${licenseName}.
</#macro>

<#-- ANTLR 3 License -->
<#macro antlrLicense>
  <@projectInfo project/>

Copyright (c) 2010 Terence Parr
All rights reserved.

  <@bsd3License/>
</#macro>

<#-- Apache License, Version 2.0 -->
<#macro apache2License>
  <#if hasNoticeFile>
    <@printStream jarFile.getInputStream(noticeFile)/>
  <#else>
  <@projectInfo project/>
  </#if>

Licensed under the Apache License, Version 2.0.
</#macro>

<#-- ASM License -->
<#macro asmLicense>
  <@projectInfo project/>

Copyright (c) 2000-2011 INRIA, France Telecom
All rights reserved.

  <@bsd3License/>
</#macro>

<#-- Automaton License -->
<#macro automatonLicense>
  <@projectInfo project/>

Copyright (c) 2001-2011 Anders Moeller
All rights reserved.

  <@bsd3License/>
</#macro>

<#-- Bouncy Castle License -->
<#macro bouncyCastleLicense>
  <@projectInfo project/>

Copyright (c) 2000 - 2016 The Legion of the Bouncy Castle Inc. (https://www.bouncycastle.org)

  <@mitLicense/>
</#macro>

<#-- BSD 3-Clause License -->
<#macro bsd3License>
Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the copyright holder nor the names of its
      contributors may be used to endorse or promote products derived from this
      software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
</#macro>

<#-- cal10n License -->
<#macro cal10nLicense>
  <@projectInfo project/>

Copyright (c) 2009 QOS.ch
All rights reserved.

  <@mitLicense/>
</#macro>

<#-- dom4j License -->
<#macro dom4jLicense>
  <@projectInfo project/>

Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.

  <@bsd3License/>
</#macro>

<#-- InfraDNA License -->
<#macro infradnaLicense>
  <@projectInfo project/>

Copyright (c) 2010, InfraDNA, Inc.

  <@mitLicense/>
</#macro>

<#-- Java Transaction API License -->
<#macro javaxTransactionLicense>
  <@projectInfo project/>

Licensed under the following license:
http://download.oracle.com/otndocs/jcp/jta-1.1-spec-oth-JSpec/jta-1.1-spec-oth-JSpec-license.html
</#macro>

<#-- JCraft License -->
<#macro jcraftLicense>
  <@projectInfo project/>

Copyright (c) 2002-2015 Atsuhiko Yamanaka, JCraft,Inc.
All rights reserved.

  <@bsd3License/>
</#macro>

<#-- Eclipse JGit License -->
<#macro jgitLicense>
  <@projectInfo project/>

Copyright (C) 2009, Google Inc.
Copyright (C) 2009, Igor Fedorenko <igor@ifedorenko.com>
Copyright (C) 2008, Imran M Yousuf <imyousuf@smartitengineering.com>
Copyright (C) 2010, Matthias Sohn <matthias.sohn@sap.com>
and other copyright owners as documented in the project's IP log.

This program and the accompanying materials are made available
under the terms of the Eclipse Distribution License v1.0 which
accompanies this distribution, is reproduced below, and is
available at http://www.eclipse.org/org/documents/edl-v10.php

All rights reserved.

  <@bsd3License/>
</#macro>

<#-- Level DB JNI License -->
<#macro leveldbjniLicense>
  <@projectInfo project/>

Copyright (c) 2011 FuseSource Corp. All rights reserved.

  <@bsd3License/>
</#macro>

<#-- MIT License -->
<#macro mitLicense>
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
</#macro>

<!-- ParanamerLicense -->
<#macro paranamerLicense>
  <@projectInfo project/>

Copyright (c) 2006 Paul Hammant & ThoughtWorks Inc
All rights reserved.

  <@bsd3License/>
</#macro>

<#-- PostgreSQL License -->
<#macro postgresqlLicense>
  <@projectInfo project/>

Portions Copyright (c) 1996-2017, The PostgreSQL Global Development Group

Portions Copyright (c) 1994, The Regents of the University of California

Permission to use, copy, modify, and distribute this software and its documentation for any purpose, without fee, and without a written agreement is hereby granted, provided that the above copyright notice and this paragraph and the following two paragraphs appear in all copies.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

  <@bsd3License/>
</#macro>

<#-- Google Protocol Buffers License -->
<#macro protobufLicense>
  <@projectInfo project/>

This license applies to all parts of Protocol Buffers except the following:

  - Atomicops support for generic gcc, located in
    src/google/protobuf/stubs/atomicops_internals_generic_gcc.h.
    This file is copyrighted by Red Hat Inc.

  - Atomicops support for AIX/POWER, located in
    src/google/protobuf/stubs/atomicops_internals_power.h.
    This file is copyrighted by Bloomberg Finance LP.

Copyright 2014, Google Inc.  All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

    * Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above
copyright notice, this list of conditions and the following disclaimer
in the documentation and/or other materials provided with the
distribution.
    * Neither the name of Google Inc. nor the names of its
contributors may be used to endorse or promote products derived from
this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

Code generated by the Protocol Buffer compiler is owned by the owner
of the input file used when generating it.  This code is not
standalone and requires a support library to be linked with it.  This
support library is itself covered by the above license.
</#macro>

<#-- Public Domain -->
<#macro publicDomain>
  <@projectInfo project/>

Available in the Public Domain.
</#macro>

<#-- SLF4J License -->
<#macro slf4jLicense>
  <@projectInfo project/>

Copyright (c) 2004-2013 QOS.ch
All rights reserved.

  <@mitLicense/>
</#macro>

<#-- STAX2 License -->
<#macro stax2License>
  <@projectInfo project/>

  <@bsd3License/>
</#macro>

<#-- XMLENC License -->
<#macro xmlencLicense>
  <@projectInfo project/>

Copyright 2003-2005, Ernst de Haan <wfe.dehaan@gmail.com>
All rights reserved.

  <@bsd3License/>
</#macro>

<#-- END INDIVIDUAL LICENSE MACRO -->

<#-- BEGIN UTILITY MACROS -->

<#-- Outputs the contents of an InputStream -->
<#macro printStream inputStream>
<#assign reader = objectConstructor("java.io.InputStreamReader", inputStream, UTF8)/>
<#list 1..128 as i><#assign bytes = reader.read(buffer)/><#if bytes != -1>${buffer.flip()}<#assign result = buffer.clear()/><#else><#break/></#if></#list>
</#macro>

<#-- Outputs info about a project -->
<#macro projectInfo project>
  <#compress>
    ${project.name} v${project.version}
    ${project.groupId}:${project.artifactId}
    <#if project.url??>${project.url}<#else></#if>
  </#compress>

</#macro>

<#-- END UTILITY MACROS -->
