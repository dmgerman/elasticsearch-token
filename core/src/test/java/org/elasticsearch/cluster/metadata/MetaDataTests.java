begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.metadata
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
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
name|common
operator|.
name|bytes
operator|.
name|BytesReference
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
name|XContentParser
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
name|json
operator|.
name|JsonXContent
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
name|ESTestCase
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
name|is
import|;
end_import

begin_class
DECL|class|MetaDataTests
specifier|public
class|class
name|MetaDataTests
extends|extends
name|ESTestCase
block|{
DECL|method|testIndexAndAliasWithSameName
specifier|public
name|void
name|testIndexAndAliasWithSameName
parameter_list|()
block|{
name|IndexMetaData
operator|.
name|Builder
name|builder
init|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
literal|"index"
argument_list|)
operator|.
name|settings
argument_list|(
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
name|CURRENT
argument_list|)
argument_list|)
operator|.
name|numberOfShards
argument_list|(
literal|1
argument_list|)
operator|.
name|numberOfReplicas
argument_list|(
literal|0
argument_list|)
operator|.
name|putAlias
argument_list|(
name|AliasMetaData
operator|.
name|builder
argument_list|(
literal|"index"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|builder
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"exception should have been thrown"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalStateException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"index and alias names need to be unique, but alias [index] and index [index] have the same name"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testResolveIndexRouting
specifier|public
name|void
name|testResolveIndexRouting
parameter_list|()
block|{
name|IndexMetaData
operator|.
name|Builder
name|builder
init|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
literal|"index"
argument_list|)
operator|.
name|settings
argument_list|(
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
name|CURRENT
argument_list|)
argument_list|)
operator|.
name|numberOfShards
argument_list|(
literal|1
argument_list|)
operator|.
name|numberOfReplicas
argument_list|(
literal|0
argument_list|)
operator|.
name|putAlias
argument_list|(
name|AliasMetaData
operator|.
name|builder
argument_list|(
literal|"alias0"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|putAlias
argument_list|(
name|AliasMetaData
operator|.
name|builder
argument_list|(
literal|"alias1"
argument_list|)
operator|.
name|routing
argument_list|(
literal|"1"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|putAlias
argument_list|(
name|AliasMetaData
operator|.
name|builder
argument_list|(
literal|"alias2"
argument_list|)
operator|.
name|routing
argument_list|(
literal|"1,2"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|MetaData
name|metaData
init|=
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|builder
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
comment|// no alias, no index
name|assertEquals
argument_list|(
name|metaData
operator|.
name|resolveIndexRouting
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|metaData
operator|.
name|resolveIndexRouting
argument_list|(
literal|null
argument_list|,
literal|"0"
argument_list|,
literal|null
argument_list|)
argument_list|,
literal|"0"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|metaData
operator|.
name|resolveIndexRouting
argument_list|(
literal|"32"
argument_list|,
literal|"0"
argument_list|,
literal|null
argument_list|)
argument_list|,
literal|"0"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|metaData
operator|.
name|resolveIndexRouting
argument_list|(
literal|"32"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|,
literal|"32"
argument_list|)
expr_stmt|;
comment|// index, no alias
name|assertEquals
argument_list|(
name|metaData
operator|.
name|resolveIndexRouting
argument_list|(
literal|"32"
argument_list|,
literal|"0"
argument_list|,
literal|"index"
argument_list|)
argument_list|,
literal|"0"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|metaData
operator|.
name|resolveIndexRouting
argument_list|(
literal|"32"
argument_list|,
literal|null
argument_list|,
literal|"index"
argument_list|)
argument_list|,
literal|"32"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|metaData
operator|.
name|resolveIndexRouting
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|"index"
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|metaData
operator|.
name|resolveIndexRouting
argument_list|(
literal|null
argument_list|,
literal|"0"
argument_list|,
literal|"index"
argument_list|)
argument_list|,
literal|"0"
argument_list|)
expr_stmt|;
comment|// alias with no index routing
name|assertEquals
argument_list|(
name|metaData
operator|.
name|resolveIndexRouting
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|"alias0"
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|metaData
operator|.
name|resolveIndexRouting
argument_list|(
literal|null
argument_list|,
literal|"0"
argument_list|,
literal|"alias0"
argument_list|)
argument_list|,
literal|"0"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|metaData
operator|.
name|resolveIndexRouting
argument_list|(
literal|"32"
argument_list|,
literal|null
argument_list|,
literal|"alias0"
argument_list|)
argument_list|,
literal|"32"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|metaData
operator|.
name|resolveIndexRouting
argument_list|(
literal|"32"
argument_list|,
literal|"0"
argument_list|,
literal|"alias0"
argument_list|)
argument_list|,
literal|"0"
argument_list|)
expr_stmt|;
comment|// alias with index routing.
name|assertEquals
argument_list|(
name|metaData
operator|.
name|resolveIndexRouting
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|"alias1"
argument_list|)
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|metaData
operator|.
name|resolveIndexRouting
argument_list|(
literal|"32"
argument_list|,
literal|null
argument_list|,
literal|"alias1"
argument_list|)
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|metaData
operator|.
name|resolveIndexRouting
argument_list|(
literal|"32"
argument_list|,
literal|"1"
argument_list|,
literal|"alias1"
argument_list|)
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
try|try
block|{
name|metaData
operator|.
name|resolveIndexRouting
argument_list|(
literal|null
argument_list|,
literal|"0"
argument_list|,
literal|"alias1"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should fail"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
name|assertThat
argument_list|(
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"Alias [alias1] has index routing associated with it [1], and was provided with routing value [0], rejecting operation"
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|metaData
operator|.
name|resolveIndexRouting
argument_list|(
literal|"32"
argument_list|,
literal|"0"
argument_list|,
literal|"alias1"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should fail"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
name|assertThat
argument_list|(
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"Alias [alias1] has index routing associated with it [1], and was provided with routing value [0], rejecting operation"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// alias with invalid index routing.
try|try
block|{
name|metaData
operator|.
name|resolveIndexRouting
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|"alias2"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should fail"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
name|assertThat
argument_list|(
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"index/alias [alias2] provided with routing value [1,2] that resolved to several routing values, rejecting operation"
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|metaData
operator|.
name|resolveIndexRouting
argument_list|(
literal|null
argument_list|,
literal|"1"
argument_list|,
literal|"alias2"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should fail"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
name|assertThat
argument_list|(
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"index/alias [alias2] provided with routing value [1,2] that resolved to several routing values, rejecting operation"
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|metaData
operator|.
name|resolveIndexRouting
argument_list|(
literal|"32"
argument_list|,
literal|null
argument_list|,
literal|"alias2"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should fail"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
name|assertThat
argument_list|(
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"index/alias [alias2] provided with routing value [1,2] that resolved to several routing values, rejecting operation"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testUnknownFieldClusterMetaData
specifier|public
name|void
name|testUnknownFieldClusterMetaData
parameter_list|()
throws|throws
name|IOException
block|{
name|BytesReference
name|metadata
init|=
name|JsonXContent
operator|.
name|contentBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"meta-data"
argument_list|)
operator|.
name|field
argument_list|(
literal|"random"
argument_list|,
literal|"value"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
decl_stmt|;
name|XContentParser
name|parser
init|=
name|JsonXContent
operator|.
name|jsonXContent
operator|.
name|createParser
argument_list|(
name|metadata
argument_list|)
decl_stmt|;
try|try
block|{
name|MetaData
operator|.
name|Builder
operator|.
name|fromXContent
argument_list|(
name|parser
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"Unexpected field [random]"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testUnknownFieldIndexMetaData
specifier|public
name|void
name|testUnknownFieldIndexMetaData
parameter_list|()
throws|throws
name|IOException
block|{
name|BytesReference
name|metadata
init|=
name|JsonXContent
operator|.
name|contentBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"index_name"
argument_list|)
operator|.
name|field
argument_list|(
literal|"random"
argument_list|,
literal|"value"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
decl_stmt|;
name|XContentParser
name|parser
init|=
name|JsonXContent
operator|.
name|jsonXContent
operator|.
name|createParser
argument_list|(
name|metadata
argument_list|)
decl_stmt|;
try|try
block|{
name|IndexMetaData
operator|.
name|Builder
operator|.
name|fromXContent
argument_list|(
name|parser
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"Unexpected field [random]"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

