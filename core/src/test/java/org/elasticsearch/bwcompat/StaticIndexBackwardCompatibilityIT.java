begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.bwcompat
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|bwcompat
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
name|LuceneTestCase
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
name|get
operator|.
name|GetIndexResponse
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
name|search
operator|.
name|SearchResponse
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
name|ESIntegTestCase
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
name|greaterThanOrEqualTo
import|;
end_import

begin_comment
comment|/**  * These tests are against static indexes, built from versions of ES that cannot be upgraded without  * a full cluster restart (ie no wire format compatibility).  */
end_comment

begin_class
annotation|@
name|LuceneTestCase
operator|.
name|SuppressCodecs
argument_list|(
literal|"*"
argument_list|)
annotation|@
name|ESIntegTestCase
operator|.
name|ClusterScope
argument_list|(
name|scope
operator|=
name|ESIntegTestCase
operator|.
name|Scope
operator|.
name|TEST
argument_list|,
name|numDataNodes
operator|=
literal|0
argument_list|,
name|minNumDataNodes
operator|=
literal|0
argument_list|,
name|maxNumDataNodes
operator|=
literal|0
argument_list|)
DECL|class|StaticIndexBackwardCompatibilityIT
specifier|public
class|class
name|StaticIndexBackwardCompatibilityIT
extends|extends
name|ESIntegTestCase
block|{
DECL|method|loadIndex
specifier|public
name|void
name|loadIndex
parameter_list|(
name|String
name|index
parameter_list|,
name|Object
modifier|...
name|settings
parameter_list|)
throws|throws
name|Exception
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"Checking static index {}"
argument_list|,
name|index
argument_list|)
expr_stmt|;
name|Settings
name|nodeSettings
init|=
name|prepareBackwardsDataDir
argument_list|(
name|getDataPath
argument_list|(
name|index
operator|+
literal|".zip"
argument_list|)
argument_list|,
name|settings
argument_list|)
decl_stmt|;
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|nodeSettings
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|assertIndexSanity
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
DECL|method|assertIndexSanity
specifier|private
name|void
name|assertIndexSanity
parameter_list|(
name|String
name|index
parameter_list|)
block|{
name|GetIndexResponse
name|getIndexResponse
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
name|prepareGetIndex
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|getIndexResponse
operator|.
name|indices
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|index
argument_list|,
name|getIndexResponse
operator|.
name|indices
argument_list|()
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|ensureYellow
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|SearchResponse
name|test
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
name|index
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|test
operator|.
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

