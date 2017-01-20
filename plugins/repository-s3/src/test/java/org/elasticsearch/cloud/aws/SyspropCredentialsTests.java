begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cloud.aws
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cloud
operator|.
name|aws
package|;
end_package

begin_import
import|import
name|com
operator|.
name|amazonaws
operator|.
name|auth
operator|.
name|AWSCredentialsProvider
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|Settings
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
DECL|class|SyspropCredentialsTests
specifier|public
class|class
name|SyspropCredentialsTests
extends|extends
name|ESTestCase
block|{
DECL|method|test
specifier|public
name|void
name|test
parameter_list|()
block|{
name|AWSCredentialsProvider
name|provider
init|=
name|InternalAwsS3Service
operator|.
name|buildCredentials
argument_list|(
name|logger
argument_list|,
name|deprecationLogger
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
comment|// NOTE: sys props are setup by the test runner in gradle
name|assertEquals
argument_list|(
literal|"sysprop_access"
argument_list|,
name|provider
operator|.
name|getCredentials
argument_list|()
operator|.
name|getAWSAccessKeyId
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"sysprop_secret"
argument_list|,
name|provider
operator|.
name|getCredentials
argument_list|()
operator|.
name|getAWSSecretKey
argument_list|()
argument_list|)
expr_stmt|;
name|assertWarnings
argument_list|(
literal|"Supplying S3 credentials through system properties is deprecated"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
