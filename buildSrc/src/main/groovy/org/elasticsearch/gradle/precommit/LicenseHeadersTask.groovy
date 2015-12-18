/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.elasticsearch.gradle.precommit

import org.apache.rat.anttasks.Report
import org.apache.rat.anttasks.SubstringLicenseMatcher
import org.apache.rat.license.SimpleLicenseFamily
import org.elasticsearch.gradle.AntTask
import org.gradle.api.tasks.SourceSet

import java.nio.file.Files

/**
 * Checks files for license headers.
 * <p>
 * This is a port of the apache lucene check
 */
public class LicenseHeadersTask extends AntTask {

    LicenseHeadersTask() {
        description = "Checks sources for missing, incorrect, or unacceptable license headers"
    }

    @Override
    protected void runAnt(AntBuilder ant) {
        ant.project.addTaskDefinition('ratReport', Report)
        ant.project.addDataTypeDefinition('substringMatcher', SubstringLicenseMatcher)
        ant.project.addDataTypeDefinition('approvedLicense', SimpleLicenseFamily)

        // create a file for the log to go to under reports/
        File reportDir = new File(project.buildDir, "reports/licenseHeaders")
        reportDir.mkdirs()
        File reportFile = new File(reportDir, "rat.log")
        Files.deleteIfExists(reportFile.toPath())
                     
        // run rat, going to the file
        ant.ratReport(reportFile: reportFile.absolutePath, addDefaultLicenseMatchers: true) {
               // checks all the java sources (allJava)
               for (SourceSet set : project.sourceSets) {
                   for (File dir : set.allJava.srcDirs) {
                       // sometimes these dirs don't exist, e.g. site-plugin has no actual java src/main...
                       if (dir.exists()) {
                           ant.fileset(dir: dir)
                       }
                   }
               }

               // BSD 4-clause stuff (is disallowed below)
               // we keep this here, in case someone adds BSD code for some reason, it should never be allowed.
               substringMatcher(licenseFamilyCategory: "BSD4 ",
                                licenseFamilyName:     "Original BSD License (with advertising clause)") {
                   pattern(substring: "All advertising materials")
               }

               // Apache
               substringMatcher(licenseFamilyCategory: "AL   ",
                                licenseFamilyName:     "Apache") {
                   // Apache license (ES)
                   pattern(substring: "Licensed to Elasticsearch under one or more contributor")
                   // Apache license (ASF)
                   pattern(substring: "Licensed to the Apache Software Foundation (ASF) under")
                   // this is the old-school one under some files
                   pattern(substring: "Licensed under the Apache License, Version 2.0 (the \"License\")")
               }

               // Generated resources
               substringMatcher(licenseFamilyCategory: "GEN  ",
                                licenseFamilyName:     "Generated") {
                   // parsers generated by antlr
                   pattern(substring: "ANTLR GENERATED CODE")
               }
                
               // approved categories
               approvedLicense(familyName: "Apache")
               approvedLicense(familyName: "Generated") 
        }
        
        // check the license file for any errors, this should be fast.
        boolean zeroUnknownLicenses = false
        boolean foundProblemsWithFiles = false
        reportFile.eachLine('UTF-8') { line ->
            if (line.startsWith("0 Unknown Licenses")) {
                zeroUnknownLicenses = true
            }
            
            if (line.startsWith(" !")) {
                foundProblemsWithFiles = true
            }
        }
        
        if (zeroUnknownLicenses == false || foundProblemsWithFiles) {
            // print the unapproved license section, usually its all you need to fix problems.
            int sectionNumber = 0
            reportFile.eachLine('UTF-8') { line ->
                if (line.startsWith("*******************************")) {
                    sectionNumber++
                } else {
                    if (sectionNumber == 2) {
                        logger.error(line)
                    }
                }
            }
            throw new IllegalStateException("License header problems were found! Full details: " + reportFile.absolutePath)
        }
    }
}
