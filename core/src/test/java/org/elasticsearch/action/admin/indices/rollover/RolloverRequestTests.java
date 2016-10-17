begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.rollover
package|package
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
name|rollover
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
name|ESTestCase
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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

begin_class
DECL|class|RolloverRequestTests
specifier|public
class|class
name|RolloverRequestTests
extends|extends
name|ESTestCase
block|{
DECL|method|testConditionsParsing
specifier|public
name|void
name|testConditionsParsing
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|RolloverRequest
name|request
init|=
operator|new
name|RolloverRequest
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|XContentBuilder
name|builder
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
literal|"conditions"
argument_list|)
operator|.
name|field
argument_list|(
literal|"max_age"
argument_list|,
literal|"10d"
argument_list|)
operator|.
name|field
argument_list|(
literal|"max_docs"
argument_list|,
literal|100
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
decl_stmt|;
name|request
operator|.
name|source
argument_list|(
name|builder
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|Condition
argument_list|>
name|conditions
init|=
name|request
operator|.
name|getConditions
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|conditions
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|Condition
name|condition
range|:
name|conditions
control|)
block|{
if|if
condition|(
name|condition
operator|instanceof
name|MaxAgeCondition
condition|)
block|{
name|MaxAgeCondition
name|maxAgeCondition
init|=
operator|(
name|MaxAgeCondition
operator|)
name|condition
decl_stmt|;
name|assertThat
argument_list|(
name|maxAgeCondition
operator|.
name|value
operator|.
name|getMillis
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|TimeValue
operator|.
name|timeValueHours
argument_list|(
literal|24
operator|*
literal|10
argument_list|)
operator|.
name|getMillis
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|condition
operator|instanceof
name|MaxDocsCondition
condition|)
block|{
name|MaxDocsCondition
name|maxDocsCondition
init|=
operator|(
name|MaxDocsCondition
operator|)
name|condition
decl_stmt|;
name|assertThat
argument_list|(
name|maxDocsCondition
operator|.
name|value
argument_list|,
name|equalTo
argument_list|(
literal|100L
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|fail
argument_list|(
literal|"unexpected condition "
operator|+
name|condition
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|testParsingWithIndexSettings
specifier|public
name|void
name|testParsingWithIndexSettings
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|RolloverRequest
name|request
init|=
operator|new
name|RolloverRequest
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|XContentBuilder
name|builder
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
literal|"conditions"
argument_list|)
operator|.
name|field
argument_list|(
literal|"max_age"
argument_list|,
literal|"10d"
argument_list|)
operator|.
name|field
argument_list|(
literal|"max_docs"
argument_list|,
literal|100
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"mappings"
argument_list|)
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
literal|"field1"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|field
argument_list|(
literal|"index"
argument_list|,
literal|"not_analyzed"
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
name|startObject
argument_list|(
literal|"settings"
argument_list|)
operator|.
name|field
argument_list|(
literal|"number_of_shards"
argument_list|,
literal|10
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"aliases"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"alias1"
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
name|request
operator|.
name|source
argument_list|(
name|builder
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|Condition
argument_list|>
name|conditions
init|=
name|request
operator|.
name|getConditions
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|conditions
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getCreateIndexRequest
argument_list|()
operator|.
name|mappings
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getCreateIndexRequest
argument_list|()
operator|.
name|aliases
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getCreateIndexRequest
argument_list|()
operator|.
name|settings
argument_list|()
operator|.
name|getAsInt
argument_list|(
literal|"number_of_shards"
argument_list|,
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
