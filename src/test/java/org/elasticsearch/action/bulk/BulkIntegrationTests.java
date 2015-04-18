begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.bulk
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|bulk
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Charsets
import|;
end_import

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
name|LuceneTestCase
operator|.
name|Slow
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
name|admin
operator|.
name|indices
operator|.
name|mapping
operator|.
name|get
operator|.
name|GetMappingsResponse
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
name|ElasticsearchIntegrationTest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
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
name|io
operator|.
name|Streams
operator|.
name|copyToStringFromClasspath
import|;
end_import

begin_class
annotation|@
name|Slow
DECL|class|BulkIntegrationTests
specifier|public
class|class
name|BulkIntegrationTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
annotation|@
name|Test
DECL|method|testBulkIndexCreatesMapping
specifier|public
name|void
name|testBulkIndexCreatesMapping
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|bulkAction
init|=
name|copyToStringFromClasspath
argument_list|(
literal|"/org/elasticsearch/action/bulk/bulk-log.json"
argument_list|)
decl_stmt|;
name|BulkRequestBuilder
name|bulkBuilder
init|=
operator|new
name|BulkRequestBuilder
argument_list|(
name|client
argument_list|()
argument_list|)
decl_stmt|;
name|bulkBuilder
operator|.
name|add
argument_list|(
name|bulkAction
operator|.
name|getBytes
argument_list|(
name|Charsets
operator|.
name|UTF_8
argument_list|)
argument_list|,
literal|0
argument_list|,
name|bulkAction
operator|.
name|length
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|bulkBuilder
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertBusy
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|GetMappingsResponse
name|mappingsResponse
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareGetMappings
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|mappingsResponse
operator|.
name|getMappings
argument_list|()
operator|.
name|containsKey
argument_list|(
literal|"logstash-2014.03.30"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|mappingsResponse
operator|.
name|getMappings
argument_list|()
operator|.
name|get
argument_list|(
literal|"logstash-2014.03.30"
argument_list|)
operator|.
name|containsKey
argument_list|(
literal|"logs"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

