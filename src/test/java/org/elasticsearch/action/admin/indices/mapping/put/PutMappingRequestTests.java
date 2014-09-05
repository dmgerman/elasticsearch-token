begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.mapping.put
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|mapping
operator|.
name|put
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|ActionRequestValidationException
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
name|ElasticsearchTestCase
import|;
end_import

begin_class
DECL|class|PutMappingRequestTests
specifier|public
class|class
name|PutMappingRequestTests
extends|extends
name|ElasticsearchTestCase
block|{
DECL|method|testValidation
specifier|public
name|void
name|testValidation
parameter_list|()
block|{
name|PutMappingRequest
name|r
init|=
operator|new
name|PutMappingRequest
argument_list|(
literal|"myindex"
argument_list|)
decl_stmt|;
name|ActionRequestValidationException
name|ex
init|=
name|r
operator|.
name|validate
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
literal|"type validation should fail"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|ex
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"type is missing"
argument_list|)
argument_list|)
expr_stmt|;
name|r
operator|.
name|type
argument_list|(
literal|""
argument_list|)
expr_stmt|;
name|ex
operator|=
name|r
operator|.
name|validate
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
literal|"type validation should fail"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|ex
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"type is empty"
argument_list|)
argument_list|)
expr_stmt|;
name|r
operator|.
name|type
argument_list|(
literal|"mytype"
argument_list|)
expr_stmt|;
name|ex
operator|=
name|r
operator|.
name|validate
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
literal|"source validation should fail"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|ex
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"source is missing"
argument_list|)
argument_list|)
expr_stmt|;
name|r
operator|.
name|source
argument_list|(
literal|""
argument_list|)
expr_stmt|;
name|ex
operator|=
name|r
operator|.
name|validate
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
literal|"source validation should fail"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|ex
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"source is empty"
argument_list|)
argument_list|)
expr_stmt|;
name|r
operator|.
name|source
argument_list|(
literal|"somevalidmapping"
argument_list|)
expr_stmt|;
name|ex
operator|=
name|r
operator|.
name|validate
argument_list|()
expr_stmt|;
name|assertNull
argument_list|(
literal|"validation should succeed"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

