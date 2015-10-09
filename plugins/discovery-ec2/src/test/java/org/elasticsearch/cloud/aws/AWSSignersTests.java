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
name|ClientConfiguration
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

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|is
import|;
end_import

begin_class
DECL|class|AWSSignersTests
specifier|public
class|class
name|AWSSignersTests
extends|extends
name|ESTestCase
block|{
DECL|method|testSigners
specifier|public
name|void
name|testSigners
parameter_list|()
block|{
name|assertThat
argument_list|(
name|signerTester
argument_list|(
literal|null
argument_list|)
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|signerTester
argument_list|(
literal|"QueryStringSignerType"
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|signerTester
argument_list|(
literal|"AWS3SignerType"
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|signerTester
argument_list|(
literal|"AWS4SignerType"
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|signerTester
argument_list|(
literal|"NoOpSignerType"
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|signerTester
argument_list|(
literal|"UndefinedSigner"
argument_list|)
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|signerTester
argument_list|(
literal|"S3SignerType"
argument_list|)
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|signerTester
argument_list|(
literal|"AWSS3V4SignerType"
argument_list|)
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|ClientConfiguration
name|configuration
init|=
operator|new
name|ClientConfiguration
argument_list|()
decl_stmt|;
name|AwsSigner
operator|.
name|configureSigner
argument_list|(
literal|"AWS4SignerType"
argument_list|,
name|configuration
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|configuration
operator|.
name|getSignerOverride
argument_list|()
argument_list|,
literal|"AWS4SignerType"
argument_list|)
expr_stmt|;
name|AwsSigner
operator|.
name|configureSigner
argument_list|(
literal|"AWS3SignerType"
argument_list|,
name|configuration
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|configuration
operator|.
name|getSignerOverride
argument_list|()
argument_list|,
literal|"AWS3SignerType"
argument_list|)
expr_stmt|;
block|}
comment|/**      * Test a signer configuration      * @param signer signer name      * @return true if successful, false otherwise      */
DECL|method|signerTester
specifier|private
name|boolean
name|signerTester
parameter_list|(
name|String
name|signer
parameter_list|)
block|{
try|try
block|{
name|AwsSigner
operator|.
name|validateSignerType
argument_list|(
name|signer
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
end_class

end_unit

