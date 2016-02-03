begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.messy.tests
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|messy
operator|.
name|tests
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
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
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
name|geo
operator|.
name|GeoDistance
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
name|plugins
operator|.
name|Plugin
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|Script
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|groovy
operator|.
name|GroovyPlugin
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
name|InternalSettingsPlugin
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
name|VersionUtils
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|hamcrest
operator|.
name|Matchers
operator|.
name|closeTo
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|GeoDistanceTests
specifier|public
class|class
name|GeoDistanceTests
extends|extends
name|ESIntegTestCase
block|{
annotation|@
name|Override
DECL|method|nodePlugins
specifier|protected
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
argument_list|>
name|nodePlugins
parameter_list|()
block|{
return|return
name|pluginList
argument_list|(
name|GroovyPlugin
operator|.
name|class
argument_list|,
name|InternalSettingsPlugin
operator|.
name|class
argument_list|)
return|;
block|}
DECL|method|testDistanceScript
specifier|public
name|void
name|testDistanceScript
parameter_list|()
throws|throws
name|Exception
block|{
name|double
name|source_lat
init|=
literal|32.798
decl_stmt|;
name|double
name|source_long
init|=
operator|-
literal|117.151
decl_stmt|;
name|double
name|target_lat
init|=
literal|32.81
decl_stmt|;
name|double
name|target_long
init|=
operator|-
literal|117.21
decl_stmt|;
name|Version
name|version
init|=
name|VersionUtils
operator|.
name|randomVersionBetween
argument_list|(
name|random
argument_list|()
argument_list|,
name|Version
operator|.
name|V_2_0_0
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
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
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|version
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|XContentBuilder
name|xContentBuilder
init|=
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
literal|"location"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"geo_point"
argument_list|)
decl_stmt|;
if|if
condition|(
name|version
operator|.
name|before
argument_list|(
name|Version
operator|.
name|V_2_2_0
argument_list|)
condition|)
block|{
name|xContentBuilder
operator|.
name|field
argument_list|(
literal|"lat_lon"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
name|xContentBuilder
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
expr_stmt|;
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
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type1"
argument_list|,
name|xContentBuilder
argument_list|)
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
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
literal|"name"
argument_list|,
literal|"TestPosition"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"location"
argument_list|)
operator|.
name|field
argument_list|(
literal|"lat"
argument_list|,
name|source_lat
argument_list|)
operator|.
name|field
argument_list|(
literal|"lon"
argument_list|,
name|source_long
argument_list|)
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
name|refresh
argument_list|()
expr_stmt|;
name|SearchResponse
name|searchResponse1
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|addField
argument_list|(
literal|"_source"
argument_list|)
operator|.
name|addScriptField
argument_list|(
literal|"distance"
argument_list|,
operator|new
name|Script
argument_list|(
literal|"doc['location'].arcDistance("
operator|+
name|target_lat
operator|+
literal|","
operator|+
name|target_long
operator|+
literal|")"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|Double
name|resultDistance1
init|=
name|searchResponse1
operator|.
name|getHits
argument_list|()
operator|.
name|getHits
argument_list|()
index|[
literal|0
index|]
operator|.
name|getFields
argument_list|()
operator|.
name|get
argument_list|(
literal|"distance"
argument_list|)
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|resultDistance1
argument_list|,
name|closeTo
argument_list|(
name|GeoDistance
operator|.
name|ARC
operator|.
name|calculate
argument_list|(
name|source_lat
argument_list|,
name|source_long
argument_list|,
name|target_lat
argument_list|,
name|target_long
argument_list|,
name|DistanceUnit
operator|.
name|DEFAULT
argument_list|)
argument_list|,
literal|0.01d
argument_list|)
argument_list|)
expr_stmt|;
name|SearchResponse
name|searchResponse2
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|addField
argument_list|(
literal|"_source"
argument_list|)
operator|.
name|addScriptField
argument_list|(
literal|"distance"
argument_list|,
operator|new
name|Script
argument_list|(
literal|"doc['location'].distance("
operator|+
name|target_lat
operator|+
literal|","
operator|+
name|target_long
operator|+
literal|")"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|Double
name|resultDistance2
init|=
name|searchResponse2
operator|.
name|getHits
argument_list|()
operator|.
name|getHits
argument_list|()
index|[
literal|0
index|]
operator|.
name|getFields
argument_list|()
operator|.
name|get
argument_list|(
literal|"distance"
argument_list|)
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|resultDistance2
argument_list|,
name|closeTo
argument_list|(
name|GeoDistance
operator|.
name|PLANE
operator|.
name|calculate
argument_list|(
name|source_lat
argument_list|,
name|source_long
argument_list|,
name|target_lat
argument_list|,
name|target_long
argument_list|,
name|DistanceUnit
operator|.
name|DEFAULT
argument_list|)
argument_list|,
literal|0.01d
argument_list|)
argument_list|)
expr_stmt|;
name|SearchResponse
name|searchResponse3
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|addField
argument_list|(
literal|"_source"
argument_list|)
operator|.
name|addScriptField
argument_list|(
literal|"distance"
argument_list|,
operator|new
name|Script
argument_list|(
literal|"doc['location'].arcDistanceInKm("
operator|+
name|target_lat
operator|+
literal|","
operator|+
name|target_long
operator|+
literal|")"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|Double
name|resultArcDistance3
init|=
name|searchResponse3
operator|.
name|getHits
argument_list|()
operator|.
name|getHits
argument_list|()
index|[
literal|0
index|]
operator|.
name|getFields
argument_list|()
operator|.
name|get
argument_list|(
literal|"distance"
argument_list|)
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|resultArcDistance3
argument_list|,
name|closeTo
argument_list|(
name|GeoDistance
operator|.
name|ARC
operator|.
name|calculate
argument_list|(
name|source_lat
argument_list|,
name|source_long
argument_list|,
name|target_lat
argument_list|,
name|target_long
argument_list|,
name|DistanceUnit
operator|.
name|KILOMETERS
argument_list|)
argument_list|,
literal|0.01d
argument_list|)
argument_list|)
expr_stmt|;
name|SearchResponse
name|searchResponse4
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|addField
argument_list|(
literal|"_source"
argument_list|)
operator|.
name|addScriptField
argument_list|(
literal|"distance"
argument_list|,
operator|new
name|Script
argument_list|(
literal|"doc['location'].distanceInKm("
operator|+
name|target_lat
operator|+
literal|","
operator|+
name|target_long
operator|+
literal|")"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|Double
name|resultDistance4
init|=
name|searchResponse4
operator|.
name|getHits
argument_list|()
operator|.
name|getHits
argument_list|()
index|[
literal|0
index|]
operator|.
name|getFields
argument_list|()
operator|.
name|get
argument_list|(
literal|"distance"
argument_list|)
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|resultDistance4
argument_list|,
name|closeTo
argument_list|(
name|GeoDistance
operator|.
name|PLANE
operator|.
name|calculate
argument_list|(
name|source_lat
argument_list|,
name|source_long
argument_list|,
name|target_lat
argument_list|,
name|target_long
argument_list|,
name|DistanceUnit
operator|.
name|KILOMETERS
argument_list|)
argument_list|,
literal|0.01d
argument_list|)
argument_list|)
expr_stmt|;
name|SearchResponse
name|searchResponse5
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|addField
argument_list|(
literal|"_source"
argument_list|)
operator|.
name|addScriptField
argument_list|(
literal|"distance"
argument_list|,
operator|new
name|Script
argument_list|(
literal|"doc['location'].arcDistanceInKm("
operator|+
operator|(
name|target_lat
operator|)
operator|+
literal|","
operator|+
operator|(
name|target_long
operator|+
literal|360
operator|)
operator|+
literal|")"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|Double
name|resultArcDistance5
init|=
name|searchResponse5
operator|.
name|getHits
argument_list|()
operator|.
name|getHits
argument_list|()
index|[
literal|0
index|]
operator|.
name|getFields
argument_list|()
operator|.
name|get
argument_list|(
literal|"distance"
argument_list|)
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|resultArcDistance5
argument_list|,
name|closeTo
argument_list|(
name|GeoDistance
operator|.
name|ARC
operator|.
name|calculate
argument_list|(
name|source_lat
argument_list|,
name|source_long
argument_list|,
name|target_lat
argument_list|,
name|target_long
argument_list|,
name|DistanceUnit
operator|.
name|KILOMETERS
argument_list|)
argument_list|,
literal|0.01d
argument_list|)
argument_list|)
expr_stmt|;
name|SearchResponse
name|searchResponse6
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|addField
argument_list|(
literal|"_source"
argument_list|)
operator|.
name|addScriptField
argument_list|(
literal|"distance"
argument_list|,
operator|new
name|Script
argument_list|(
literal|"doc['location'].arcDistanceInKm("
operator|+
operator|(
name|target_lat
operator|+
literal|360
operator|)
operator|+
literal|","
operator|+
operator|(
name|target_long
operator|)
operator|+
literal|")"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|Double
name|resultArcDistance6
init|=
name|searchResponse6
operator|.
name|getHits
argument_list|()
operator|.
name|getHits
argument_list|()
index|[
literal|0
index|]
operator|.
name|getFields
argument_list|()
operator|.
name|get
argument_list|(
literal|"distance"
argument_list|)
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|resultArcDistance6
argument_list|,
name|closeTo
argument_list|(
name|GeoDistance
operator|.
name|ARC
operator|.
name|calculate
argument_list|(
name|source_lat
argument_list|,
name|source_long
argument_list|,
name|target_lat
argument_list|,
name|target_long
argument_list|,
name|DistanceUnit
operator|.
name|KILOMETERS
argument_list|)
argument_list|,
literal|0.01d
argument_list|)
argument_list|)
expr_stmt|;
name|SearchResponse
name|searchResponse7
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|addField
argument_list|(
literal|"_source"
argument_list|)
operator|.
name|addScriptField
argument_list|(
literal|"distance"
argument_list|,
operator|new
name|Script
argument_list|(
literal|"doc['location'].arcDistanceInMiles("
operator|+
name|target_lat
operator|+
literal|","
operator|+
name|target_long
operator|+
literal|")"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|Double
name|resultDistance7
init|=
name|searchResponse7
operator|.
name|getHits
argument_list|()
operator|.
name|getHits
argument_list|()
index|[
literal|0
index|]
operator|.
name|getFields
argument_list|()
operator|.
name|get
argument_list|(
literal|"distance"
argument_list|)
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|resultDistance7
argument_list|,
name|closeTo
argument_list|(
name|GeoDistance
operator|.
name|ARC
operator|.
name|calculate
argument_list|(
name|source_lat
argument_list|,
name|source_long
argument_list|,
name|target_lat
argument_list|,
name|target_long
argument_list|,
name|DistanceUnit
operator|.
name|MILES
argument_list|)
argument_list|,
literal|0.01d
argument_list|)
argument_list|)
expr_stmt|;
name|SearchResponse
name|searchResponse8
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|addField
argument_list|(
literal|"_source"
argument_list|)
operator|.
name|addScriptField
argument_list|(
literal|"distance"
argument_list|,
operator|new
name|Script
argument_list|(
literal|"doc['location'].distanceInMiles("
operator|+
name|target_lat
operator|+
literal|","
operator|+
name|target_long
operator|+
literal|")"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|Double
name|resultDistance8
init|=
name|searchResponse8
operator|.
name|getHits
argument_list|()
operator|.
name|getHits
argument_list|()
index|[
literal|0
index|]
operator|.
name|getFields
argument_list|()
operator|.
name|get
argument_list|(
literal|"distance"
argument_list|)
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|resultDistance8
argument_list|,
name|closeTo
argument_list|(
name|GeoDistance
operator|.
name|PLANE
operator|.
name|calculate
argument_list|(
name|source_lat
argument_list|,
name|source_long
argument_list|,
name|target_lat
argument_list|,
name|target_long
argument_list|,
name|DistanceUnit
operator|.
name|MILES
argument_list|)
argument_list|,
literal|0.01d
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

