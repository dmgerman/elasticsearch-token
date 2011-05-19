begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.translog
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|translog
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|Term
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
name|index
operator|.
name|shard
operator|.
name|ShardId
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|AfterMethod
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|BeforeMethod
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|Test
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
name|translog
operator|.
name|TranslogSizeMatcher
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|MatcherAssert
operator|.
name|*
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
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|AbstractSimpleTranslogTests
specifier|public
specifier|abstract
class|class
name|AbstractSimpleTranslogTests
block|{
DECL|field|shardId
specifier|protected
specifier|final
name|ShardId
name|shardId
init|=
operator|new
name|ShardId
argument_list|(
operator|new
name|Index
argument_list|(
literal|"index"
argument_list|)
argument_list|,
literal|1
argument_list|)
decl_stmt|;
DECL|field|translog
specifier|protected
name|Translog
name|translog
decl_stmt|;
DECL|method|setUp
annotation|@
name|BeforeMethod
specifier|public
name|void
name|setUp
parameter_list|()
block|{
name|translog
operator|=
name|create
argument_list|()
expr_stmt|;
name|translog
operator|.
name|newTranslog
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
DECL|method|tearDown
annotation|@
name|AfterMethod
specifier|public
name|void
name|tearDown
parameter_list|()
block|{
name|translog
operator|.
name|close
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
DECL|method|create
specifier|protected
specifier|abstract
name|Translog
name|create
parameter_list|()
function_decl|;
DECL|method|testTransientTranslog
annotation|@
name|Test
specifier|public
name|void
name|testTransientTranslog
parameter_list|()
block|{
name|Translog
operator|.
name|Snapshot
name|snapshot
init|=
name|translog
operator|.
name|snapshot
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|snapshot
argument_list|,
name|translogSize
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|snapshot
operator|.
name|release
argument_list|()
expr_stmt|;
name|translog
operator|.
name|add
argument_list|(
operator|new
name|Translog
operator|.
name|Create
argument_list|(
literal|"test"
argument_list|,
literal|"1"
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|1
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|snapshot
operator|=
name|translog
operator|.
name|snapshot
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot
argument_list|,
name|translogSize
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot
operator|.
name|estimatedTotalOperations
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|snapshot
operator|.
name|release
argument_list|()
expr_stmt|;
name|translog
operator|.
name|newTransientTranslog
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|snapshot
operator|=
name|translog
operator|.
name|snapshot
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot
argument_list|,
name|translogSize
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot
operator|.
name|estimatedTotalOperations
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|snapshot
operator|.
name|release
argument_list|()
expr_stmt|;
name|translog
operator|.
name|add
argument_list|(
operator|new
name|Translog
operator|.
name|Index
argument_list|(
literal|"test"
argument_list|,
literal|"2"
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|2
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|snapshot
operator|=
name|translog
operator|.
name|snapshot
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot
argument_list|,
name|translogSize
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot
operator|.
name|estimatedTotalOperations
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|snapshot
operator|.
name|release
argument_list|()
expr_stmt|;
name|translog
operator|.
name|makeTransientCurrent
argument_list|()
expr_stmt|;
name|snapshot
operator|=
name|translog
operator|.
name|snapshot
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot
argument_list|,
name|translogSize
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
comment|// now its one, since it only includes "2"
name|assertThat
argument_list|(
name|snapshot
operator|.
name|estimatedTotalOperations
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|snapshot
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
DECL|method|testSimpleOperations
annotation|@
name|Test
specifier|public
name|void
name|testSimpleOperations
parameter_list|()
block|{
name|Translog
operator|.
name|Snapshot
name|snapshot
init|=
name|translog
operator|.
name|snapshot
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|snapshot
argument_list|,
name|translogSize
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|snapshot
operator|.
name|release
argument_list|()
expr_stmt|;
name|translog
operator|.
name|add
argument_list|(
operator|new
name|Translog
operator|.
name|Create
argument_list|(
literal|"test"
argument_list|,
literal|"1"
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|1
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|snapshot
operator|=
name|translog
operator|.
name|snapshot
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot
argument_list|,
name|translogSize
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot
operator|.
name|estimatedTotalOperations
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|snapshot
operator|.
name|release
argument_list|()
expr_stmt|;
name|translog
operator|.
name|add
argument_list|(
operator|new
name|Translog
operator|.
name|Index
argument_list|(
literal|"test"
argument_list|,
literal|"2"
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|2
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|snapshot
operator|=
name|translog
operator|.
name|snapshot
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot
argument_list|,
name|translogSize
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot
operator|.
name|estimatedTotalOperations
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|snapshot
operator|.
name|release
argument_list|()
expr_stmt|;
name|translog
operator|.
name|add
argument_list|(
operator|new
name|Translog
operator|.
name|Delete
argument_list|(
name|newUid
argument_list|(
literal|"3"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|snapshot
operator|=
name|translog
operator|.
name|snapshot
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot
argument_list|,
name|translogSize
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot
operator|.
name|estimatedTotalOperations
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|snapshot
operator|.
name|release
argument_list|()
expr_stmt|;
name|translog
operator|.
name|add
argument_list|(
operator|new
name|Translog
operator|.
name|DeleteByQuery
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|4
block|}
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|snapshot
operator|=
name|translog
operator|.
name|snapshot
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot
argument_list|,
name|translogSize
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot
operator|.
name|estimatedTotalOperations
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|snapshot
operator|.
name|release
argument_list|()
expr_stmt|;
name|snapshot
operator|=
name|translog
operator|.
name|snapshot
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot
operator|.
name|hasNext
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|Translog
operator|.
name|Create
name|create
init|=
operator|(
name|Translog
operator|.
name|Create
operator|)
name|snapshot
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|create
operator|.
name|source
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|1
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot
operator|.
name|hasNext
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|Translog
operator|.
name|Index
name|index
init|=
operator|(
name|Translog
operator|.
name|Index
operator|)
name|snapshot
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|index
operator|.
name|source
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|2
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot
operator|.
name|hasNext
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|Translog
operator|.
name|Delete
name|delete
init|=
operator|(
name|Translog
operator|.
name|Delete
operator|)
name|snapshot
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|delete
operator|.
name|uid
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|newUid
argument_list|(
literal|"3"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot
operator|.
name|hasNext
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|Translog
operator|.
name|DeleteByQuery
name|deleteByQuery
init|=
operator|(
name|Translog
operator|.
name|DeleteByQuery
operator|)
name|snapshot
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|deleteByQuery
operator|.
name|source
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|4
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot
operator|.
name|hasNext
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|snapshot
operator|.
name|release
argument_list|()
expr_stmt|;
name|long
name|firstId
init|=
name|translog
operator|.
name|currentId
argument_list|()
decl_stmt|;
name|translog
operator|.
name|newTranslog
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|translog
operator|.
name|currentId
argument_list|()
argument_list|,
name|Matchers
operator|.
name|not
argument_list|(
name|equalTo
argument_list|(
name|firstId
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|snapshot
operator|=
name|translog
operator|.
name|snapshot
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot
argument_list|,
name|translogSize
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot
operator|.
name|estimatedTotalOperations
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|snapshot
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
DECL|method|testSnapshot
annotation|@
name|Test
specifier|public
name|void
name|testSnapshot
parameter_list|()
block|{
name|Translog
operator|.
name|Snapshot
name|snapshot
init|=
name|translog
operator|.
name|snapshot
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|snapshot
argument_list|,
name|translogSize
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|snapshot
operator|.
name|release
argument_list|()
expr_stmt|;
name|translog
operator|.
name|add
argument_list|(
operator|new
name|Translog
operator|.
name|Create
argument_list|(
literal|"test"
argument_list|,
literal|"1"
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|1
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|snapshot
operator|=
name|translog
operator|.
name|snapshot
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot
argument_list|,
name|translogSize
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot
operator|.
name|estimatedTotalOperations
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|snapshot
operator|.
name|release
argument_list|()
expr_stmt|;
name|snapshot
operator|=
name|translog
operator|.
name|snapshot
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot
operator|.
name|hasNext
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|Translog
operator|.
name|Create
name|create
init|=
operator|(
name|Translog
operator|.
name|Create
operator|)
name|snapshot
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|create
operator|.
name|source
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|1
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|snapshot
operator|.
name|release
argument_list|()
expr_stmt|;
name|Translog
operator|.
name|Snapshot
name|snapshot1
init|=
name|translog
operator|.
name|snapshot
argument_list|()
decl_stmt|;
comment|// we use the translogSize to also navigate to the last position on this snapshot
comment|// so snapshot(Snapshot) will work properly
name|assertThat
argument_list|(
name|snapshot1
argument_list|,
name|translogSize
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot1
operator|.
name|estimatedTotalOperations
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|translog
operator|.
name|add
argument_list|(
operator|new
name|Translog
operator|.
name|Index
argument_list|(
literal|"test"
argument_list|,
literal|"2"
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|2
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|snapshot
operator|=
name|translog
operator|.
name|snapshot
argument_list|(
name|snapshot1
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot
argument_list|,
name|translogSize
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot
operator|.
name|estimatedTotalOperations
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|snapshot
operator|.
name|release
argument_list|()
expr_stmt|;
name|snapshot
operator|=
name|translog
operator|.
name|snapshot
argument_list|(
name|snapshot1
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot
operator|.
name|hasNext
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|Translog
operator|.
name|Index
name|index
init|=
operator|(
name|Translog
operator|.
name|Index
operator|)
name|snapshot
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|index
operator|.
name|source
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|2
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot
operator|.
name|hasNext
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot
operator|.
name|estimatedTotalOperations
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|snapshot
operator|.
name|release
argument_list|()
expr_stmt|;
name|snapshot1
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
DECL|method|testSnapshotWithNewTranslog
annotation|@
name|Test
specifier|public
name|void
name|testSnapshotWithNewTranslog
parameter_list|()
block|{
name|Translog
operator|.
name|Snapshot
name|snapshot
init|=
name|translog
operator|.
name|snapshot
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|snapshot
argument_list|,
name|translogSize
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|snapshot
operator|.
name|release
argument_list|()
expr_stmt|;
name|translog
operator|.
name|add
argument_list|(
operator|new
name|Translog
operator|.
name|Create
argument_list|(
literal|"test"
argument_list|,
literal|"1"
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|1
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|Translog
operator|.
name|Snapshot
name|actualSnapshot
init|=
name|translog
operator|.
name|snapshot
argument_list|()
decl_stmt|;
name|translog
operator|.
name|add
argument_list|(
operator|new
name|Translog
operator|.
name|Index
argument_list|(
literal|"test"
argument_list|,
literal|"2"
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|2
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|translog
operator|.
name|newTranslog
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|translog
operator|.
name|add
argument_list|(
operator|new
name|Translog
operator|.
name|Index
argument_list|(
literal|"test"
argument_list|,
literal|"3"
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|3
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|snapshot
operator|=
name|translog
operator|.
name|snapshot
argument_list|(
name|actualSnapshot
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot
argument_list|,
name|translogSize
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|snapshot
operator|.
name|release
argument_list|()
expr_stmt|;
name|snapshot
operator|=
name|translog
operator|.
name|snapshot
argument_list|(
name|actualSnapshot
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot
operator|.
name|hasNext
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|Translog
operator|.
name|Index
name|index
init|=
operator|(
name|Translog
operator|.
name|Index
operator|)
name|snapshot
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|index
operator|.
name|source
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|3
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot
operator|.
name|hasNext
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|actualSnapshot
operator|.
name|release
argument_list|()
expr_stmt|;
name|snapshot
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
DECL|method|testSnapshotWithSeekForward
annotation|@
name|Test
specifier|public
name|void
name|testSnapshotWithSeekForward
parameter_list|()
block|{
name|Translog
operator|.
name|Snapshot
name|snapshot
init|=
name|translog
operator|.
name|snapshot
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|snapshot
argument_list|,
name|translogSize
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|snapshot
operator|.
name|release
argument_list|()
expr_stmt|;
name|translog
operator|.
name|add
argument_list|(
operator|new
name|Translog
operator|.
name|Create
argument_list|(
literal|"test"
argument_list|,
literal|"1"
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|1
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|snapshot
operator|=
name|translog
operator|.
name|snapshot
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot
argument_list|,
name|translogSize
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|long
name|lastPosition
init|=
name|snapshot
operator|.
name|position
argument_list|()
decl_stmt|;
name|snapshot
operator|.
name|release
argument_list|()
expr_stmt|;
name|translog
operator|.
name|add
argument_list|(
operator|new
name|Translog
operator|.
name|Create
argument_list|(
literal|"test"
argument_list|,
literal|"2"
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|1
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|snapshot
operator|=
name|translog
operator|.
name|snapshot
argument_list|()
expr_stmt|;
name|snapshot
operator|.
name|seekForward
argument_list|(
name|lastPosition
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot
argument_list|,
name|translogSize
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|snapshot
operator|.
name|release
argument_list|()
expr_stmt|;
name|snapshot
operator|=
name|translog
operator|.
name|snapshot
argument_list|()
expr_stmt|;
name|snapshot
operator|.
name|seekForward
argument_list|(
name|lastPosition
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot
operator|.
name|hasNext
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|Translog
operator|.
name|Create
name|create
init|=
operator|(
name|Translog
operator|.
name|Create
operator|)
name|snapshot
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|create
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
name|snapshot
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
DECL|method|newUid
specifier|private
name|Term
name|newUid
parameter_list|(
name|String
name|id
parameter_list|)
block|{
return|return
operator|new
name|Term
argument_list|(
literal|"_uid"
argument_list|,
name|id
argument_list|)
return|;
block|}
block|}
end_class

end_unit

