begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.timestamp
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|timestamp
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
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|mapping
operator|.
name|put
operator|.
name|PutMappingResponse
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
name|get
operator|.
name|GetResponse
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
name|Priority
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
name|java
operator|.
name|util
operator|.
name|Arrays
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
import|import
name|java
operator|.
name|util
operator|.
name|Locale
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|WriteRequest
operator|.
name|RefreshPolicy
operator|.
name|IMMEDIATE
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
name|equalTo
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

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|is
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
name|lessThanOrEqualTo
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
name|notNullValue
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|SimpleTimestampIT
specifier|public
class|class
name|SimpleTimestampIT
extends|extends
name|ESIntegTestCase
block|{
DECL|field|BW_SETTINGS
specifier|private
specifier|static
specifier|final
name|Settings
name|BW_SETTINGS
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|Version
operator|.
name|V_2_3_0
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
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
name|Arrays
operator|.
name|asList
argument_list|(
name|InternalSettingsPlugin
operator|.
name|class
argument_list|)
return|;
block|}
DECL|method|testSimpleTimestamp
specifier|public
name|void
name|testSimpleTimestamp
parameter_list|()
throws|throws
name|Exception
block|{
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
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|BW_SETTINGS
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type1"
argument_list|,
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
literal|"_timestamp"
argument_list|)
operator|.
name|field
argument_list|(
literal|"enabled"
argument_list|,
literal|true
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
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
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
argument_list|()
operator|.
name|setWaitForEvents
argument_list|(
name|Priority
operator|.
name|LANGUID
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
name|logger
operator|.
name|info
argument_list|(
literal|"--> check with automatic timestamp"
argument_list|)
expr_stmt|;
name|long
name|now1
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
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
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|setRefreshPolicy
argument_list|(
name|IMMEDIATE
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|long
name|now2
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
comment|// non realtime get (stored)
name|GetResponse
name|getResponse
init|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setStoredFields
argument_list|(
literal|"_timestamp"
argument_list|)
operator|.
name|setRealtime
argument_list|(
name|randomBoolean
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|long
name|timestamp
init|=
operator|(
operator|(
name|Number
operator|)
name|getResponse
operator|.
name|getField
argument_list|(
literal|"_timestamp"
argument_list|)
operator|.
name|getValue
argument_list|()
operator|)
operator|.
name|longValue
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|timestamp
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
name|now1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|timestamp
argument_list|,
name|lessThanOrEqualTo
argument_list|(
name|now2
argument_list|)
argument_list|)
expr_stmt|;
comment|// verify its the same timestamp when going the replica
name|getResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setStoredFields
argument_list|(
literal|"_timestamp"
argument_list|)
operator|.
name|setRealtime
argument_list|(
name|randomBoolean
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|Number
operator|)
name|getResponse
operator|.
name|getField
argument_list|(
literal|"_timestamp"
argument_list|)
operator|.
name|getValue
argument_list|()
operator|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|timestamp
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> check with custom timestamp (numeric)"
argument_list|)
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
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|setTimestamp
argument_list|(
literal|"10"
argument_list|)
operator|.
name|setRefreshPolicy
argument_list|(
name|IMMEDIATE
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|getResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setStoredFields
argument_list|(
literal|"_timestamp"
argument_list|)
operator|.
name|setRealtime
argument_list|(
literal|false
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|timestamp
operator|=
operator|(
operator|(
name|Number
operator|)
name|getResponse
operator|.
name|getField
argument_list|(
literal|"_timestamp"
argument_list|)
operator|.
name|getValue
argument_list|()
operator|)
operator|.
name|longValue
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|timestamp
argument_list|,
name|equalTo
argument_list|(
literal|10L
argument_list|)
argument_list|)
expr_stmt|;
comment|// verify its the same timestamp when going the replica
name|getResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setStoredFields
argument_list|(
literal|"_timestamp"
argument_list|)
operator|.
name|setRealtime
argument_list|(
literal|false
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|Number
operator|)
name|getResponse
operator|.
name|getField
argument_list|(
literal|"_timestamp"
argument_list|)
operator|.
name|getValue
argument_list|()
operator|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|timestamp
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> check with custom timestamp (string)"
argument_list|)
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
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|setTimestamp
argument_list|(
literal|"1970-01-01T00:00:00.020"
argument_list|)
operator|.
name|setRefreshPolicy
argument_list|(
name|IMMEDIATE
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|getResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setStoredFields
argument_list|(
literal|"_timestamp"
argument_list|)
operator|.
name|setRealtime
argument_list|(
literal|false
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|timestamp
operator|=
operator|(
operator|(
name|Number
operator|)
name|getResponse
operator|.
name|getField
argument_list|(
literal|"_timestamp"
argument_list|)
operator|.
name|getValue
argument_list|()
operator|)
operator|.
name|longValue
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|timestamp
argument_list|,
name|equalTo
argument_list|(
literal|20L
argument_list|)
argument_list|)
expr_stmt|;
comment|// verify its the same timestamp when going the replica
name|getResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setStoredFields
argument_list|(
literal|"_timestamp"
argument_list|)
operator|.
name|setRealtime
argument_list|(
literal|false
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|Number
operator|)
name|getResponse
operator|.
name|getField
argument_list|(
literal|"_timestamp"
argument_list|)
operator|.
name|getValue
argument_list|()
operator|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|timestamp
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// issue #5053
DECL|method|testThatUpdatingMappingShouldNotRemoveTimestampConfiguration
specifier|public
name|void
name|testThatUpdatingMappingShouldNotRemoveTimestampConfiguration
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|index
init|=
literal|"foo"
decl_stmt|;
name|String
name|type
init|=
literal|"mytype"
decl_stmt|;
name|XContentBuilder
name|builder
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"_timestamp"
argument_list|)
operator|.
name|field
argument_list|(
literal|"enabled"
argument_list|,
literal|true
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
decl_stmt|;
name|assertAcked
argument_list|(
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
name|index
argument_list|)
operator|.
name|setSettings
argument_list|(
name|BW_SETTINGS
argument_list|)
operator|.
name|addMapping
argument_list|(
name|type
argument_list|,
name|builder
argument_list|)
argument_list|)
expr_stmt|;
comment|// check mapping again
name|assertTimestampMappingEnabled
argument_list|(
name|index
argument_list|,
name|type
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// update some field in the mapping
name|XContentBuilder
name|updateMappingBuilder
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"otherField"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"text"
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
decl_stmt|;
name|PutMappingResponse
name|putMappingResponse
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
name|preparePutMapping
argument_list|(
name|index
argument_list|)
operator|.
name|setType
argument_list|(
name|type
argument_list|)
operator|.
name|setSource
argument_list|(
name|updateMappingBuilder
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertAcked
argument_list|(
name|putMappingResponse
argument_list|)
expr_stmt|;
comment|// make sure timestamp field is still in mapping
name|assertTimestampMappingEnabled
argument_list|(
name|index
argument_list|,
name|type
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
DECL|method|testThatTimestampCanBeSwitchedOnAndOff
specifier|public
name|void
name|testThatTimestampCanBeSwitchedOnAndOff
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|index
init|=
literal|"foo"
decl_stmt|;
name|String
name|type
init|=
literal|"mytype"
decl_stmt|;
name|XContentBuilder
name|builder
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"_timestamp"
argument_list|)
operator|.
name|field
argument_list|(
literal|"enabled"
argument_list|,
literal|true
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
decl_stmt|;
name|assertAcked
argument_list|(
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
name|index
argument_list|)
operator|.
name|setSettings
argument_list|(
name|BW_SETTINGS
argument_list|)
operator|.
name|addMapping
argument_list|(
name|type
argument_list|,
name|builder
argument_list|)
argument_list|)
expr_stmt|;
comment|// check mapping again
name|assertTimestampMappingEnabled
argument_list|(
name|index
argument_list|,
name|type
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// update some field in the mapping
name|XContentBuilder
name|updateMappingBuilder
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"_timestamp"
argument_list|)
operator|.
name|field
argument_list|(
literal|"enabled"
argument_list|,
literal|false
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
decl_stmt|;
name|PutMappingResponse
name|putMappingResponse
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
name|preparePutMapping
argument_list|(
name|index
argument_list|)
operator|.
name|setType
argument_list|(
name|type
argument_list|)
operator|.
name|setSource
argument_list|(
name|updateMappingBuilder
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertAcked
argument_list|(
name|putMappingResponse
argument_list|)
expr_stmt|;
comment|// make sure timestamp field is still in mapping
name|assertTimestampMappingEnabled
argument_list|(
name|index
argument_list|,
name|type
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
DECL|method|assertTimestampMappingEnabled
specifier|private
name|void
name|assertTimestampMappingEnabled
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|type
parameter_list|,
name|boolean
name|enabled
parameter_list|)
block|{
name|GetMappingsResponse
name|getMappingsResponse
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
argument_list|(
name|index
argument_list|)
operator|.
name|addTypes
argument_list|(
name|type
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|MappingMetaData
operator|.
name|Timestamp
name|timestamp
init|=
name|getMappingsResponse
operator|.
name|getMappings
argument_list|()
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|get
argument_list|(
name|type
argument_list|)
operator|.
name|timestamp
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|timestamp
argument_list|,
name|is
argument_list|(
name|notNullValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|errMsg
init|=
name|String
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
literal|"Expected timestamp field mapping to be "
operator|+
operator|(
name|enabled
condition|?
literal|"enabled"
else|:
literal|"disabled"
operator|)
operator|+
literal|" for %s/%s"
argument_list|,
name|index
argument_list|,
name|type
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|errMsg
argument_list|,
name|timestamp
operator|.
name|enabled
argument_list|()
argument_list|,
name|is
argument_list|(
name|enabled
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

