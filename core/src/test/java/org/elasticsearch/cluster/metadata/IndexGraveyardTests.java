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
name|common
operator|.
name|UUIDs
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
name|io
operator|.
name|stream
operator|.
name|BytesStreamOutput
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
name|ToXContent
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
name|index
operator|.
name|Index
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
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
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
name|lessThan
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
name|not
import|;
end_import

begin_comment
comment|/**  * Tests for the {@link IndexGraveyard} class  */
end_comment

begin_class
DECL|class|IndexGraveyardTests
specifier|public
class|class
name|IndexGraveyardTests
extends|extends
name|ESTestCase
block|{
DECL|method|testEquals
specifier|public
name|void
name|testEquals
parameter_list|()
block|{
specifier|final
name|IndexGraveyard
name|graveyard
init|=
name|createRandom
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|graveyard
argument_list|,
name|equalTo
argument_list|(
name|IndexGraveyard
operator|.
name|builder
argument_list|(
name|graveyard
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|IndexGraveyard
operator|.
name|Builder
name|newGraveyard
init|=
name|IndexGraveyard
operator|.
name|builder
argument_list|(
name|graveyard
argument_list|)
decl_stmt|;
name|newGraveyard
operator|.
name|addTombstone
argument_list|(
operator|new
name|Index
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|4
argument_list|,
literal|15
argument_list|)
argument_list|,
name|UUIDs
operator|.
name|randomBase64UUID
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|newGraveyard
operator|.
name|build
argument_list|()
argument_list|,
name|not
argument_list|(
name|graveyard
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSerialization
specifier|public
name|void
name|testSerialization
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|IndexGraveyard
name|graveyard
init|=
name|createRandom
argument_list|()
decl_stmt|;
specifier|final
name|BytesStreamOutput
name|out
init|=
operator|new
name|BytesStreamOutput
argument_list|()
decl_stmt|;
name|graveyard
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|IndexGraveyard
operator|.
name|fromStream
argument_list|(
name|out
operator|.
name|bytes
argument_list|()
operator|.
name|streamInput
argument_list|()
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|graveyard
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testXContent
specifier|public
name|void
name|testXContent
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|IndexGraveyard
name|graveyard
init|=
name|createRandom
argument_list|()
decl_stmt|;
specifier|final
name|XContentBuilder
name|builder
init|=
name|JsonXContent
operator|.
name|contentBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|graveyard
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|ToXContent
operator|.
name|EMPTY_PARAMS
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|XContentParser
name|parser
init|=
name|createParser
argument_list|(
name|JsonXContent
operator|.
name|jsonXContent
argument_list|,
name|builder
operator|.
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
comment|// the beginning of the parser
name|assertThat
argument_list|(
name|IndexGraveyard
operator|.
name|PROTO
operator|.
name|fromXContent
argument_list|(
name|parser
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|graveyard
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testAddTombstones
specifier|public
name|void
name|testAddTombstones
parameter_list|()
block|{
specifier|final
name|IndexGraveyard
name|graveyard1
init|=
name|createRandom
argument_list|()
decl_stmt|;
specifier|final
name|IndexGraveyard
operator|.
name|Builder
name|graveyardBuidler
init|=
name|IndexGraveyard
operator|.
name|builder
argument_list|(
name|graveyard1
argument_list|)
decl_stmt|;
specifier|final
name|int
name|numAdds
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|4
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|numAdds
condition|;
name|j
operator|++
control|)
block|{
name|graveyardBuidler
operator|.
name|addTombstone
argument_list|(
operator|new
name|Index
argument_list|(
literal|"nidx-"
operator|+
name|j
argument_list|,
name|UUIDs
operator|.
name|randomBase64UUID
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|final
name|IndexGraveyard
name|graveyard2
init|=
name|graveyardBuidler
operator|.
name|build
argument_list|()
decl_stmt|;
if|if
condition|(
name|numAdds
operator|==
literal|0
condition|)
block|{
name|assertThat
argument_list|(
name|graveyard2
argument_list|,
name|equalTo
argument_list|(
name|graveyard1
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
name|graveyard2
argument_list|,
name|not
argument_list|(
name|graveyard1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|graveyard1
operator|.
name|getTombstones
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|lessThan
argument_list|(
name|graveyard2
operator|.
name|getTombstones
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Collections
operator|.
name|indexOfSubList
argument_list|(
name|graveyard2
operator|.
name|getTombstones
argument_list|()
argument_list|,
name|graveyard1
operator|.
name|getTombstones
argument_list|()
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testPurge
specifier|public
name|void
name|testPurge
parameter_list|()
block|{
comment|// try with max tombstones as some positive integer
name|executePurgeTestWithMaxTombstones
argument_list|(
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|20
argument_list|)
argument_list|)
expr_stmt|;
comment|// try with max tombstones as the default
name|executePurgeTestWithMaxTombstones
argument_list|(
name|IndexGraveyard
operator|.
name|SETTING_MAX_TOMBSTONES
operator|.
name|getDefault
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testDiffs
specifier|public
name|void
name|testDiffs
parameter_list|()
block|{
name|IndexGraveyard
operator|.
name|Builder
name|graveyardBuilder
init|=
name|IndexGraveyard
operator|.
name|builder
argument_list|()
decl_stmt|;
specifier|final
name|int
name|numToPurge
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|4
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Index
argument_list|>
name|removals
init|=
operator|new
name|ArrayList
argument_list|<>
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
name|numToPurge
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|Index
name|indexToRemove
init|=
operator|new
name|Index
argument_list|(
literal|"ridx-"
operator|+
name|i
argument_list|,
name|UUIDs
operator|.
name|randomBase64UUID
argument_list|()
argument_list|)
decl_stmt|;
name|graveyardBuilder
operator|.
name|addTombstone
argument_list|(
name|indexToRemove
argument_list|)
expr_stmt|;
name|removals
operator|.
name|add
argument_list|(
name|indexToRemove
argument_list|)
expr_stmt|;
block|}
specifier|final
name|int
name|numTombstones
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|4
argument_list|)
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
name|numTombstones
condition|;
name|i
operator|++
control|)
block|{
name|graveyardBuilder
operator|.
name|addTombstone
argument_list|(
operator|new
name|Index
argument_list|(
literal|"idx-"
operator|+
name|i
argument_list|,
name|UUIDs
operator|.
name|randomBase64UUID
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|final
name|IndexGraveyard
name|graveyard1
init|=
name|graveyardBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|graveyardBuilder
operator|=
name|IndexGraveyard
operator|.
name|builder
argument_list|(
name|graveyard1
argument_list|)
expr_stmt|;
specifier|final
name|int
name|numToAdd
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|4
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Index
argument_list|>
name|additions
init|=
operator|new
name|ArrayList
argument_list|<>
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
name|numToAdd
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|Index
name|indexToAdd
init|=
operator|new
name|Index
argument_list|(
literal|"nidx-"
operator|+
name|i
argument_list|,
name|UUIDs
operator|.
name|randomBase64UUID
argument_list|()
argument_list|)
decl_stmt|;
name|graveyardBuilder
operator|.
name|addTombstone
argument_list|(
name|indexToAdd
argument_list|)
expr_stmt|;
name|additions
operator|.
name|add
argument_list|(
name|indexToAdd
argument_list|)
expr_stmt|;
block|}
specifier|final
name|IndexGraveyard
name|graveyard2
init|=
name|graveyardBuilder
operator|.
name|build
argument_list|(
name|settingsWithMaxTombstones
argument_list|(
name|numTombstones
operator|+
name|numToAdd
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|int
name|numPurged
init|=
name|graveyardBuilder
operator|.
name|getNumPurged
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|numPurged
argument_list|,
name|equalTo
argument_list|(
name|numToPurge
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|IndexGraveyard
operator|.
name|IndexGraveyardDiff
name|diff
init|=
operator|new
name|IndexGraveyard
operator|.
name|IndexGraveyardDiff
argument_list|(
name|graveyard1
argument_list|,
name|graveyard2
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Index
argument_list|>
name|actualAdded
init|=
name|diff
operator|.
name|getAdded
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|t
lambda|->
name|t
operator|.
name|getIndex
argument_list|()
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|actualAdded
argument_list|)
argument_list|,
name|equalTo
argument_list|(
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|additions
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|diff
operator|.
name|getRemovedCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|removals
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testContains
specifier|public
name|void
name|testContains
parameter_list|()
block|{
name|List
argument_list|<
name|Index
argument_list|>
name|indices
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|int
name|numIndices
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
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
name|numIndices
condition|;
name|i
operator|++
control|)
block|{
name|indices
operator|.
name|add
argument_list|(
operator|new
name|Index
argument_list|(
literal|"idx-"
operator|+
name|i
argument_list|,
name|UUIDs
operator|.
name|randomBase64UUID
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|final
name|IndexGraveyard
operator|.
name|Builder
name|graveyard
init|=
name|IndexGraveyard
operator|.
name|builder
argument_list|()
decl_stmt|;
for|for
control|(
specifier|final
name|Index
name|index
range|:
name|indices
control|)
block|{
name|graveyard
operator|.
name|addTombstone
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
specifier|final
name|IndexGraveyard
name|indexGraveyard
init|=
name|graveyard
operator|.
name|build
argument_list|()
decl_stmt|;
for|for
control|(
specifier|final
name|Index
name|index
range|:
name|indices
control|)
block|{
name|assertTrue
argument_list|(
name|indexGraveyard
operator|.
name|containsIndex
argument_list|(
name|index
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertFalse
argument_list|(
name|indexGraveyard
operator|.
name|containsIndex
argument_list|(
operator|new
name|Index
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|6
argument_list|)
argument_list|,
name|UUIDs
operator|.
name|randomBase64UUID
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|createRandom
specifier|public
specifier|static
name|IndexGraveyard
name|createRandom
parameter_list|()
block|{
specifier|final
name|IndexGraveyard
operator|.
name|Builder
name|graveyard
init|=
name|IndexGraveyard
operator|.
name|builder
argument_list|()
decl_stmt|;
specifier|final
name|int
name|numTombstones
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|4
argument_list|)
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
name|numTombstones
condition|;
name|i
operator|++
control|)
block|{
name|graveyard
operator|.
name|addTombstone
argument_list|(
operator|new
name|Index
argument_list|(
literal|"idx-"
operator|+
name|i
argument_list|,
name|UUIDs
operator|.
name|randomBase64UUID
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|graveyard
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|executePurgeTestWithMaxTombstones
specifier|private
name|void
name|executePurgeTestWithMaxTombstones
parameter_list|(
specifier|final
name|int
name|maxTombstones
parameter_list|)
block|{
specifier|final
name|int
name|numExtra
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
specifier|final
name|IndexGraveyard
operator|.
name|Builder
name|graveyardBuilder
init|=
name|createWithDeletions
argument_list|(
name|maxTombstones
operator|+
name|numExtra
argument_list|)
decl_stmt|;
specifier|final
name|IndexGraveyard
name|graveyard
init|=
name|graveyardBuilder
operator|.
name|build
argument_list|(
name|settingsWithMaxTombstones
argument_list|(
name|maxTombstones
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|int
name|numPurged
init|=
name|graveyardBuilder
operator|.
name|getNumPurged
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|numPurged
argument_list|,
name|equalTo
argument_list|(
name|numExtra
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|graveyard
operator|.
name|getTombstones
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|maxTombstones
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|createWithDeletions
specifier|private
specifier|static
name|IndexGraveyard
operator|.
name|Builder
name|createWithDeletions
parameter_list|(
specifier|final
name|int
name|numAdd
parameter_list|)
block|{
specifier|final
name|IndexGraveyard
operator|.
name|Builder
name|graveyard
init|=
name|IndexGraveyard
operator|.
name|builder
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
name|numAdd
condition|;
name|i
operator|++
control|)
block|{
name|graveyard
operator|.
name|addTombstone
argument_list|(
operator|new
name|Index
argument_list|(
literal|"idx-"
operator|+
name|i
argument_list|,
name|UUIDs
operator|.
name|randomBase64UUID
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|graveyard
return|;
block|}
DECL|method|settingsWithMaxTombstones
specifier|private
specifier|static
name|Settings
name|settingsWithMaxTombstones
parameter_list|(
specifier|final
name|int
name|maxTombstones
parameter_list|)
block|{
return|return
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexGraveyard
operator|.
name|SETTING_MAX_TOMBSTONES
operator|.
name|getKey
argument_list|()
argument_list|,
name|maxTombstones
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit

