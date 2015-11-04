begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.percolator
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|percolator
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|index
operator|.
name|IndexRequestBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|percolate
operator|.
name|PercolateResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|percolate
operator|.
name|PercolateSourceBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|percolator
operator|.
name|PercolatorException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryShardException
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
name|ESIntegTestCase
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentFactory
operator|.
name|jsonBuilder
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|termQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertAcked
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertMatchCount
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|instanceOf
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|PercolatorBackwardsCompatibilityIT
specifier|public
class|class
name|PercolatorBackwardsCompatibilityIT
extends|extends
name|ESIntegTestCase
block|{
DECL|method|testPercolatorUpgrading
specifier|public
name|void
name|testPercolatorUpgrading
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Simulates an index created on an node before 1.4.0 where the field resolution isn't strict.
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|settings
argument_list|(
name|Version
operator|.
name|V_1_3_0
argument_list|)
operator|.
name|put
argument_list|(
name|indexSettings
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|int
name|numDocs
init|=
name|randomIntBetween
argument_list|(
literal|100
argument_list|,
literal|150
argument_list|)
decl_stmt|;
name|IndexRequestBuilder
index|[]
name|docs
init|=
operator|new
name|IndexRequestBuilder
index|[
name|numDocs
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numDocs
condition|;
name|i
operator|++
control|)
block|{
name|docs
index|[
name|i
index|]
operator|=
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
name|PercolatorService
operator|.
name|TYPE_NAME
argument_list|)
operator|.
name|setSource
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"query"
argument_list|,
name|termQuery
argument_list|(
literal|"field1"
argument_list|,
literal|"value"
argument_list|)
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|indexRandom
argument_list|(
literal|true
argument_list|,
name|docs
argument_list|)
expr_stmt|;
name|PercolateResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|preparePercolate
argument_list|()
operator|.
name|setIndices
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setDocumentType
argument_list|(
literal|"type"
argument_list|)
operator|.
name|setPercolateDoc
argument_list|(
operator|new
name|PercolateSourceBuilder
operator|.
name|DocBuilder
argument_list|()
operator|.
name|setDoc
argument_list|(
literal|"field1"
argument_list|,
literal|"value"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertMatchCount
argument_list|(
name|response
argument_list|,
name|numDocs
argument_list|)
expr_stmt|;
comment|// After upgrade indices, indices created before the upgrade allow that queries refer to fields not available in mapping
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
name|PercolatorService
operator|.
name|TYPE_NAME
argument_list|)
operator|.
name|setSource
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"query"
argument_list|,
name|termQuery
argument_list|(
literal|"field2"
argument_list|,
literal|"value"
argument_list|)
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
comment|// However on new indices, the field resolution is strict, no queries with unmapped fields are allowed
name|createIndex
argument_list|(
literal|"test2"
argument_list|)
expr_stmt|;
try|try
block|{
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test2"
argument_list|,
name|PercolatorService
operator|.
name|TYPE_NAME
argument_list|)
operator|.
name|setSource
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"query"
argument_list|,
name|termQuery
argument_list|(
literal|"field1"
argument_list|,
literal|"value"
argument_list|)
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|PercolatorException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getRootCause
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|QueryShardException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

