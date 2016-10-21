begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
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
name|xcontent
operator|.
name|ToXContent
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
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|containsString
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|object
operator|.
name|HasToString
operator|.
name|hasToString
import|;
end_import

begin_class
DECL|class|NodeIndicesStatsTests
specifier|public
class|class
name|NodeIndicesStatsTests
extends|extends
name|ESTestCase
block|{
DECL|method|testInvalidLevel
specifier|public
name|void
name|testInvalidLevel
parameter_list|()
block|{
specifier|final
name|NodeIndicesStats
name|stats
init|=
operator|new
name|NodeIndicesStats
argument_list|()
decl_stmt|;
specifier|final
name|String
name|level
init|=
name|randomAsciiOfLength
argument_list|(
literal|16
argument_list|)
decl_stmt|;
specifier|final
name|ToXContent
operator|.
name|Params
name|params
init|=
operator|new
name|ToXContent
operator|.
name|MapParams
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"level"
argument_list|,
name|level
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|IllegalArgumentException
name|e
init|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|stats
operator|.
name|toXContent
argument_list|(
literal|null
argument_list|,
name|params
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
argument_list|,
name|hasToString
argument_list|(
name|containsString
argument_list|(
literal|"level parameter must be one of [indices] or [node] or [shards] but was ["
operator|+
name|level
operator|+
literal|"]"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

