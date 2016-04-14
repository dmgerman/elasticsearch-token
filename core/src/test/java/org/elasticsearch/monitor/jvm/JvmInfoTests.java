begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.monitor.jvm
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|monitor
operator|.
name|jvm
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|Constants
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|bootstrap
operator|.
name|JavaVersion
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESTestCase
import|;
end_import

begin_class
DECL|class|JvmInfoTests
specifier|public
class|class
name|JvmInfoTests
extends|extends
name|ESTestCase
block|{
DECL|method|testUseG1GC
specifier|public
name|void
name|testUseG1GC
parameter_list|()
block|{
comment|// if we are running on HotSpot, and the test JVM was started
comment|// with UseG1GC, then JvmInfo should successfully report that
comment|// G1GC is enabled
if|if
condition|(
name|Constants
operator|.
name|JVM_NAME
operator|.
name|contains
argument_list|(
literal|"HotSpot"
argument_list|)
operator|||
name|Constants
operator|.
name|JVM_NAME
operator|.
name|contains
argument_list|(
literal|"OpenJDK"
argument_list|)
condition|)
block|{
name|assertEquals
argument_list|(
name|Boolean
operator|.
name|toString
argument_list|(
name|isG1GCEnabled
argument_list|()
argument_list|)
argument_list|,
name|JvmInfo
operator|.
name|jvmInfo
argument_list|()
operator|.
name|useG1GC
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
literal|"unknown"
argument_list|,
name|JvmInfo
operator|.
name|jvmInfo
argument_list|()
operator|.
name|useG1GC
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|isG1GCEnabled
specifier|private
name|boolean
name|isG1GCEnabled
parameter_list|()
block|{
specifier|final
name|String
name|argline
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"tests.jvm.argline"
argument_list|)
decl_stmt|;
specifier|final
name|boolean
name|g1GCEnabled
init|=
name|flagIsEnabled
argument_list|(
name|argline
argument_list|,
literal|"UseG1GC"
argument_list|)
decl_stmt|;
comment|// for JDK 9 the default collector when no collector is specified is G1 GC
specifier|final
name|boolean
name|versionIsAtLeastJava9
init|=
name|JavaVersion
operator|.
name|current
argument_list|()
operator|.
name|compareTo
argument_list|(
name|JavaVersion
operator|.
name|parse
argument_list|(
literal|"9"
argument_list|)
argument_list|)
operator|>=
literal|0
decl_stmt|;
specifier|final
name|boolean
name|noOtherCollectorSpecified
init|=
name|argline
operator|==
literal|null
operator|||
operator|(
operator|!
name|flagIsEnabled
argument_list|(
name|argline
argument_list|,
literal|"UseParNewGC"
argument_list|)
operator|&&
operator|!
name|flagIsEnabled
argument_list|(
name|argline
argument_list|,
literal|"UseParallelGC"
argument_list|)
operator|&&
operator|!
name|flagIsEnabled
argument_list|(
name|argline
argument_list|,
literal|"UseParallelOldGC"
argument_list|)
operator|&&
operator|!
name|flagIsEnabled
argument_list|(
name|argline
argument_list|,
literal|"UseSerialGC"
argument_list|)
operator|&&
operator|!
name|flagIsEnabled
argument_list|(
name|argline
argument_list|,
literal|"UseConcMarkSweepGC"
argument_list|)
operator|)
decl_stmt|;
return|return
name|g1GCEnabled
operator|||
operator|(
name|versionIsAtLeastJava9
operator|&&
name|noOtherCollectorSpecified
operator|)
return|;
block|}
DECL|method|flagIsEnabled
specifier|private
name|boolean
name|flagIsEnabled
parameter_list|(
name|String
name|argline
parameter_list|,
name|String
name|flag
parameter_list|)
block|{
specifier|final
name|boolean
name|containsPositiveFlag
init|=
name|argline
operator|!=
literal|null
operator|&&
name|argline
operator|.
name|contains
argument_list|(
literal|"-XX:+"
operator|+
name|flag
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|containsPositiveFlag
condition|)
return|return
literal|false
return|;
specifier|final
name|int
name|index
init|=
name|argline
operator|.
name|lastIndexOf
argument_list|(
name|flag
argument_list|)
decl_stmt|;
return|return
name|argline
operator|.
name|charAt
argument_list|(
name|index
operator|-
literal|1
argument_list|)
operator|==
literal|'+'
return|;
block|}
block|}
end_class

end_unit

