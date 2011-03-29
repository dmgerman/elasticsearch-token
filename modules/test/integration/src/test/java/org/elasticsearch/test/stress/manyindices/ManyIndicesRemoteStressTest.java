begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.stress.manyindices
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|stress
operator|.
name|manyindices
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
name|logging
operator|.
name|ESLogger
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
name|logging
operator|.
name|Loggers
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
name|ImmutableSettings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|node
operator|.
name|Node
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|node
operator|.
name|NodeBuilder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Date
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|ManyIndicesRemoteStressTest
specifier|public
class|class
name|ManyIndicesRemoteStressTest
block|{
DECL|field|logger
specifier|private
specifier|static
specifier|final
name|ESLogger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|ManyIndicesRemoteStressTest
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|method|main
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|System
operator|.
name|setProperty
argument_list|(
literal|"es.logger.prefix"
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|int
name|numberOfShards
init|=
literal|1
decl_stmt|;
name|int
name|numberOfReplicas
init|=
literal|1
decl_stmt|;
name|int
name|numberOfIndices
init|=
literal|1000
decl_stmt|;
name|int
name|numberOfDocs
init|=
literal|0
decl_stmt|;
name|Node
name|node
init|=
name|NodeBuilder
operator|.
name|nodeBuilder
argument_list|()
operator|.
name|client
argument_list|(
literal|true
argument_list|)
operator|.
name|node
argument_list|()
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
name|numberOfIndices
condition|;
name|i
operator|++
control|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"START index [{}] ..."
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|node
operator|.
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareCreate
argument_list|(
literal|"index_"
operator|+
name|i
argument_list|)
operator|.
name|setSettings
argument_list|(
name|ImmutableSettings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
name|numberOfShards
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_replicas"
argument_list|,
name|numberOfReplicas
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|numberOfDocs
condition|;
name|j
operator|++
control|)
block|{
name|node
operator|.
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"index_"
operator|+
name|i
argument_list|,
literal|"type"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|"test"
argument_list|,
literal|"field2"
argument_list|,
literal|2
argument_list|,
literal|"field3"
argument_list|,
operator|new
name|Date
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"DONE  index [{}]"
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
name|node
operator|.
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareGatewaySnapshot
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"closing node..."
argument_list|)
expr_stmt|;
name|node
operator|.
name|close
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"node closed"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

