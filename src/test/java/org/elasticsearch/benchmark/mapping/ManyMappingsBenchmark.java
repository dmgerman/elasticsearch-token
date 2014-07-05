begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.benchmark.mapping
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|benchmark
operator|.
name|mapping
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
name|bulk
operator|.
name|BulkRequestBuilder
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
name|support
operator|.
name|IndicesOptions
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
name|jna
operator|.
name|Natives
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
name|unit
operator|.
name|TimeValue
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
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportModule
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
name|SETTING_NUMBER_OF_REPLICAS
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
name|SETTING_NUMBER_OF_SHARDS
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
name|settingsBuilder
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
name|node
operator|.
name|NodeBuilder
operator|.
name|nodeBuilder
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|ManyMappingsBenchmark
specifier|public
class|class
name|ManyMappingsBenchmark
block|{
DECL|field|MAPPING
specifier|private
specifier|static
specifier|final
name|String
name|MAPPING
init|=
literal|"{\n"
operator|+
literal|"        \"dynamic_templates\": [\n"
operator|+
literal|"          {\n"
operator|+
literal|"            \"t1\": {\n"
operator|+
literal|"              \"mapping\": {\n"
operator|+
literal|"                \"store\": false,\n"
operator|+
literal|"                \"norms\": {\n"
operator|+
literal|"                  \"enabled\": false\n"
operator|+
literal|"                },\n"
operator|+
literal|"                \"type\": \"string\"\n"
operator|+
literal|"              },\n"
operator|+
literal|"              \"match\": \"*_ss\"\n"
operator|+
literal|"            }\n"
operator|+
literal|"          },\n"
operator|+
literal|"          {\n"
operator|+
literal|"            \"t2\": {\n"
operator|+
literal|"              \"mapping\": {\n"
operator|+
literal|"                \"store\": false,\n"
operator|+
literal|"                \"type\": \"date\"\n"
operator|+
literal|"              },\n"
operator|+
literal|"              \"match\": \"*_dt\"\n"
operator|+
literal|"            }\n"
operator|+
literal|"          },\n"
operator|+
literal|"          {\n"
operator|+
literal|"            \"t3\": {\n"
operator|+
literal|"              \"mapping\": {\n"
operator|+
literal|"                \"store\": false,\n"
operator|+
literal|"                \"type\": \"integer\"\n"
operator|+
literal|"              },\n"
operator|+
literal|"              \"match\": \"*_i\"\n"
operator|+
literal|"            }\n"
operator|+
literal|"          }\n"
operator|+
literal|"        ],\n"
operator|+
literal|"        \"_source\": {\n"
operator|+
literal|"          \"enabled\": false\n"
operator|+
literal|"        },\n"
operator|+
literal|"        \"properties\": {}\n"
operator|+
literal|"      }"
decl_stmt|;
DECL|field|INDEX_NAME
specifier|private
specifier|static
specifier|final
name|String
name|INDEX_NAME
init|=
literal|"index"
decl_stmt|;
DECL|field|TYPE_NAME
specifier|private
specifier|static
specifier|final
name|String
name|TYPE_NAME
init|=
literal|"type"
decl_stmt|;
DECL|field|FIELD_COUNT
specifier|private
specifier|static
specifier|final
name|int
name|FIELD_COUNT
init|=
literal|100000
decl_stmt|;
DECL|field|DOC_COUNT
specifier|private
specifier|static
specifier|final
name|int
name|DOC_COUNT
init|=
literal|10000000
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
name|Natives
operator|.
name|tryMlockall
argument_list|()
expr_stmt|;
name|Settings
name|settings
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.refresh_interval"
argument_list|,
literal|"-1"
argument_list|)
operator|.
name|put
argument_list|(
literal|"gateway.type"
argument_list|,
literal|"local"
argument_list|)
operator|.
name|put
argument_list|(
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
literal|5
argument_list|)
operator|.
name|put
argument_list|(
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|0
argument_list|)
operator|.
name|put
argument_list|(
name|TransportModule
operator|.
name|TRANSPORT_TYPE_KEY
argument_list|,
literal|"local"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|String
name|clusterName
init|=
name|ManyMappingsBenchmark
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
decl_stmt|;
name|Node
name|node
init|=
name|nodeBuilder
argument_list|()
operator|.
name|clusterName
argument_list|(
name|clusterName
argument_list|)
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
argument_list|)
operator|.
name|node
argument_list|()
decl_stmt|;
name|Client
name|client
init|=
name|node
operator|.
name|client
argument_list|()
decl_stmt|;
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareDelete
argument_list|(
name|INDEX_NAME
argument_list|)
operator|.
name|setIndicesOptions
argument_list|(
name|IndicesOptions
operator|.
name|lenientExpandOpen
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareCreate
argument_list|(
name|INDEX_NAME
argument_list|)
operator|.
name|addMapping
argument_list|(
name|TYPE_NAME
argument_list|,
name|MAPPING
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|BulkRequestBuilder
name|builder
init|=
name|client
operator|.
name|prepareBulk
argument_list|()
decl_stmt|;
name|int
name|fieldCount
init|=
literal|0
decl_stmt|;
name|long
name|time
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
specifier|final
name|int
name|PRINT
init|=
literal|1000
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
name|DOC_COUNT
condition|;
name|i
operator|++
control|)
block|{
name|XContentBuilder
name|sourceBuilder
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
decl_stmt|;
name|sourceBuilder
operator|.
name|field
argument_list|(
operator|++
name|fieldCount
operator|+
literal|"_ss"
argument_list|,
literal|"xyz"
argument_list|)
expr_stmt|;
name|sourceBuilder
operator|.
name|field
argument_list|(
operator|++
name|fieldCount
operator|+
literal|"_dt"
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
name|sourceBuilder
operator|.
name|field
argument_list|(
operator|++
name|fieldCount
operator|+
literal|"_i"
argument_list|,
name|i
operator|%
literal|100
argument_list|)
expr_stmt|;
name|sourceBuilder
operator|.
name|endObject
argument_list|()
expr_stmt|;
if|if
condition|(
name|fieldCount
operator|>=
name|FIELD_COUNT
condition|)
block|{
name|fieldCount
operator|=
literal|0
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"dynamic fields rolled up"
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|add
argument_list|(
name|client
operator|.
name|prepareIndex
argument_list|(
name|INDEX_NAME
argument_list|,
name|TYPE_NAME
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|setSource
argument_list|(
name|sourceBuilder
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|builder
operator|.
name|numberOfActions
argument_list|()
operator|>=
literal|1000
condition|)
block|{
name|builder
operator|.
name|get
argument_list|()
expr_stmt|;
name|builder
operator|=
name|client
operator|.
name|prepareBulk
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|i
operator|%
name|PRINT
operator|==
literal|0
condition|)
block|{
name|long
name|took
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|time
decl_stmt|;
name|time
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Indexed "
operator|+
name|i
operator|+
literal|" docs, in "
operator|+
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
name|took
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|builder
operator|.
name|numberOfActions
argument_list|()
operator|>
literal|0
condition|)
block|{
name|builder
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

