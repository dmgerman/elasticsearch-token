begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.benchmark.percolator
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|benchmark
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
name|client
operator|.
name|Client
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
name|StopWatch
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
name|common
operator|.
name|xcontent
operator|.
name|XContentBuilder
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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Requests
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
operator|.
name|*
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
name|settings
operator|.
name|ImmutableSettings
operator|.
name|*
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
name|*
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
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|node
operator|.
name|NodeBuilder
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|SinglePercolatorStressBenchmark
specifier|public
class|class
name|SinglePercolatorStressBenchmark
block|{
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
name|Settings
name|settings
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"cluster.routing.schedule"
argument_list|,
literal|200
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|.
name|put
argument_list|(
literal|"gateway.type"
argument_list|,
literal|"none"
argument_list|)
operator|.
name|put
argument_list|(
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
literal|2
argument_list|)
operator|.
name|put
argument_list|(
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|1
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Node
index|[]
name|nodes
init|=
operator|new
name|Node
index|[
literal|2
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
name|nodes
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|nodes
index|[
name|i
index|]
operator|=
name|nodeBuilder
argument_list|()
operator|.
name|settings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|settings
argument_list|)
operator|.
name|put
argument_list|(
literal|"name"
argument_list|,
literal|"node"
operator|+
name|i
argument_list|)
argument_list|)
operator|.
name|node
argument_list|()
expr_stmt|;
block|}
name|Node
name|client
init|=
name|nodeBuilder
argument_list|()
operator|.
name|settings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|settings
argument_list|)
operator|.
name|put
argument_list|(
literal|"name"
argument_list|,
literal|"client"
argument_list|)
argument_list|)
operator|.
name|client
argument_list|(
literal|true
argument_list|)
operator|.
name|node
argument_list|()
decl_stmt|;
name|Client
name|client1
init|=
name|client
operator|.
name|client
argument_list|()
decl_stmt|;
name|client1
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|create
argument_list|(
name|createIndexRequest
argument_list|(
literal|"test"
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|int
name|COUNT
init|=
literal|200000
decl_stmt|;
name|int
name|QUERIES
init|=
literal|10
decl_stmt|;
comment|// register queries
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|QUERIES
condition|;
name|i
operator|++
control|)
block|{
name|client1
operator|.
name|prepareIndex
argument_list|(
literal|"_percolator"
argument_list|,
literal|"test"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
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
literal|"name"
argument_list|,
literal|"value"
argument_list|)
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|setRefresh
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
name|StopWatch
name|stopWatch
init|=
operator|new
name|StopWatch
argument_list|()
operator|.
name|start
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Percolating ["
operator|+
name|COUNT
operator|+
literal|"] ..."
argument_list|)
expr_stmt|;
name|int
name|i
init|=
literal|1
decl_stmt|;
for|for
control|(
init|;
name|i
operator|<=
name|COUNT
condition|;
name|i
operator|++
control|)
block|{
name|PercolateResponse
name|percolate
init|=
name|client1
operator|.
name|preparePercolate
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|)
operator|.
name|setSource
argument_list|(
name|source
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|,
literal|"value"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
if|if
condition|(
name|percolate
operator|.
name|matches
argument_list|()
operator|.
name|size
argument_list|()
operator|!=
name|QUERIES
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"No matching number of queries"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|i
operator|%
literal|10000
operator|)
operator|==
literal|0
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Percolated "
operator|+
name|i
operator|+
literal|" took "
operator|+
name|stopWatch
operator|.
name|stop
argument_list|()
operator|.
name|lastTaskTime
argument_list|()
argument_list|)
expr_stmt|;
name|stopWatch
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Percolation took "
operator|+
name|stopWatch
operator|.
name|totalTime
argument_list|()
operator|+
literal|", TPS "
operator|+
operator|(
operator|(
operator|(
name|double
operator|)
name|COUNT
operator|)
operator|/
name|stopWatch
operator|.
name|totalTime
argument_list|()
operator|.
name|secondsFrac
argument_list|()
operator|)
argument_list|)
expr_stmt|;
name|client
operator|.
name|close
argument_list|()
expr_stmt|;
for|for
control|(
name|Node
name|node
range|:
name|nodes
control|)
block|{
name|node
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|source
specifier|private
specifier|static
name|XContentBuilder
name|source
parameter_list|(
name|String
name|id
parameter_list|,
name|String
name|nameValue
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|time
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
return|return
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"doc"
argument_list|)
operator|.
name|field
argument_list|(
literal|"id"
argument_list|,
name|id
argument_list|)
operator|.
name|field
argument_list|(
literal|"numeric1"
argument_list|,
name|time
argument_list|)
operator|.
name|field
argument_list|(
literal|"numeric2"
argument_list|,
name|time
argument_list|)
operator|.
name|field
argument_list|(
literal|"numeric3"
argument_list|,
name|time
argument_list|)
operator|.
name|field
argument_list|(
literal|"numeric4"
argument_list|,
name|time
argument_list|)
operator|.
name|field
argument_list|(
literal|"numeric5"
argument_list|,
name|time
argument_list|)
operator|.
name|field
argument_list|(
literal|"numeric6"
argument_list|,
name|time
argument_list|)
operator|.
name|field
argument_list|(
literal|"numeric7"
argument_list|,
name|time
argument_list|)
operator|.
name|field
argument_list|(
literal|"numeric8"
argument_list|,
name|time
argument_list|)
operator|.
name|field
argument_list|(
literal|"numeric9"
argument_list|,
name|time
argument_list|)
operator|.
name|field
argument_list|(
literal|"numeric10"
argument_list|,
name|time
argument_list|)
operator|.
name|field
argument_list|(
literal|"name"
argument_list|,
name|nameValue
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
return|;
block|}
block|}
end_class

end_unit

