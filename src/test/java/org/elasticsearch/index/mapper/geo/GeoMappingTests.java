begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.geo
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|geo
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
name|admin
operator|.
name|indices
operator|.
name|mapping
operator|.
name|get
operator|.
name|GetMappingsRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|MappingMetaData
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
name|collect
operator|.
name|ImmutableOpenMap
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
name|DistanceUnit
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
name|DistanceUnit
operator|.
name|Distance
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
name|XContentFactory
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
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_class
DECL|class|GeoMappingTests
specifier|public
class|class
name|GeoMappingTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
DECL|method|testUpdatePrecision
specifier|public
name|void
name|testUpdatePrecision
parameter_list|()
throws|throws
name|Exception
block|{
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type1"
argument_list|,
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"pin"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"geo_point"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"fielddata"
argument_list|)
operator|.
name|field
argument_list|(
literal|"format"
argument_list|,
literal|"compressed"
argument_list|)
operator|.
name|field
argument_list|(
literal|"precision"
argument_list|,
literal|"2mm"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|ensureYellow
argument_list|()
expr_stmt|;
name|assertPrecision
argument_list|(
operator|new
name|Distance
argument_list|(
literal|2
argument_list|,
name|DistanceUnit
operator|.
name|MILLIMETERS
argument_list|)
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|preparePutMapping
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|setSource
argument_list|(
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"pin"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"geo_point"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"fielddata"
argument_list|)
operator|.
name|field
argument_list|(
literal|"format"
argument_list|,
literal|"compressed"
argument_list|)
operator|.
name|field
argument_list|(
literal|"precision"
argument_list|,
literal|"11m"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertPrecision
argument_list|(
operator|new
name|Distance
argument_list|(
literal|11
argument_list|,
name|DistanceUnit
operator|.
name|METERS
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|assertPrecision
specifier|private
name|void
name|assertPrecision
parameter_list|(
name|Distance
name|expected
parameter_list|)
throws|throws
name|Exception
block|{
name|ImmutableOpenMap
argument_list|<
name|String
argument_list|,
name|ImmutableOpenMap
argument_list|<
name|String
argument_list|,
name|MappingMetaData
argument_list|>
argument_list|>
name|mappings
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
name|getMappings
argument_list|(
operator|new
name|GetMappingsRequest
argument_list|()
operator|.
name|indices
argument_list|(
literal|"test"
argument_list|)
operator|.
name|types
argument_list|(
literal|"type1"
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
operator|.
name|getMappings
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|mappings
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|?
argument_list|>
name|properties
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|?
argument_list|>
operator|)
name|mappings
operator|.
name|get
argument_list|(
literal|"test"
argument_list|)
operator|.
name|get
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|getSourceAsMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"properties"
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|?
argument_list|>
name|pinProperties
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|?
argument_list|>
operator|)
name|properties
operator|.
name|get
argument_list|(
literal|"pin"
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|?
argument_list|>
name|pinFieldData
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|?
argument_list|>
operator|)
name|pinProperties
operator|.
name|get
argument_list|(
literal|"fielddata"
argument_list|)
decl_stmt|;
name|Distance
name|precision
init|=
name|Distance
operator|.
name|parseDistance
argument_list|(
name|pinFieldData
operator|.
name|get
argument_list|(
literal|"precision"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|precision
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

