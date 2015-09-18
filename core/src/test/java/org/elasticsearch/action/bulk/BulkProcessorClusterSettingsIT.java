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
name|ESIntegTestCase
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
operator|.
name|ClusterScope
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
operator|.
name|Scope
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

begin_class
annotation|@
name|ClusterScope
argument_list|(
name|scope
operator|=
name|Scope
operator|.
name|TEST
argument_list|,
name|numDataNodes
operator|=
literal|0
argument_list|)
DECL|class|BulkProcessorClusterSettingsIT
specifier|public
class|class
name|BulkProcessorClusterSettingsIT
extends|extends
name|ESIntegTestCase
block|{
annotation|@
name|Test
DECL|method|testBulkProcessorAutoCreateRestrictions
specifier|public
name|void
name|testBulkProcessorAutoCreateRestrictions
parameter_list|()
throws|throws
name|Exception
block|{
comment|// See issue #8125
name|Settings
name|settings
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"action.auto_create_index"
argument_list|,
literal|false
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|createIndex
argument_list|(
literal|"willwork"
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareHealth
argument_list|(
literal|"willwork"
argument_list|)
operator|.
name|setWaitForGreenStatus
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|BulkRequestBuilder
name|bulkRequestBuilder
init|=
name|client
argument_list|()
operator|.
name|prepareBulk
argument_list|()
decl_stmt|;
name|bulkRequestBuilder
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"willwork"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{\"foo\":1}"
argument_list|)
argument_list|)
expr_stmt|;
name|bulkRequestBuilder
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"wontwork"
argument_list|,
literal|"type1"
argument_list|,
literal|"2"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{\"foo\":2}"
argument_list|)
argument_list|)
expr_stmt|;
name|bulkRequestBuilder
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"willwork"
argument_list|,
literal|"type1"
argument_list|,
literal|"3"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{\"foo\":3}"
argument_list|)
argument_list|)
expr_stmt|;
name|BulkResponse
name|br
init|=
name|bulkRequestBuilder
operator|.
name|get
argument_list|()
decl_stmt|;
name|BulkItemResponse
index|[]
name|responses
init|=
name|br
operator|.
name|getItems
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|responses
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"Operation on existing index should succeed"
argument_list|,
name|responses
index|[
literal|0
index|]
operator|.
name|isFailed
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Missing index should have been flagged"
argument_list|,
name|responses
index|[
literal|1
index|]
operator|.
name|isFailed
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"[wontwork] IndexNotFoundException[no such index]"
argument_list|,
name|responses
index|[
literal|1
index|]
operator|.
name|getFailureMessage
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"Operation on existing index should succeed"
argument_list|,
name|responses
index|[
literal|2
index|]
operator|.
name|isFailed
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
